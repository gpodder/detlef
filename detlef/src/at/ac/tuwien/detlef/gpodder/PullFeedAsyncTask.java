/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */


package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.FeedUpdate;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.feeds.FeedServiceClient;
import com.dragontek.mygpoclient.feeds.FeedServiceResponse;

/**
 * A Runnable to fetch feed changes. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to implement
 * the Callback's handle & handleFailure methods.
 */
public class PullFeedAsyncTask implements Runnable {

    private static final int GENERIC_ERROR = -1;

    private final NoDataResultHandler<?> callback;
    private final Podcast podcast;

    public PullFeedAsyncTask(NoDataResultHandler<?> callback, Podcast podcast) {
        this.callback = callback;
        this.podcast = podcast;
    }

    @Override
    public void run() {
        String err = Detlef.getAppContext().getString(R.string.no_podcast_specified);

        if (podcast == null) {
            sendError(GENERIC_ERROR, err);
        }

        long since = podcast.getLastUpdate();

        /* Retrieve settings.*/
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        String username = gps.getUsername();
        String password = gps.getPassword();

        FeedServiceClient fsc = new FeedServiceClient(
                "http://" + gps.getFeedHostname(),
                username,
                password
                );

        FeedUpdate feed = null;
        try {
            /* Get the feed */
            FeedServiceResponse fsr = fsc.parseFeeds(new String[] {podcast.getUrl()}, since);
            if (fsr == null || fsr.size() == 0) {
                String e = Detlef.getAppContext().getString(R.string.failed_to_download_feed);
                sendError(GENERIC_ERROR, String.format("%s: %s", podcast.getTitle(), e));
                return;
            }

            feed = new FeedUpdate(fsr.get(0), podcast);

            DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
            .upsertAndDeleteEpisodes(Detlef.getAppContext(), podcast, feed);

            /* Update last changed timestamp.*/
            podcast.setLastUpdate(feed.getLastReleaseTime());
            PodcastDAOImpl.i().updateLastUpdate(podcast);
        } catch (ClientProtocolException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
            return;
        } catch (IOException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
            return;
        }

        /* Tell receiver we're done.. */
        callback.sendEvent(new NoDataResultHandler.NoDataSuccessEvent(callback));
    }

    /**
     * Called when the task encounters an error. The given error code and string are sent.
     * The Task should exit after this has been called.
     *
     * @param errCode The error code.
     * @param errString The error string.
     */
    private void sendError(int errCode, String errString) {
        callback.sendEvent(new ResultHandler.GenericFailureEvent(callback, errCode, errString));
    }
}
