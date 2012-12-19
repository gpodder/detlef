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

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.feeds.IFeed;

/**
 * provides database access for episodes.
 */
public interface EpisodeDBAssistant {

    /**
     * Delivers all episodes from one podcast.
     * 
     * @param context : The application context eg.
     *            Activity.getApplicationContext());
     * @param podcast : The podcast which contains the episodes
     * @return returns all episodes of the given podcast
     */
    List<Episode> getEpisodes(Context context, Podcast podcast);

    /**
     * Delivers all episodes.
     * 
     * @param context : The application context eg.
     *            Activity.getApplicationContext());
     * @return returns all episodes which are stored in the database
     */
    List<Episode> getAllEpisodes(Context context);

    /**
     * applies all changes in the EpisodeActionChanges to the database.
     * 
     * @param context: the application context
     * @param changes: the changes made in an episode
     */
    void applyActionChanges(Context context, EpisodeActionChanges changes);

    /**
     * ??
     */
    void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed);

    /**
     * Gets an episode by database id.
     * 
     * @param context : The application context eg.
     *            Activity.getApplicationContext());
     * @param id The ID of the episode in the database.
     * @return The episode with the given ID, if any. Otherwise null.
     */
    Episode getEpisodeById(Context context, int id);

    /**
     * Toggle the ActionState of the episode between read and unread
     * (new/play/download vs delete).
     * 
     * @param episode The episode whose state to toggle.
     */
    void toggleEpisodeReadState(Episode episode);
}
