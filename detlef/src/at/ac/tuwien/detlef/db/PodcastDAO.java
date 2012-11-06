
package at.ac.tuwien.detlef.db;

import java.util.List;

import at.ac.tuwien.detlef.domain.Podcast;

/**
 * DAO for podcast access
 */
public interface PodcastDAO {

    /**
     * Inserts a podcast into the Database
     *
     * @param podcast The podcast object
     * @return returns the number of inserted rows (if it fails then <1 will be
     *         returned)
     */
    public abstract long insertPodcast(Podcast podcast);

    /**
     * Deletes a podcast from the Database
     *
     * @param podcast The podcast which should be deleted (only the id will be
     *            used for this)
     * @return returns the number of deleted rows (if it fails then <1 will be
     *         returned)
     */
    public abstract int deletePodcast(Podcast podcast);

    /**
     * Delivers all podcast from the database
     *
     * @return A list of podcast objects
     */
    public abstract List<Podcast> getAllPodcasts();

    /**
     * Updates the lastUpdate column of the given podcast (uses the lastUpdate
     * attribute of the podcast)
     *
     * @param podcast the podcast which should be updated
     * @return the number of updated rows will be returned (if it fails then <1
     *         will be returned)
     */
    public abstract int updateLastUpdate(Podcast podcast);

    /**
     * Updates the logoFilePath column of the given podcast (uses the
     * logoFilePath attribute of the podcast)
     *
     * @param podcast the podcast which should be updated
     * @return the number of updated rows will be returned
     */
    public abstract int updateLogoFilePath(Podcast podcast);

    /**
     * Delivers the podcast with the given id
     *
     * @param podcastId the id from the podcast which will be returned
     * @return returns the podcast with the given id
     */
    public abstract Podcast getPodcastById(long podcastId);
}
