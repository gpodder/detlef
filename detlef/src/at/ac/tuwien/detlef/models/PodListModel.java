package at.ac.tuwien.detlef.models;

import java.util.List;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * PodListModel consists of a list of podcasts.
 */
public class PodListModel<T extends IPodcast> {

    private final List<T> podlist;

    public PodListModel(List<T> podlist) {
        this.podlist = podlist;
    }

    public T get(int position) {
        return podlist.get(position);
    }

    public void addPodcast(T podcast) {
        podlist.add(podcast);
    }

    public void removePodcast(IPodcast podcast) {
        podlist.remove(podcast);
    }
}
