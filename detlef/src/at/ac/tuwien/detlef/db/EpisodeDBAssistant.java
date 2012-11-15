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
     * @param context
     *            : The application context eg.
     *            Activity.getApplicationContext());
     * 
     * @param podcast
     *            : The podcast which contains the episodes
     * 
     * @return returns all episodes of the given podcast
     */
    List<Episode> getEpisodes(Context context, Podcast podcast);

    /**
     * Delivers all episodes.
     * 
     * @param context
     *            : The application context eg.
     *            Activity.getApplicationContext());
     * 
     * @return returns all episodes which are stored in the database
     */
    List<Episode> getAllEpisodes(Context context);

    /**
     * applys all changes in the EpisodeActionChanges of the podcast to the database.
     * 
     * @param context: the application context
     * 
     * @param podcast: the podcast
     * 
     * @param changes: the changes made in an episode
     */
    void applyActionChanges(Context context, Podcast podcast, EpisodeActionChanges changes);

    /**
     * ??
     */
    void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed);

    /**
     * Gets an episode by database id.
     * 
     * @param context
     *            : The application context eg.
     *            Activity.getApplicationContext());
     * @param id
     *            The ID of the episode in the database.
     * @return The episode with the given ID, if any. Otherwise null.
     */
    Episode getEpisodeById(Context context, int id);
}
