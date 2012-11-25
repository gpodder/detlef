package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

public final class EpisodeDAOImpl implements EpisodeDAO {

    private static final String TAG = EpisodeDAOImpl.class.getName();

    private static EpisodeDAOImpl instance = null;

    private final DatabaseHelper dbHelper;
    private final PodcastDAOImpl podcastDAO;
    private final Set<OnEpisodeChangeListener> listeners = new HashSet<OnEpisodeChangeListener>();
    private final HashMap<Long, Episode> hashMapEpisode = new HashMap<Long, Episode>();
    
    private EpisodeDAOCore episodeDAOCore;
    
    
    /**
     * Interface for listeners interested in episode status changes.
     */
    public interface OnEpisodeChangeListener {
        void onEpisodeChanged(Episode episode);
        void onEpisodeAdded(Episode episode);
        void onEpisodeDeleted(Episode episode);
    }

    /**
     * Returns (and lazily initializes) the EpisodeDAOImpl singleton instance.
     */
    public static EpisodeDAOImpl i(Context context) {
        if (instance == null) {
            instance = new EpisodeDAOImpl(context);
        }
        return instance;
    }

    public EpisodeDAOImpl(Context context) {
        synchronized (DatabaseHelper.bigFrigginLock) {
            dbHelper = new DatabaseHelper(context);
            podcastDAO = PodcastDAOImpl.i(context);
            
            episodeDAOCore = new EpisodeDAOCore(context, podcastDAO);
            
            /* Take care of any pending database upgrades. */

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
        }
    }

    /**
     * @see EpisodeDAO#insertEpisode(Episode)
     */
    @Override
    public Episode insertEpisode(Episode episode) {
        synchronized (DatabaseHelper.bigFrigginLock) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_EPISODE_AUTHOR,
                        episode.getAuthor());
                values.put(DatabaseHelper.COLUMN_EPISODE_DESCRIPTION,
                        episode.getDescription());
                values.put(DatabaseHelper.COLUMN_EPISODE_FILESIZE,
                        episode.getFileSize());
                values.put(DatabaseHelper.COLUMN_EPISODE_GUID, episode.getGuid());
                values.put(DatabaseHelper.COLUMN_EPISODE_LINK, episode.getLink());
                values.put(DatabaseHelper.COLUMN_EPISODE_MIMETYPE,
                        episode.getMimetype());
                if (episode.getPodcast() == null) {
                    throw new IllegalArgumentException("The episode must belong to a podcast");
                }
                values.put(DatabaseHelper.COLUMN_EPISODE_PODCAST, episode
                        .getPodcast().getId());
                values.put(DatabaseHelper.COLUMN_EPISODE_RELEASED,
                        episode.getReleased());
                values.put(DatabaseHelper.COLUMN_EPISODE_TITLE, episode.getTitle());
                values.put(DatabaseHelper.COLUMN_EPISODE_URL, episode.getUrl());
                if (episode.getFilePath() == null) {
                    values.putNull(DatabaseHelper.COLUMN_EPISODE_FILEPATH);
                } else {
                    values.put(DatabaseHelper.COLUMN_EPISODE_FILEPATH,
                            episode.getFilePath());
                }
                if (episode.getStorageState() == null) {
                    values.putNull(DatabaseHelper.COLUMN_EPISODE_STATE);
                } else {
                    values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode
                            .getStorageState().toString());
                }

                long id = db.insert(DatabaseHelper.TABLE_EPISODE, null, values);
                if (id == -1) {
                    throw new SQLiteException("Episode insert failed");
                }

                episode.setId(id);
                hashMapEpisode.put(id, episode);
                notifyListenersAdded(episode);

                return episode;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return null;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * @see EpisodeDAO#deleteEpisode(Episode)
     */
    @Override
    public int deleteEpisode(Episode episode) {
        synchronized (DatabaseHelper.bigFrigginLock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
            String[] selectionArgs = { String.valueOf(episode.getId()) };

            int ret = db.delete(DatabaseHelper.TABLE_EPISODE, selection, selectionArgs);
            db.close();

            notifyListenersDeleted(episode);
            if (hashMapEpisode.containsKey(episode.getId())) {
                hashMapEpisode.remove(episode.getId());
            }

            return ret;
        }
    }

    /**
     * @see EpisodeDAO#getAllEpisodes()
     */
    @Override
    public List<Episode> getAllEpisodes() {
        return episodeDAOCore.getEpisodesWhere(null, null);
    }

    @Override
    public Episode getEpisode(long id) {
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        List<Episode> ret = episodeDAOCore.getEpisodesWhere(selection, selectionArgs);
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    /**
     * @see EpisodeDAO#getEpisodes(Podcast)
     */
    @Override
    public List<Episode> getEpisodes(Podcast podcast) {
        String selection = DatabaseHelper.COLUMN_EPISODE_PODCAST + " = ?";
        String[] selectionArgs = { String.valueOf(podcast.getId()) };
        return episodeDAOCore.getEpisodesWhere(selection, selectionArgs);
    }

    /**
     * @see EpisodeDAO#updateFilePath(Episode)
     */
    @Override
    public int updateFilePath(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EPISODE_FILEPATH,
                episode.getFilePath());
        return updateFieldUsingEpisodeId(episode, values);
    }

    /**
     * @see EpisodeDAO#updateState(Episode)
     */
    @Override
    public int updateState(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode.getStorageState()
                .toString());
        return updateFieldUsingEpisodeId(episode, values);
    }

    private int
    updateFieldUsingEpisodeId(Episode episode, ContentValues values) {
        synchronized (DatabaseHelper.bigFrigginLock) {
            try {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
                String[] selectionArgs = { String.valueOf(episode.getId()) };

                int ret = db.update(DatabaseHelper.TABLE_EPISODE, values, selection, selectionArgs);
                db.close();

                notifyListenersChanged(episode);

                return ret;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return 0;
            }
        }
    }

    public void addEpisodeChangedListener(OnEpisodeChangeListener listener) {
        listeners.add(listener);
    }

    public void removeEpisodeChangedListener(OnEpisodeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersChanged(Episode episode) {
        for (OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeChanged(episode);
        }
    }

    private void notifyListenersAdded(Episode episode) {
        for (OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeAdded(episode);
        }
    }

    private void notifyListenersDeleted(Episode episode) {
        for (OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeDeleted(episode);
        }
    }

    @Override
    public Episode getEpisodeByUrlOrGuid(String url, String guid) {
        String selection = DatabaseHelper.COLUMN_EPISODE_URL + " = ? OR "
                + DatabaseHelper.COLUMN_EPISODE_GUID + " = ?";
        String[] selectionArgs = { url, guid };
        List<Episode> episodes = episodeDAOCore.getEpisodesWhere(selection, selectionArgs);
        if (episodes.size() > 0) {
            return episodes.get(0);
        }
        return null;
    }

}
