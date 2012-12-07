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


package at.ac.tuwien.detlef.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

/**
 * EpisodeListModel consists of a list of episodes.
 */
public class EpisodeListModel {

    /** Contains all known episodes. */
    private final List<Episode> eplist;

    public EpisodeListModel(List<Episode> eplist) {
        this.eplist = eplist;
    }

    public Episode get(int position) {
        return eplist.get(position);
    }

    public List<Episode> getAll() {
        return Collections.unmodifiableList(eplist);
    }

    public List<Episode> getByPodcast(Podcast podcast) {
        List<Episode> filtered = new ArrayList<Episode>(eplist);

        for (int i = filtered.size() - 1; i >= 0; i--) {
            Episode e = filtered.get(i);
            Podcast p = e.getPodcast();
            if (p == null || p.getId() != podcast.getId()) {
                filtered.remove(i);
            }
        }

        return filtered;
    }

    public void addEpisode(Episode episode) {
        eplist.add(episode);
    }

    public void removeEpisode(IEpisode episode) {
        eplist.remove(episode);
    }
}
