
package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.feeds.IFeed;

/**
 * provides database access for episodes
 */
public interface EpisodeDBAssistant {

    /**
     * Delivers all episodes from one podcast
     *
     * @param context: The application context eg.
     *            Activity.getApplicationContext());
     * @param podcast: The podcast which contains the episodes
     * @return returns all episodes of the given podcast
     */
    public List<Episode> getEpisodes(Context context, Podcast podcast);

    /**
     * Delivers all episodes
     *
     * @param context: The application context eg.
     *            Activity.getApplicationContext());
     * @return returns all episodes which are stored in the database
     */
    public List<Episode> getAllEpisodes(Context context);

    /**
     * ??
     */
    public void applyActionChanges(Context context, Podcast podcast);

    /**
     * ??
     */
    public void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed);

}
