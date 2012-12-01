
package at.ac.tuwien.detlef.db;

import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * DAO for podcast access.
 */
public interface PlaylistDAO {

    /**
     * Adds an episode to the end of the playlist.
     * 
     * @param podcast The episode to be added
     */
    boolean addEpisodeToEndOfPlaylist(Episode episode);

    /**
     * Adds an episode to the beginning of the playlist.
     * 
     * @param podcast The episode to be added
     * @return True if successful, false if not.
     */
    boolean addEpisodeToBeginningOfPlaylist(Episode episode);

    /**
     * @return Gets a List of the episodes in playlist order.
     */
    List<Episode> getRawEpisodes();

    /**
     * Removes an episode from playlist.
     * 
     * @param episode The ordering number of the episode to be removed.
     * @return True if successful, false if not.
     */
    boolean removeEpisode(int position);

    /**
     * Moves an episode to a different position in the playlist.
     * 
     * @param firstPosition The position of the episode to be moved.
     * @param secondPosition The position where the episode should be moved
     *            into.
     * @return True if successful, false if not.
     */
    boolean moveEpisode(int firstPosition, int secondPosition);
}
