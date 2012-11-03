
package at.ac.tuwien.detlef.db;

import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

/*
 * DAO for episode access
 */
public interface EpisodeDAO {

    /*
     * inserts an episode into the database
     * @param episode: the episode which should be saved into the database
     * @return returns the number of inserted rows (if no one is inserted 0 will
     * be returned)
     */
    public abstract long insertEpisode(Episode episode);

    /*
     * deletes an episode from the database
     * @param episode: the episode which should be deleted (the id will be used
     * for this)
     * @return returns the number of deleted episodes (if no one is deleted 0
     * will be returned)
     */
    public abstract int deleteEpisode(Episode episode);

    /*
     * delivers all episodes which are stored in the database
     * @return returns a list of all episodes
     */
    public abstract List<Episode> getAllEpisodes();

    /*
     * delivers all episodes from a given podcast
     * @param podcast: the podcast which contains the episodes
     * @return returns a list of episodes which belongs to the given podcast
     */
    public abstract List<Episode> getEpisodes(Podcast podcast);

}
