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
import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.State;
import at.ac.tuwien.detlef.domain.Podcast;

public final class EpisodeDAOImpl implements EpisodeDAO {

    private static final String TAG = EpisodeDAOImpl.class.getName();

    private static EpisodeDAOImpl instance = null;

    private final DatabaseHelper dbHelper;

    private final PodcastDAOImpl podcastDAO;

    private final Set<OnEpisodeChangeListener> listeners =
            new HashSet<OnEpisodeChangeListener>();

    private final HashMap<Long, Episode> hashMapEpisode =
            new HashMap<Long, Episode>();

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
        dbHelper = new DatabaseHelper(context);
        podcastDAO = PodcastDAOImpl.i(context);

        /* Take care of any pending database upgrades. */

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();
    }

    /**
     * @see EpisodeDAO#insertEpisode(Episode)
     */
    @Override
    public Episode insertEpisode(Episode episode) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
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
                db.close();
                return null;
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
            if (episode.getState() == null) {
                values.putNull(DatabaseHelper.COLUMN_EPISODE_STATE);
            } else {
                values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode
                        .getState().toString());
            }
            long id = db.insert(DatabaseHelper.TABLE_EPISODE, null, values);
            db.close();
            if (id == -1) {
                // error occurred
                return null;
            }
            episode.setId(id);
            hashMapEpisode.put(id, episode);
            notifyListenersAdded(episode);
            return episode;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
    }

    /**
     * @see EpisodeDAO#deleteEpisode(Episode)
     */
    @Override
    public int deleteEpisode(Episode episode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(episode.getId()) };

        int ret =
                db.delete(DatabaseHelper.TABLE_EPISODE, selection,
                        selectionArgs);
        db.close();

        notifyListenersDeleted(episode);
        if (hashMapEpisode.containsKey(episode.getId())) {
            hashMapEpisode.remove(episode.getId());
        }

        return ret;
    }

    /**
     * @see EpisodeDAO#getAllEpisodes()
     */
    @Override
    public List<Episode> getAllEpisodes() {
        return getEpisodesWhere(null, null);
    }

    @Override
    public Episode getEpisode(int id) {
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        List<Episode> ret = getEpisodesWhere(selection, selectionArgs);
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
        return getEpisodesWhere(selection, selectionArgs);
    }

    private List<Episode> getEpisodesWhere(String selection,
            String[] selectionArgs) {
        List<Episode> allEpisodes = new ArrayList<Episode>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection =
            { DatabaseHelper.COLUMN_EPISODE_AUTHOR,
                DatabaseHelper.COLUMN_EPISODE_DESCRIPTION,
                DatabaseHelper.COLUMN_EPISODE_FILESIZE,
                DatabaseHelper.COLUMN_EPISODE_GUID,
                DatabaseHelper.COLUMN_EPISODE_ID,
                DatabaseHelper.COLUMN_EPISODE_LINK,
                DatabaseHelper.COLUMN_EPISODE_MIMETYPE,
                DatabaseHelper.COLUMN_EPISODE_PODCAST,
                DatabaseHelper.COLUMN_EPISODE_RELEASED,
                DatabaseHelper.COLUMN_EPISODE_TITLE,
                DatabaseHelper.COLUMN_EPISODE_URL,
                DatabaseHelper.COLUMN_EPISODE_FILEPATH,
                DatabaseHelper.COLUMN_EPISODE_STATE };

        Cursor c =
                db.query(DatabaseHelper.TABLE_EPISODE, projection, selection, // columns
                        // for
                        // where
                        // clause
                        selectionArgs, // values for where clause
                        null, // group
                        null, // filter by row group
                        null // sort order
                        );

        if (c.moveToFirst()) {
            do {
                Episode e = getEpisode(c);
                allEpisodes.add(e);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return allEpisodes;
    }

    private Episode getEpisode(Cursor c) {
        long key = c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_ID));
        if (hashMapEpisode.containsKey(key)) {
            return hashMapEpisode.get(key);
        };

        Episode e = new Episode(podcastDAO.getPodcastById(c.getLong(
                c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_PODCAST))));
        e.setAuthor(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_AUTHOR)));
        e.setDescription(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_DESCRIPTION)));
        e.setFileSize(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_FILESIZE)));
        e.setGuid(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_GUID)));
        e.setId(key);
        e.setLink(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_LINK)));
        e.setMimetype(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_MIMETYPE)));
        e.setReleased(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_RELEASED)));
        e.setTitle(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_TITLE)));
        e.setUrl(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_URL)));
        e.setFilePath(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_FILEPATH)));
        String state = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_STATE));
        if (state != null) {
            e.setState(State.valueOf(state));
        }

        hashMapEpisode.put(key, e);

        return e;
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
        values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode.getState()
                .toString());
        return updateFieldUsingEpisodeId(episode, values);
    }

    private int
    updateFieldUsingEpisodeId(Episode episode, ContentValues values) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
            String[] selectionArgs = { String.valueOf(episode.getId()) };

            int ret =
                    db.update(DatabaseHelper.TABLE_EPISODE, values, selection,
                            selectionArgs);
            db.close();

            notifyListenersChanged(episode);

            return ret;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return 0;
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
        String selection =
                DatabaseHelper.COLUMN_EPISODE_URL + " = ? OR "
                        + DatabaseHelper.COLUMN_EPISODE_GUID + " = ?";
        String[] selectionArgs = { url, guid };
        List<Episode> episodes = getEpisodesWhere(selection, selectionArgs);
        if (episodes.size() > 0) {
            return episodes.get(0);
        }
        return null;
    }

}
