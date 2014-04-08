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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.x4a42.volksempfaenger.feedparser.Feed;
import net.x4a42.volksempfaenger.feedparser.FeedItem;

import com.dragontek.mygpoclient.feeds.IFeed;

public class FeedUpdate implements IFeed {

    private final String title;
    private final String link;
    private final String description;
    private final String url;
    private final List<Episode> episodes = new LinkedList<Episode>();
    private final long lastRelease;

    private static final long MS_PER_SEC = 1000;

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
     * @param f The original feed
     * @param podcast The podcast it belongs to.
     */
    public FeedUpdate(Feed f, Podcast podcast) {
        List<FeedItem> items = f.items;

        long lastRelease = podcast.getLastUpdate();
        for (FeedItem fi : items) {
            // TODO: skip Episodes without enclosure, find a way to correctly handle this case.
            if (fi.enclosures.isEmpty()) {
                continue;
            }

            final long released = fi.date.getTime() / MS_PER_SEC;
            if (released <= podcast.getLastUpdate()) {
                continue;
            }

            lastRelease = Math.max(lastRelease, released);

            episodes.add(new Episode(fi.enclosures.get(0), podcast));
        }

        this.lastRelease = lastRelease;
        title = f.title;
        description = f.description;
        url = f.url;
        link = f.website;
    }

    public long getLastReleaseTime() {
        return lastRelease;
    }
}
