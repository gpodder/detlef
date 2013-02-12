package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class CachingEpisodeDAO implements EpisodeDAO {

    private final HashMap<Long, Episode> cache;
    private final SimpleEpisodeDAO dao;

    public CachingEpisodeDAO(Context context) {
        cache = new HashMap<Long, Episode>();
        dao = new SimpleEpisodeDAO(context);
    }

    @Override
    public Episode insertEpisode(Episode episode) {
        if (dao.insertEpisode(episode) == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(episode.getId(), episode);
        }

        return episode;
    }

    @Override
    public int deleteEpisode(Episode episode) {
        synchronized (cache) {
            cache.remove(episode.getId());
        }

        return dao.deleteEpisode(episode);
    }

    @Override
    public List<Episode> getAllEpisodes() {
        return cacheResults(dao.getAllEpisodes());
    }

    private List<Episode> cacheResults(List<Episode> es) {
        if (es == null) {
            return null;
        }

        synchronized (cache) {
            for (Episode e : es) {
                if (!cache.containsKey(e.getId())) {
                    cache.put(e.getId(), e);
                }
            }

            return new ArrayList<Episode>(cache.values());
        }
    }

    @Override
    public List<Episode> getEpisodes(Podcast podcast) {
        return cacheResults(dao.getEpisodes(podcast));
    }

    @Override
    public int update(Episode episode) {
        return dao.update(episode);
    }

    @Override
    public Episode getEpisode(long id) {
        Episode e;
        synchronized (cache) {
            e = cache.get(id);
        }

        if (e != null) {
            return e;
        }

        e = dao.getEpisode(id);
        if (e == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(e.getId(), e);
        }

        return e;
    }

    @Override
    public Episode getEpisodeByUrlOrGuid(String url, String guid) {
        synchronized (cache) {
            for (Episode e : cache.values()) {
                String otherUrl = e.getUrl();
                if (otherUrl != null && otherUrl.equals(url)) {
                    return e;
                }

                String otherGuid = e.getGuid();
                if (otherGuid != null && otherGuid.equals(guid)) {
                    return e;
                }
            }
        }

        Episode e = dao.getEpisodeByUrlOrGuid(url, guid);
        if (e == null) {
            return null;
        }

        synchronized (cache) {
            cache.put(e.getId(), e);
        }

        return e;
    }

    @Override
    public void addEpisodeChangedListener(OnEpisodeChangeListener listener) {
        dao.addEpisodeChangedListener(listener);
    }

    @Override
    public void removeEpisodeChangedListener(OnEpisodeChangeListener listener) {
        dao.removeEpisodeChangedListener(listener);
    }

}
