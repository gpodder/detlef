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

import at.ac.tuwien.detlef.domain.Podcast;

/**
 * DAO for podcast access.
 */
public interface PodcastDAO {

    /**
     * Inserts a podcast into the Database.
     *
     * @param podcast The podcast object
     * @return the inserted Podcast, null if an error occurs
     */
    Podcast insertPodcast(Podcast podcast);

    /**
     * delivers the podcast with the given url.
     * @param url: the url from the podcast
     * @return returns the podcast object with the given url
     */
    Podcast getPodcastByUrl(String url);

    /**
     * Deletes a podcast and all episodes that are associated with this podcast from the database.
     *
     * @param podcast The podcast which should be deleted (only the id will be
     *            used for this)
     * @return returns the number of deleted rows (if it fails then &lt1 will be
     *         returned)
     */
    int deletePodcast(Podcast podcast);

    /**
     * Deletes all podcasts (and therefore also all episodes) from the local database. This is
     * useful if you want to perform a complete resync of all feeds and get rid of messed up
     * data quickly. It is important to note that all remote data stored on gpodder.net <strong>
     * will not be touched</strong> by calling this method. So it sounds more drastic than it
     * actually is, because all data can be restored by simply refreshing the feed list.
     *
     * <p>This is also useful if you change the user account and want to wipe all data with one
     * handy function.</p>
     *
     * @return The number of deleted podcasts.
     */
    int deleteAllPodcasts();

    /**
     * Delivers all podcast from the database.
     *
     * @return A list of podcast objects
     */
    List<Podcast> getAllPodcasts();

    /**
     * Updates the database entry of the given podcast.
     *
     * @param podcast the podcast which should be updated
     * @return the number of updated rows
     */
    int update(Podcast podcast);

    /**
     * Delivers the podcast with the given id.
     *
     * @param podcastId the id from the podcast which will be returned
     * @return the podcast with the given id
     */
    Podcast getPodcastById(long podcastId);

    /**
     * Deletes the podcast locally and removes all its episodes.
     *
     * If the podcast has only been added locally but was never uploaded it is completely
     * removed, otherwise it is marked as deleted in the database. Once this delete action has
     * been pushed to the gpodder service the podcast should be completely deleted (with
     * deletePodcast()).
     *
     * @param podcast The podcast to delete.
     * @return True on success, false otherwise.
     */
    boolean localDeletePodcast(Podcast podcast);

    /**
     * Removes the podcast from both the local add and the local delete list.
     *
     * @param podcast The podcast to remove from the local add and delete lists.
     * @return True on success, false otherwise.
     */
    boolean setRemotePodcast(Podcast podcast);

    /**
     * Returns all podcasts that aren't marked as deleted.
     *
     * @return A list of podcast objects.
     */
    List<Podcast> getNonDeletedPodcasts();

    /**
     * Returns all podcasts are marked as locally added.
     * This method is NOT guaranteed to return the same object
     * for the same podcast when called repeatedly.
     *
     * @return A list of podcast objects.
     */
    List<Podcast> getLocallyAddedPodcasts();

    /**
     * Returns all podcasts are marked as locally deleted.
     * This method is NOT guaranteed to return the same object
     * for the same podcast when called repeatedly.
     *
     * @return A list of podcast objects.
     */
    List<Podcast> getLocallyDeletedPodcasts();
}
