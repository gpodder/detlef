package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.feeds.IFeed;

public class EpisodeDBAssistantImpl implements EpisodeDBAssistant {

    public List<Episode> getEpisodes(Context context, Podcast podcast) {
        EpisodeDAO dao = new EpisodeDAOImpl(context);
        return dao.getEpisodes(podcast);
    }

    public List<Episode> getAllEpisodes(Context context) {
        EpisodeDAO dao = new EpisodeDAOImpl(context);
        return dao.getAllEpisodes();
    }

    public void applyActionChanges(Context context, Podcast podcast) {
        // TODO Auto-generated method stub

    }

    public void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed) {
        // TODO Auto-generated method stub

    }

}
