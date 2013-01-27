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

import com.dragontek.mygpoclient.api.EpisodeAction;


public class RemoteEpisodeAction extends EpisodeAction {
    private final long id;

    /**
     * This should only be created inside of an EpisodeActionDAO.
     *
     * @param id
     * @param podcast
     * @param episode
     * @param action
     * @param device
     * @param timestamp
     * @param started
     * @param position
     * @param total
     */
    public RemoteEpisodeAction(long id, String podcast, String episode, String action,
                               String device, String timestamp, Integer started, Integer position, Integer total) {
        super(podcast, episode, action, device, timestamp, started, position, total);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
