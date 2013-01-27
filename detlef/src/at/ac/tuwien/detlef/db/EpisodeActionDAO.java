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

package at.ac.tuwien.detlef.db;

import java.util.List;

import at.ac.tuwien.detlef.domain.LocalEpisodeAction;
import at.ac.tuwien.detlef.domain.RemoteEpisodeAction;

/**
 * DAO for episode action access.
 */
public interface EpisodeActionDAO {

    /**
     * Inserts an episode action into the database.
     *
     * @param episodeAction The episode action which should be saved into the
     *            database.
     * @return Returns true on success, false otherwise.
     */
    boolean insertEpisodeAction(LocalEpisodeAction episodeAction);

    /**
     * Delivers all episode actions which are stored in the database.
     *
     * @return Returns a list of all episode actions.
     */
    List<RemoteEpisodeAction> getAllEpisodeActions();

    /**
     * Deletes all specified episode actions from the database. This should be
     * called after they were persisted on the gpodder service.
     *
     * @param episodeActions The episode actions to delete.
     * @return Returns true on success, false otherwise.
     */
    boolean flushEpisodeActions(List<RemoteEpisodeAction> episodeActions);

}
