/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */

package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
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
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();
        return dao.getEpisodes(podcast);
    }

    @Override
    public List<Episode> getAllEpisodes(Context context) {
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();
        return dao.getAllEpisodes();
    }

    @Override
    public void applyActionChanges(Context context, EpisodeActionChanges changes) {
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();
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
                    /* Episode uses milliseconds. */
                    ep.setPlayPosition(action.position * 1000);
                    if (dao.update(ep) != 1) {
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
                int ret = dao.update(ep);
                Log.i(TAG, "asdf: " + ret);
            }
        }

    }

    @Override
    public void upsertAndDeleteEpisodes(Context context, Podcast p, IFeed feed) {
        try {
            EpisodeDAO dao = Singletons.i().getEpisodeDAO();
            for (IEpisode ep : feed.getEpisodes()) {
                try {
                    if (ep.getEnclosure() != null) {
                        Episode newEp = new Episode(ep, p);

                        dao.insertEpisode(newEp);
                    }
                } catch (Exception ex) {
                    Log.i(TAG, ("enclosure missing, " + ex.getMessage()) != null ? ex.getMessage()
                          : ex.toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public Episode getEpisodeById(Context context, int id) {
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();
        return dao.getEpisode(id);
    }

    @Override
    public void toggleEpisodeReadState(Episode episode) {
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();
        switch (episode.getActionState()) {
        case DOWNLOAD: // fall-through
        case NEW: // fall-through
        case PLAY:
            episode.setActionState(ActionState.DELETE);
            dao.update(episode);
            break;
        case DELETE:
            episode.setActionState(ActionState.NEW);
            dao.update(episode);
            break;
        default:
            Log.e(TAG, "Unknown action state encountered");
        }
    }

}
