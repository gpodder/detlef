package at.ac.tuwien.detlef.models;

import java.util.ArrayList;
import java.util.List;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * PodListModel consists of a list of podcasts and subscribed
 * listeners which are notified on every change to the podcast list.
 *
 * This interface will probably need to change once it becomes clearer
 * how backend classes work.
 */
public class PodListModel<T extends IPodcast> {

    private final List<T> podlist;
    private final List<PodListChangeListener> listeners = new ArrayList<PodListChangeListener>();

    public PodListModel(List<T> podlist) {
        this.podlist = podlist;
    }

    public T get(int position) {
        return podlist.get(position);
    }

    public void addPodcast(T podcast) {
        podlist.add(podcast);
        notifyListeners();
    }

    public void removePodcast(IPodcast podcast) {
        podlist.remove(podcast);
        notifyListeners();
    }

    public interface PodListChangeListener {
        void onPodListChange();
    }

    public void addPodListChangeListener(PodListChangeListener listener) {
        listeners.add(listener);
    }

    public void removePodListChangeListener(PodListChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (PodListChangeListener listener : listeners) {
            listener.onPodListChange();
        }
    }
}
