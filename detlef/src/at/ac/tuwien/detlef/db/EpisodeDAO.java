
package at.ac.tuwien.detlef.db;

import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * DAO for episode access.
 */
public interface EpisodeDAO {

    /**
     * inserts an episode into the database.
     *
     * @param episode: the episode which should be saved into the database
     * @return returns the refreshed Episode, null if error occurs
     */
    Episode insertEpisode(Episode episode);

    /**
     * deletes an episode from the database.
     *
     * @param episode: the episode which should be deleted (the id will be used
     *            for this)
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
     * @param podcast: the podcast which contains the episodes
     * @return returns a list of episodes which belongs to the given podcast
     */
    List<Episode> getEpisodes(Podcast podcast);

    /**
     * updates the filePath column of the given episode (uses the filePath
     * attribute of the episode).
     *
     * @param episode the episode which should be updated
     * @return the number of updated rows
     */
    int updateFilePath(Episode episode);

    /**
     * updates the state column of the given episode (uses the state attribute
     * of the episode).
     *
     * @param episode the episode which should be updated
     * @return the number of updated rows
     */
    int updateState(Episode episode);

}
