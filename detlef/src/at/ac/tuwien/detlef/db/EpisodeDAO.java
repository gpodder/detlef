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

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * DAO for episode access.
 */
public interface EpisodeDAO {

    /**
     * Interface for listeners interested in episode status changes.
     */
    public interface OnEpisodeChangeListener {
        void onEpisodeChanged(Episode episode);
        void onEpisodeAdded(Episode episode);
        void onEpisodeDeleted(Episode episode);
    }

    /**
     * inserts an episode into the database.
     *
     * @param episode
     *            : the episode which should be saved into the database
     * @return returns the refreshed Episode, null if error occurs
     */
    Episode insertEpisode(Episode episode);

    /**
     * deletes an episode from the database.
     *
     * @param episode
     *            : the episode which should be deleted (the id will be used for
     *            this)
     * @return returns the number of deleted episodes (if no one is deleted &lt1
     *         will be returned)
     */
    int deleteEpisode(Episode episode);

    /**
     * delivers all episodes which are stored in the database.
     *
     * @return returns a list of all episodes
     */
    List<Episode> getAllEpisodes();

    /**
     * delivers all episodes from a given podcast.
     *
     * @param podcast
     *            : the podcast which contains the episodes
     * @return returns a list of episodes which belongs to the given podcast
     */
    List<Episode> getEpisodes(Podcast podcast);

    /**
     * updates the filePath column of the given episode (uses the filePath
     * attribute of the episode).
     *
     * @param episode
     *            the episode which should be updated
     * @return the number of updated rows
     */
    int updateFilePath(Episode episode);

    /**
     * updates the storage state column of the given episode (uses the state attribute
     * of the episode).
     *
     * @param episode
     *            the episode which should be updated
     * @return the number of updated rows
     */
    int updateStorageState(Episode episode);


    /**
     * updates the playposition of the given episode.
     *
     * @param episode the episode which should be updated
     *
     * @return the number of updated rows
     */
    int updatePlayPosition(Episode episode);

    /**
     * updates the actionstate of the given episode.
     *
     * @param episode the episode which should be updated
     *
     * @return the number of updated rows
     */
    int updateActionState(Episode episode);

    /**
     * Gets an episode by ID.
     *
     * @param id
     *            The ID of the episode to fetch.
     * @return The episode with the given ID, if it exists. Otherwise null.
     */
    Episode getEpisode(long id);

    /**
     * Gets an episode by the given guid or the url (both should be unique).
     *
     * @param url
     *            the url of the episode
     * @param guid
     *            the guid of the episode
     * @return the episode
     */
    Episode getEpisodeByUrlOrGuid(String url, String guid);

    /**
     * Creates a custom query on the episodes that caches with the internal episodes map.
     * @param selection The fields in the where clause.
     * @param selectionArgs The values of the fields in the where clause.
     * @return A cached list of episodes that match the description.
     */
    List<Episode> getEpisodesWhere(String selection,
                                   String[] selectionArgs);

    void addEpisodeChangedListener(EpisodeDAO.OnEpisodeChangeListener listener);

    void removeEpisodeChangedListener(EpisodeDAO.OnEpisodeChangeListener listener);

}
