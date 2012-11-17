
package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * provides database access for podcasts.
 */
public interface PodcastDBAssistant {

    /**
     * delivers all podcasts which are stored in the database.
     * 
     * @param context: the application context eg.
     *            Activity.getApplicationContext());
     * @return returns a list of all podcasts
     */
    List<Podcast> getAllPodcasts(Context context);

    /**
     * Saves the subscriptionchanges into the database.
     * 
     * @param context: the application context
     */
    void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes);
}
