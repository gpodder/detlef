package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;

/**
 * provides database access for podcasts
 */
public interface PodcastDBAssistant {

    /**
     * delivers all podcasts which are stored in the database
     *
     * @param context: the application context eg.
     *            Activity.getApplicationContext());
     *
     * @return returns a list of all podcasts
     */
    List<Podcast> getAllPodcasts(Context context);

    /**
     * ??
     */
    void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes);
}
