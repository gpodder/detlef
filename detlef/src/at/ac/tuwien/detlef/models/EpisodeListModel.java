package at.ac.tuwien.detlef.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

/**
 * EpisodeListModel consists of a list of episodes and subscribed listeners which
 * are notified on every change to the episodes list.
 *
 * This interface will probably need to change once it becomes clearer how
 * backend classes work.
 */
public class EpisodeListModel {

    /** Contains all known episodes. */
    private final List<Episode> eplist;

    /** The list of all listeners. */
    private final List<EpisodeListChangeListener> listeners =
            new ArrayList<EpisodeListChangeListener>();

    public EpisodeListModel(List<Episode> eplist) {
        this.eplist = eplist;
    }

    public Episode get(int position) {
        return eplist.get(position);
    }

    public List<Episode> getAll() {
        return Collections.unmodifiableList(eplist);
    }

    public List<Episode> getByPodcast(Podcast podcast) {
        List<Episode> filtered = new ArrayList<Episode>(eplist);

        for (int i = filtered.size() - 1; i >= 0; i--) {
            Episode e = filtered.get(i);
            Podcast p = e.getPodcast();
            if (p == null || p.getId() != podcast.getId()) {
                filtered.remove(i);
            }
        }

        return filtered;
    }

    public void addEpisode(Episode episode) {
        eplist.add(episode);
        notifyListeners();
    }

    public void removeEpisode(IEpisode episode) {
        eplist.remove(episode);
        notifyListeners();
    }

    public interface EpisodeListChangeListener {
        void onEpisodeListChange();
    }

    public void addPodListChangeListener(EpisodeListChangeListener listener) {
        listeners.add(listener);
    }

    public void removePodListChangeListener(EpisodeListChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (EpisodeListChangeListener listener : listeners) {
            listener.onEpisodeListChange();
        }
    }
}
