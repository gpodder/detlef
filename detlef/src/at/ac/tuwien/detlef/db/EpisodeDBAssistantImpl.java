package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.feeds.IFeed;

public class EpisodeDBAssistantImpl implements EpisodeDBAssistant {

    @Override
    public List<Episode> getEpisodes(Context context, Podcast podcast) {
        EpisodeDAO dao = EpisodeDAOImpl.i(context);
        return dao.getEpisodes(podcast);
    }

    @Override
    public List<Episode> getAllEpisodes(Context context) {
        EpisodeDAO dao = EpisodeDAOImpl.i(context);
        return dao.getAllEpisodes();
    }

    @Override
    public void applyActionChanges(Context context, Podcast podcast) {
        // TODO Auto-generated method stub

    }

    @Override
    public void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed) {
        // TODO Auto-generated method stub

    }

    @Override
    public Episode getEpisodeById(Context context, int id) {
        EpisodeDAO dao = EpisodeDAOImpl.i(context);
        return dao.getEpisode(id);
    }

}
