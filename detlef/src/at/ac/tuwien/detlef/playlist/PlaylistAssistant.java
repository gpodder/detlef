
package at.ac.tuwien.detlef.playlist;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * This class manages the user's episode playlist. It provides methods for
 * adding, removing and rearraning a playlist and keeps the results in a hidden
 * setting.
 * 
 * @author johannes
 */
public class PlaylistAssistant {

    private ArrayList<Episode> pl = new ArrayList<Episode>();

    public void loadEpisodes() {
        pl.clear();
    }

    /**
     * Returns the episodes currently in the playlist. Call loadEpisodes first!
     * 
     * @return The episodes in the user's playlist.
     */
    public List<Episode> getEpisodes() {
        // TODO @Joshi
        return null;
    }

    /**
     * Gets the episode at the given position in the playlist.
     * 
     * @param position The position of the episode to get.
     * @return The specified episode, if the argument is in the range. Otherwise
     *         null.
     */
    public Episode getEpisodeAtPosition(int position) {
        // TODO @Joshi
        return null;
    }

    /**
     * Adds an episode to the end of this playlist.
     * 
     * @param episode The episode to be added.
     */
    public void addEpisode(Episode episode) {
        // TODO @Joshi
    }

    /**
     * Removes the given episode from this playlist.
     * 
     * @param episode The episode to be removed
     */
    public void removeEpisode(Episode episode) {
        // TODO @Joshi
    }

    /**
     * Moves this playlist's 'from'th item to position 'to'.
     * 
     * @param from The position from which to take the episode.
     * @param to The position to which to move the episode.
     */
    public void move(int from, int to) {
        // TODO @Joshi
    }

}
