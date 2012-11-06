package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodcastDBAssistantImpl implements PodcastDBAssistant {

    public List<Podcast> getAllPodcasts(Context context) {
        PodcastDAO dao = new PodcastDAOImpl(context);
        return dao.getAllPodcasts();
    }

    public void applySubscriptionChanges(Context context) {
        // TODO Auto-generated method stub

    }

}
