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

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.feeds.IFeed;

/* TODO: The DB implementation is in need of some refactoring.
 *
 * Most of the functionality is implemented in DAOImpl classes, but
 * some (with no reason) is in DBAssistants. Originally, it was
 * intended to have public DBAssistant classes and to keep DAOImpl's
 * private. This is still doable but requires some effort.
 *
 * DB object caching (episodes/podcasts/...) should be moved into a separate
 * class layer (decorators).
 */

/**
 * provides database access for episodes.
 */
public interface EpisodeDBAssistant {

    /**
     * applies all changes in the EpisodeActionChanges to the database.
     *
     * @param context: the application context
     * @param changes: the changes made in an episode
     */
    void applyActionChanges(Context context, EpisodeActionChanges changes);

    void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed);

    /**
     * Toggle the ActionState of the episode between read and unread
     * (new/play/download vs delete).
     *
     * @param episode The episode whose state to toggle.
     */
    void toggleEpisodeReadState(Episode episode);
}
