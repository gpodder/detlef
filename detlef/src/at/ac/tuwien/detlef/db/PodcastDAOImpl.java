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
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public final class PodcastDAOImpl implements PodcastDAO {

    private static final String TAG = PodcastDAOImpl.class.getName();

    private static PodcastDAOImpl instance = new PodcastDAOImpl(Detlef.getAppContext());

    private final DatabaseHelper dbHelper;
    private final Set<PodcastDAO.OnPodcastChangeListener> listeners =
            new HashSet<PodcastDAO.OnPodcastChangeListener>();
    private final HashMap<Long, Podcast> hashMapPodcast = new HashMap<Long, Podcast>();

    /**
     * Returns the PodcastDAOImpl singleton instance.
     */
    public static PodcastDAOImpl i() {
        return instance;
    }

    public PodcastDAOImpl(Context context) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            dbHelper = new DatabaseHelper(context);

            /* Take care of any pending database upgrades. */

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
        }
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#insertPodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    @Override
    public Podcast insertPodcast(Podcast podcast) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PODCAST_DESCRIPTION, podcast.getDescription());
                values.put(DatabaseHelper.COLUMN_PODCAST_URL, podcast.getUrl());
                values.put(DatabaseHelper.COLUMN_PODCAST_TITLE, podcast.getTitle());
                if (podcast.getLogoUrl() == null) {
                    values.putNull(DatabaseHelper.COLUMN_PODCAST_LOGO_URL);
                } else {
                    values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_URL, podcast.getLogoUrl());
                }
                values.put(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE, podcast.getLastUpdate());
                if (podcast.getLogoFilePath() == null) {
                    values.putNull(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH);
                } else {
                    values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH,
                            podcast.getLogoFilePath());
                }

                long id = db.insert(DatabaseHelper.TABLE_PODCAST, null, values);
                if (id == -1) {
                    throw new SQLiteException("Failed to insert podcast");
                }

                podcast.setId(id);
                hashMapPodcast.put(id, podcast);
                notifyListenersAdded(podcast);

                return podcast;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
                return null;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#deletePodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    @Override
    public int deletePodcast(Podcast podcast) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            int ret = 0;
            SQLiteDatabase db = null;
            try {
                // delete podcasts manually because of refreshing
                // the episodeListFragment
                EpisodeDAOImpl epDao = EpisodeDAOImpl.i();
                List<Episode> epList = epDao.getEpisodes(podcast);
                for (Episode ep : epList) {
                    epDao.deleteEpisode(ep);
                }

                db = dbHelper.getWritableDatabase();
                String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
                String[] selectionArgs = {
                        String.valueOf(podcast.getId())
                };

                ret = db.delete(DatabaseHelper.TABLE_PODCAST, selection, selectionArgs);
                db.close();

                notifyListenersDeleted(podcast);
                if (hashMapPodcast.containsKey(podcast.getId())) {
                    hashMapPodcast.remove(podcast.getId());
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
            return ret;
        }
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getAllPodcasts()
     */
    @Override
    public List<Podcast> getAllPodcasts() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            List<Podcast> allPodcasts = new ArrayList<Podcast>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {
                    DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
                    DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_URL,
                    DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH
            };

            Cursor c = db.query(DatabaseHelper.TABLE_PODCAST, projection, null, null, null, null,
                    null);

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
    }

    private Podcast getPodcast(Cursor c) {
        long key = c.getLong(0);

        if (hashMapPodcast.containsKey(key)) {
            return hashMapPodcast.get(key);
        }

        Podcast p = new Podcast();
        p.setId(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_ID)));
        p.setUrl(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_URL)));
        p.setTitle(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_TITLE)));
        p.setDescription(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_DESCRIPTION)));
        p.setLogoUrl(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LOGO_URL)));
        p.setLastUpdate(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE)));
        p.setLogoFilePath(c.getString(c
                .getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH)));

        hashMapPodcast.put(key, p);

        return p;
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#updateLastUpdate(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    @Override
    public int updateLastUpdate(Podcast podcast) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
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
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getPodcastById(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    @Override
    public Podcast getPodcastById(long podcastId) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {
                    DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
                    DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_URL,
                    DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH
            };

            String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
            String[] selectionArgs = {
                    String.valueOf(podcastId)
            };

            Cursor c = db.query(DatabaseHelper.TABLE_PODCAST, projection, selection, selectionArgs,
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
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#updateLogoFilePath(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    @Override
    public int updateLogoFilePath(Podcast podcast) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
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
    }

    public void addPodcastChangedListener(PodcastDAO.OnPodcastChangeListener listener) {
        listeners.add(listener);
    }

    public void removePodListChangeListener(PodcastDAO.OnPodcastChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersChanged(Podcast podcast) {
        for (PodcastDAO.OnPodcastChangeListener listener : listeners) {
            listener.onPodcastChanged(podcast);
        }
    }

    private void notifyListenersAdded(Podcast podcast) {
        for (PodcastDAO.OnPodcastChangeListener listener : listeners) {
            listener.onPodcastAdded(podcast);
        }
    }

    private void notifyListenersDeleted(Podcast podcast) {
        for (PodcastDAO.OnPodcastChangeListener listener : listeners) {
            listener.onPodcastDeleted(podcast);
        }
    }

    @Override
    public Podcast getPodcastByUrl(String url) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {
                    DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
                    DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_URL,
                    DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
                    DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH
            };

            String selection = DatabaseHelper.COLUMN_PODCAST_URL + " = ?";
            String[] selectionArgs = {
                    String.valueOf(url)
            };

            Cursor c = db.query(DatabaseHelper.TABLE_PODCAST, projection, selection, selectionArgs,
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
    }
}
