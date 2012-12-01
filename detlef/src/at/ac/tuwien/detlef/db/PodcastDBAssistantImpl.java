
package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodcastDBAssistantImpl implements PodcastDBAssistant {
    /** Logging tag. */
    private static final String TAG = "PodcastDBAssistant";

    @Override
    public List<Podcast> getAllPodcasts(Context context) {
        PodcastDAO dao = PodcastDAOImpl.i();
        return dao.getAllPodcasts();
    }

    @Override
    public void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes) {
        Log.d(TAG, "applying changes");
        PodcastDAO dao = PodcastDAOImpl.i();
        for (Podcast p : changes.getAdd()) {
            if ((p.getTitle() != null) && (p.getUrl() != null)) {
                dao.insertPodcast(p);
            } else {
                Log.w(TAG, "Cannot insert podcast without title/url");
            }
        }

        for (Podcast p : changes.getRemove()) {
            Podcast pod = dao.getPodcastByUrl(p.getUrl());
            if (pod != null) {
                dao.deletePodcast(pod);
            }
        }
    }

}
