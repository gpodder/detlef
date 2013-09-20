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

import java.util.List;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * PodListModel consists of a list of podcasts.
 */
public class PodListModel<T extends IPodcast> {

    private final List<T> podlist;

    public PodListModel(List<T> podlist) {
        this.podlist = podlist;
    }

    public T get(int position) {
        return podlist.get(position);
    }

    public void addPodcast(T podcast) {
        podlist.add(podcast);
    }

    public void removePodcast(IPodcast podcast) {
        podlist.remove(podcast);
    }

    public void update(List<T> podlist) {
        this.podlist.clear();
        this.podlist.addAll(podlist);
    }
}
