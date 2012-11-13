package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;

public class PodcastDBAssistantImpl implements PodcastDBAssistant {
    /** Logging tag. */
    private static final String TAG = "PodcastDBAssistant";

    public List<Podcast> getAllPodcasts(Context context) {
        PodcastDAO dao = PodcastDAOImpl.i(context);
        return dao.getAllPodcasts();
    }

    public void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes) {
        // TODO Auto-generated method stub
        Log.d(TAG, "applying changes");
    }

}
