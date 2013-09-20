package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Podcast;

public class CachingPodcastDAO implements PodcastDAO {

    private final HashMap<Long, Podcast> cache;
    private final SimplePodcastDAO dao;

    public CachingPodcastDAO(Context context) {
        dao = new SimplePodcastDAO(context);
        cache = new HashMap<Long, Podcast>();
    }

    @Override
    public Podcast insertPodcast(Podcast podcast) {
        if (dao.insertPodcast(podcast) == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(podcast.getId(), podcast);
        }

        return podcast;
    }

    @Override
    public Podcast getPodcastByUrl(String url) {
        synchronized (cache) {
            for (Podcast p : cache.values()) {
                if (p.getUrl().equals(url)) {
                    return p;
                }
            }
        }

        Podcast p = dao.getPodcastByUrl(url);
        if (p == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(p.getId(), p);
        }

        return p;
    }

    @Override
    public int deletePodcast(Podcast podcast) {
        synchronized (cache) {
            cache.remove(podcast.getId());
        }

        return dao.deletePodcast(podcast);
    }

    @Override
    public int deleteAllPodcasts() {
        synchronized (cache) {
            cache.clear();
        }

        return dao.deleteAllPodcasts();
    }

    @Override
    public List<Podcast> getAllPodcasts() {
        List<Podcast> ps = dao.getAllPodcasts();

        return cacheResults(ps);
    }

    private List<Podcast> cacheResults(List<Podcast> ps) {
        if (ps == null) {
            return null;
        }

        synchronized (cache) {
            for (Podcast p : ps) {
                if (!cache.containsKey(p.getId())) {
                    cache.put(p.getId(), p);
                }
            }

            return new ArrayList<Podcast>(cache.values());
        }
    }

    @Override
    public int update(Podcast podcast) {
        return dao.update(podcast);
    }

    @Override
    public Podcast getPodcastById(long podcastId) {
        Podcast p;
        synchronized (cache) {
            p = cache.get(podcastId);
        }

        if (p != null) {
            return p;
        }

        p = dao.getPodcastById(podcastId);
        if (p == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(p.getId(), p);
        }

        return p;
    }

    @Override
    public boolean localDeletePodcast(Podcast podcast) {
        if (!dao.localDeletePodcast(podcast)) {
            return false;
        }

        synchronized (cache) {
            cache.remove(podcast.getId());
        }

        return true;
    }

    @Override
    public boolean setRemotePodcast(Podcast podcast) {
        return dao.setRemotePodcast(podcast);
    }

    @Override
    public List<Podcast> getNonDeletedPodcasts() {
        return cacheResults(dao.getNonDeletedPodcasts());
    }

    @Override
    public List<Podcast> getLocallyAddedPodcasts() {
        return dao.getLocallyAddedPodcasts();
    }

    @Override
    public List<Podcast> getLocallyDeletedPodcasts() {
        return dao.getLocallyDeletedPodcasts();
    }

}
