package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.feeds.FeedServiceClient;
import com.dragontek.mygpoclient.feeds.IFeed;

/**
 * An IntentService to fetch feed changes.
 *
 * It is started via an intent and gets a podcast as parameter. It sends a reply via the Intent
 * action "at.ac.tuwien.detlef.custom.intent.action.PULL_FEED". The user of the Task needs
 * a receiver for this action.
 */
public class PullFeedAsyncTask extends IntentService {
    /** Logging tag. */
    private static final String TAG = "PullFeedAsyncTask";

    /** The reply-Intent's action. */
    static final String ACTION = "at.ac.tuwien.detlef.custom.intent.action.PULL_FEED";

    /** Key for the podcast extra variable. */
    public static final String EXTRA_PODCAST = "EXTRA_PODCAST";

    /** Key for the state extra variable. */
    static final String EXTRA_STATE = "EXTRA_STATE";

    /** Key for the exception extra variable. */
    static final String EXTRA_EXCEPTION = "EXTRA_EXCEPTION";

    /** Key for the changes extra variable. */
    static final String EXTRA_CHANGES = "EXTRA_CHANGES";

    /** Key for the feed extra variable. */
    static final String EXTRA_FEED = "EXTRA_CHANGES";

    /** The Task was successful. */
    static final int TASK_SUCC = 0;

    /** The Task was failed. */
    static final int TASK_FAIL = 1;

    /** The host for creating a FeedServiceClient. */
    private static final String HOST = "http://feeds.gpodder.net";

    public PullFeedAsyncTask() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        String err = getString(R.string.no_podcast_specified);

        /* Get the podcast from our intent. */
        Podcast podcast = null;
        try {
            podcast = (Podcast) intent.getSerializableExtra(EXTRA_PODCAST);
        } catch (Exception e) {
            err = e.getLocalizedMessage();
        }

        if (podcast == null) {
            sendError(new GPodderException(err));
        }

        // TODO: get since
        long since = 0;

        /* Retrieve settings. */
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant().getGpodderSettings(this);

        String deviceID = gps.getDevicename();
        String username = gps.getUsername();
        String password = gps.getPassword();

        FeedServiceClient fsc = new FeedServiceClient(HOST, username, password);
        MygPodderClient gpc = new MygPodderClient(username, password);

        IFeed feed = null;
        EpisodeActionChanges changes = null;
        try {
            /* Get the feed */
            feed = fsc.parseFeeds(new String[] {podcast.getUrl()}, since).get(0);

            /* Get episode actions */
            changes = gpc.downloadEpisodeActions(since, podcast.getUrl(), deviceID);

            // TODO: We should think about updating the db here and set the update time last.

            /* Update last changed timestamp. */
            // TODO: setLastUpdate
        } catch (ClientProtocolException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        } catch (IOException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        }

        if (changes == null) {
            return;
        }

        /* Send the result. */
        Intent reply = new Intent(ACTION);
        reply.putExtra(EXTRA_STATE, TASK_SUCC);
        // TODO: make it work
        //reply.putExtra(EXTRA_CHANGES, changes);
        //reply.putExtra(EXTRA_FEED, feed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(reply);
    }

    /**
     * Called when the task encounters an error.
     * The given Exception is sent. The Task should exit after this has been called.
     * @param e An Exception describing the error.
     */
    private void sendError(GPodderException e) {
        Intent reply = new Intent(ACTION);
        reply.putExtra(EXTRA_STATE, TASK_FAIL);
        reply.putExtra(EXTRA_EXCEPTION, e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(reply);
    }
}
