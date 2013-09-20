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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import net.x4a42.volksempfaenger.feedparser.Feed;
import net.x4a42.volksempfaenger.feedparser.FeedParser;
import net.x4a42.volksempfaenger.feedparser.FeedParserException;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.FeedUpdate;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.PullFeedResultEvent;

import com.google.gson.JsonParseException;

import de.greenrobot.event.EventBus;

/* TODO: Port to PodderIntentService. */

/**
 * A Runnable to fetch feed changes. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to implement
 * the Callback's handle & handleFailure methods.
 */
public class PullFeedAsyncTask implements Runnable {

    private static final int GENERIC_ERROR = -1;

    private static final String TAG = PullFeedAsyncTask.class.getName();

    private final Bundle bundle;
    private final Podcast podcast;

    public PullFeedAsyncTask(Bundle bundle, Podcast podcast) {
        this.bundle = bundle;
        this.podcast = podcast;
    }

    @Override
    public void run() {
        String err = Detlef.getAppContext().getString(R.string.no_podcast_specified);

        if (podcast == null) {
            sendError(GENERIC_ERROR, err);
        }

        FeedUpdate feed = null;
        try {
            /* Get the feed */
            Feed f = parseFeed(podcast.getUrl());

            feed = new FeedUpdate(f, podcast);

            insertEpisodes(Detlef.getAppContext(), podcast, feed.getEpisodeList());

            /* Update last changed timestamp.*/
            podcast.setLastUpdate(feed.getLastReleaseTime());

            PodcastDAO pdao = Singletons.i().getPodcastDAO();
            pdao.update(podcast);
        } catch (JsonParseException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        } catch (ClientProtocolException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
            return;
        } catch (IOException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
            return;
        } catch (FeedParserException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
            return;
        }

        /* Tell receiver we're done.. */
        EventBus.getDefault().post(new PullFeedResultEvent(ErrorCode.SUCCESS, bundle));
    }

    private static Feed parseFeed(String url) throws FeedParserException, IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            return FeedParser.parse(in);
        } finally {
            if (in != null) { try { in.close(); } catch (IOException e) { } }
        }

    }

    /**
     * Called when the task encounters an error. The given error code and string are sent.
     * The Task should exit after this has been called.
     *
     * @param errCode The error code.
     * @param errString The error string.
     */
    private void sendError(int errCode, String errString) {
        EventBus.getDefault().post(new PullFeedResultEvent(ErrorCode.GENERIC_FAILURE, bundle));
    }

    private void insertEpisodes(Context context, Podcast p, List<Episode> episodes) {
        try {
            EpisodeDAO dao = Singletons.i().getEpisodeDAO();
            for (Episode ep : episodes) {
                dao.insertEpisode(ep);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }
}
