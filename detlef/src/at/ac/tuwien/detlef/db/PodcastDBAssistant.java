
package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Podcast;

/*
 * provides database access for podcasts
 */
public interface PodcastDBAssistant {

    /*
     * delivers all podcasts which are stored in the database
     * @param context: the application context eg.
     * Activity.getApplicationContext());
     * @return returns a list of all podcasts
     */
    public List<Podcast> getAllPodcasts(Context context);

    /*
     * ??
     */
    public void applySubscriptionChanges(Context context);
}
