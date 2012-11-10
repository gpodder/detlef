
package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import at.ac.tuwien.detlef.domain.Podcast;

public final class PodcastDAOImpl implements PodcastDAO {

    private static PodcastDAOImpl instance = null;

    private final DatabaseHelper dbHelper;
    private final Set<OnPodcastChangeListener> listeners = new HashSet<OnPodcastChangeListener>();

    /**
     * Interface for listeners interested in podcast status changes.
     */
    public interface OnPodcastChangeListener {
        void onPodcastChanged(Podcast podcast);
        void onPodcastAdded(Podcast podcast);
        void onPodcastDeleted(Podcast podcast);
    }

    /**
     * Returns (and lazily initializes) the PodcastDAOImpl singleton instance.
     */
    public static PodcastDAOImpl i(Context context) {
        if (instance == null) {
            instance = new PodcastDAOImpl(context);
        }
        return instance;
    }

    private PodcastDAOImpl(Context context) {
        dbHelper = new DatabaseHelper(context);

        /* Take care of any pending database upgrades. */

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();
    }

    /**
     *
     * @see at.ac.tuwien.detlef.db.PodcastDAO#insertPodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    public long insertPodcast(Podcast podcast) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PODCAST_DESCRIPTION, podcast.getDescription());
        values.put(DatabaseHelper.COLUMN_PODCAST_URL, podcast.getUrl());
        values.put(DatabaseHelper.COLUMN_PODCAST_TITLE, podcast.getTitle());
        values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_URL, podcast.getLogoUrl());
        values.put(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE, podcast.getLastUpdate());
        values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH, podcast.getLogoFilePath());

        long id = db.insert(DatabaseHelper.TABLE_PODCAST, null, values);
        db.close();

        notifyListenersAdded(podcast);

        return id;

    }

    /**
     * 
     * @see at.ac.tuwien.detlef.db.PodcastDAO#deletePodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    public int deletePodcast(Podcast podcast) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcast.getId())
        };

        int ret = db.delete(DatabaseHelper.TABLE_PODCAST, selection, selectionArgs);
        db.close();

        notifyListenersDeleted(podcast);

        return ret;
    }

    /**
     *
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getAllPodcasts()
     */
    public List<Podcast> getAllPodcasts() {
        List<Podcast> allPodcasts = new ArrayList<Podcast>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
                DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
                DatabaseHelper.COLUMN_PODCAST_LOGO_URL, DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
                DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH
        };

        Cursor c = db.query(DatabaseHelper.TABLE_PODCAST, projection,
                null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                Podcast p = getPodcast(c);
                allPodcasts.add(p);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return allPodcasts;
    }

    private Podcast getPodcast(Cursor c) {
        Podcast p = new Podcast();
        p.setId(c.getLong(0));
        p.setUrl(c.getString(1));
        p.setTitle(c.getString(2));
        p.setDescription(c.getString(3));
        p.setLogoUrl(c.getString(4));
        p.setLastUpdate(c.getLong(5));
        p.setLogoFilePath(c.getString(6));
        return p;
    }

    /**
     * 
     * @see at.ac.tuwien.detlef.db.PodcastDAO#updateLastUpdate(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    public int updateLastUpdate(Podcast podcast) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE, podcast.getLastUpdate());

        String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcast.getId())
        };

        int ret = db.update(DatabaseHelper.TABLE_PODCAST, values, selection, selectionArgs);
        db.close();

        notifyListenersChanged(podcast);

        return ret;
    }

    /**
     * 
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getPodcastById(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    public Podcast getPodcastById(long podcastId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
                DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
                DatabaseHelper.COLUMN_PODCAST_LOGO_URL, DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
                DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH
        };

        String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcastId)
        };

        Cursor c = db.query(DatabaseHelper.TABLE_PODCAST,
                projection,
                selection,
                selectionArgs,
                null, null, null);

        Podcast p = null;
        if (c.moveToFirst()) {
            do {
                p = getPodcast(c);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return p;
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#updateLogoFilePath(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    public int updateLogoFilePath(Podcast podcast) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH, podcast.getLogoFilePath());

        String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcast.getId())
        };

        int ret = db.update(DatabaseHelper.TABLE_PODCAST, values, selection, selectionArgs);
        db.close();

        notifyListenersChanged(podcast);

        return ret;
    }

    public void addPodcastChangedListener(OnPodcastChangeListener listener) {
        listeners.add(listener);
    }

    public void removePodListChangeListener(OnPodcastChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersChanged(Podcast podcast) {
        for (OnPodcastChangeListener listener : listeners) {
            listener.onPodcastChanged(podcast);
        }
    }

    private void notifyListenersAdded(Podcast podcast) {
        for (OnPodcastChangeListener listener : listeners) {
            listener.onPodcastAdded(podcast);
        }
    }

    private void notifyListenersDeleted(Podcast podcast) {
        for (OnPodcastChangeListener listener : listeners) {
            listener.onPodcastDeleted(podcast);
        }
    }
}
