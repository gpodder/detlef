package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.api.EpisodeAction;
import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.feeds.IFeed;
import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

public class EpisodeDBAssistantImpl implements EpisodeDBAssistant {

    private static final String TAG = EpisodeDBAssistantImpl.class.getName();

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
    public void applyActionChanges(Context context, Podcast podcast,
            EpisodeActionChanges changes) {
        EpisodeDAO dao = EpisodeDAOImpl.i(context);
        for (EpisodeAction action : changes.actions) {
            // update playposition 
            Episode ep = dao.getEpisodeByUrlOrGuid(action.episode, action.episode);
            if (ep != null) {
                ActionState newActionState = ActionState.NEW;
                if (action.action.equals("play")) {
                    newActionState = ActionState.PLAY;
                    Log.i(TAG, "updating play position from: " + action.episode + " pos: " 
                            + action.position + " started:" + action.started + " total: " 
                            + action.total);
                    ep.setPlayPosition(action.position);
                    if (dao.updateState(ep) != 1) {
                        Log.w(TAG, "update play position went wrong: " + ep.getLink());
                    }
                    
                } else {
                    if (action.action.equals("download")) {
                        newActionState = ActionState.DOWNLOAD;
                    } else {
                        if (action.action.equals("delete")) {
                            newActionState = ActionState.DELETE;
                        }
                    }
                }
                ep.setActionState(newActionState);
                int ret = dao.updateActionState(ep);
                Log.i(TAG, "asdf: " + ret);
            }
        }

    }

    @Override
    public void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed) {
        try {
            EpisodeDAO dao = EpisodeDAOImpl.i(context);
            for (IEpisode ep : feed.getEpisodes()) {
                try {
                    if (ep.getEnclosure() != null) {
                        Episode newEp = new Episode(ep, p);
                        dao.insertEpisode(newEp);
                    }
                } catch (Exception ex) {
                    Log.i(TAG, "enclosure missing, " +  ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public Episode getEpisodeById(Context context, int id) {
        EpisodeDAO dao = EpisodeDAOImpl.i(context);
        return dao.getEpisode(id);
    }

}
