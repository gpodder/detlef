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


package at.ac.tuwien.detlef.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.dragontek.mygpoclient.feeds.IFeed;

public class FeedUpdate implements IFeed {

    private static final String TAG = FeedUpdate.class.getName();

    private final String title;
    private final String link;
    private final String description;
    private final String url;
    private final List<Episode> episodes;
    private final long lastRelease;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IEpisode[] getEpisodes() {
        IEpisode[] ret = new IEpisode[episodes.size()];

        int i = 0;
        for (IEpisode e : episodes) {
            ret[i++] = e;
        }

        return ret;
    }

    public List<Episode> getEpisodeList() {
        return Collections.unmodifiableList(episodes);
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Constructs a FeedUpdate containing all Episodes which were added to the podcast after its
     * last update time.
     * @param iFeed The original feed
     * @param podcast The podcast it belongs to.
     */
    public FeedUpdate(IFeed iFeed, Podcast podcast) {
        IEpisode[] iepisodes = iFeed.getEpisodes();
        episodes = new ArrayList<Episode>(iepisodes.length);
        long lastRelease = podcast.getLastUpdate();
        for (IEpisode ie : iepisodes) {
            // TODO: skip Episodes without enclosure, find a way to correctly handle this case.
            try {
                if (ie.getEnclosure() == null) {
                    continue;
                }
            } catch (Exception ex) {
                Log.i(TAG, "missing enclosure " + ex.getMessage());
                continue;
            }
            if (ie.getReleased() <= podcast.getLastUpdate()) {
                continue;
            }
            if (ie.getReleased() > lastRelease) {
                lastRelease = ie.getReleased();
            }
            episodes.add(new Episode(ie, podcast));
        }

        this.lastRelease = lastRelease;
        title = iFeed.getTitle();
        description = iFeed.getDescription();
        url = iFeed.getUrl();
        link = iFeed.getLink();
    }

    public long getLastReleaseTime() {
        return lastRelease;
    }
}
