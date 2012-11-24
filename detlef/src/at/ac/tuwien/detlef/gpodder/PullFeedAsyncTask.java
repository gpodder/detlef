package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.FeedUpdate;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.feeds.FeedServiceClient;
import com.dragontek.mygpoclient.feeds.IFeed;

/**
 * A Runnable to fetch feed changes. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to implement
 * the Callback's handle & handleFailure methods.
 */
public class PullFeedAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = "PullFeedAsyncTask";

    /** The host for creating a FeedServiceClient. */
    private static final String HOST = "http://feeds.gpodder.net";

    private final FeedSyncResultHandler<? extends Activity> callback;
    private final Podcast podcast;

    public PullFeedAsyncTask(FeedSyncResultHandler<? extends Activity> callback, Podcast podcast) {
        this.callback = callback;
        this.podcast = podcast;
    }

    @Override
    public void run() {
        String err = Detlef.getAppContext().getString(R.string.no_podcast_specified);

        if (podcast == null) {
            sendError(new GPodderException(err));
        }

        long since = podcast.getLastUpdate();;

        /* Retrieve settings.*/
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        String deviceID = gps.getDevicename();
        String username = gps.getUsername();
        String password = gps.getPassword();

        FeedServiceClient fsc = new FeedServiceClient(HOST, username, password);
        MygPodderClient gpc = new MygPodderClient(username, password);

        FeedUpdate feed = null;
        EpisodeActionChanges changes = null;
        try {
            /* Get the feed */
            IFeed iFeed = fsc.parseFeeds(new String[] {podcast.getUrl()}, since).get(0);
            if (iFeed == null) {
                String e = Detlef.getAppContext().getString(R.string.failed_to_download_feed);
                sendError(new GPodderException(String.format("%s: %s", podcast.getTitle(), e)));
                return;
            }

            feed = new FeedUpdate(iFeed, podcast);

            DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
            .upsertAndDeleteEpisodes(Detlef.getAppContext(), podcast, feed);

            /* Get episode actions */
            changes = gpc.downloadEpisodeActions(since, podcast.getUrl(), deviceID);

            DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
            .applyActionChanges(Detlef.getAppContext(), podcast, changes);

            /* Update last changed timestamp.*/
            podcast.setLastUpdate(feed.getLastReleaseTime());
            PodcastDAOImpl.i(Detlef.getAppContext()).updateLastUpdate(podcast);
        } catch (ClientProtocolException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        } catch (IOException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        }

        if (changes == null) {
            return;
        }

        /* Tell receiver we're done.. */
        callback.sendEvent(new FeedSyncResultHandler.FeedSyncEventSuccess(callback));
    }

    /**
     * Called when the task encounters an error.
     * The given Exception is sent. The Task should exit after this has been called.
     * @param e An Exception describing the error.
     */
    private void sendError(GPodderException e) {
        callback.sendEvent(new FeedSyncResultHandler.FeedSyncEventError(callback, e));
    }
}
