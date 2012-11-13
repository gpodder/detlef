package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;

import com.dragontek.mygpoclient.simple.IPodcast;

public class PodcastDBAssistantImpl implements PodcastDBAssistant {
    /** Logging tag. */
    private static final String TAG = "PodcastDBAssistant";

    public List<Podcast> getAllPodcasts(Context context) {
        PodcastDAO dao = PodcastDAOImpl.i(context);
        return dao.getAllPodcasts();
    }

    public void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes) {
        Log.d(TAG, "applying changes");
        PodcastDAO dao = PodcastDAOImpl.i(context);
        for (IPodcast p : changes.getAdd()) {
            Podcast newPod = new Podcast();
            newPod.setDescription(p.getDescription());
            newPod.setLastUpdate(changes.getTimestamp());
            newPod.setLogoUrl(p.getLogoUrl());
            newPod.setTitle(p.getTitle());
            newPod.setUrl(p.getUrl());
            dao.insertPodcast(newPod);
        }

        for (IPodcast p :changes.getRemove()) {
            Podcast pod = dao.getPodcastByUrl(p.getUrl());
            if (pod != null) {
                dao.deletePodcast(pod);
            }
        }
    }

}
