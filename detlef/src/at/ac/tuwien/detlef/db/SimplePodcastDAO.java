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
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.domain.PodcastPersistence;

public class SimplePodcastDAO implements PodcastDAO {

    private static final String TAG = SimplePodcastDAO.class.getName();

    private final DatabaseHelper dbHelper;

    public SimplePodcastDAO(Context context) {
        dbHelper = Singletons.i().getDatabaseHelper();

        /* Take care of any pending database upgrades. */

        dbHelper.getWritableDatabase();
    }

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#insertPodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    @Override
    public Podcast insertPodcast(Podcast podcast) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = toContentValues(podcast);

            db.beginTransaction();

            long id = db.insert(DatabaseHelper.TABLE_PODCAST, null, values);
            if (id == -1) {
                throw new SQLiteException("Failed to insert podcast");
            }

            if (podcast.isLocalAdd()) {
                values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PODCAST_ADD_ID, id);
                if (db.insert(DatabaseHelper.TABLE_PODCAST_LOCAL_ADD, null, values) == -1) {
                    throw new SQLiteException("Failed to insert podcast into local add table");
                }
            }

            if (podcast.isLocalDel()) {
                values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PODCAST_DEL_ID, id);
                if (db.insert(DatabaseHelper.TABLE_PODCAST_LOCAL_DEL, null, values) == -1) {
                    throw new SQLiteException("Failed to insert podcast into local del table");
                }
            }

            podcast.setId(id);

            db.setTransactionSuccessful();

            return podcast;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
            return null;
        } finally {
            if (db != null && db.isOpen()) {
                db.endTransaction();
            }
        }
    }

    private ContentValues toContentValues(Podcast podcast) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PODCAST_DESCRIPTION, podcast.getDescription());
        values.put(DatabaseHelper.COLUMN_PODCAST_URL, podcast.getUrl());
        values.put(DatabaseHelper.COLUMN_PODCAST_TITLE, podcast.getTitle());
        values.put(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE, podcast.getLastUpdate());

        if (podcast.getLogoUrl() == null) {
            values.putNull(DatabaseHelper.COLUMN_PODCAST_LOGO_URL);
        } else {
            values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_URL, podcast.getLogoUrl());
        }

        if (podcast.getLogoFilePath() == null) {
            values.putNull(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH);
        } else {
            values.put(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH,
                       podcast.getLogoFilePath());
        }

        return values;
    }

    private void deleteEpisodesForPodcast(Podcast podcast) {
        // delete podcasts manually because of refreshing
        // the episodeListFragment
        EpisodeDAO epDao = Singletons.i().getEpisodeDAO();
        List<Episode> epList = epDao.getEpisodes(podcast);
        for (Episode ep : epList) {
            epDao.deleteEpisode(ep);
        }
    }
    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#deletePodcast(at.ac.tuwien.detlef.domain
     *      .Podcast)
     */
    @Override
    public int deletePodcast(Podcast podcast) {
        PodcastPersistence.delete(podcast);

        SQLiteDatabase db = null;
        try {
            deleteEpisodesForPodcast(podcast);

            db = dbHelper.getWritableDatabase();
            String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
            String[] selectionArgs = {
                String.valueOf(podcast.getId())
            };

            int ret = db.delete(DatabaseHelper.TABLE_PODCAST, selection, selectionArgs);

            return ret;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
            return -1;
        }
    }

    private static final String QUERY_COLUMN_PODCAST_LOCAL_ADD = "lAdd";
    private static final String QUERY_COLUMN_PODCAST_LOCAL_DEL = "lDel";
    private static final String QUERY_ALL_PODCASTS = String.format("select "
            + "p.%s,"
            + "p.%s,"
            + "p.%s,"
            + "p.%s,"
            + "p.%s,"
            + "p.%s,"
            + "p.%s,"
            + "a.%s,d.%s "
            + "from %s p "
            + "left outer join (select %s as addID,'add' as %s from %s) a on %s = addID "
            + "left outer join (select %s as delID,'del' as %s from %s) d on %s = delID;",
            DatabaseHelper.COLUMN_PODCAST_ID, DatabaseHelper.COLUMN_PODCAST_URL,
            DatabaseHelper.COLUMN_PODCAST_TITLE, DatabaseHelper.COLUMN_PODCAST_DESCRIPTION,
            DatabaseHelper.COLUMN_PODCAST_LOGO_URL,
            DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE,
            DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH,
            QUERY_COLUMN_PODCAST_LOCAL_ADD, QUERY_COLUMN_PODCAST_LOCAL_DEL,
            DatabaseHelper.TABLE_PODCAST,
            DatabaseHelper.COLUMN_PODCAST_ADD_ID, QUERY_COLUMN_PODCAST_LOCAL_ADD,
            DatabaseHelper.TABLE_PODCAST_LOCAL_ADD, DatabaseHelper.COLUMN_PODCAST_ID,
            DatabaseHelper.COLUMN_PODCAST_DEL_ID, QUERY_COLUMN_PODCAST_LOCAL_DEL,
            DatabaseHelper.TABLE_PODCAST_LOCAL_DEL, DatabaseHelper.COLUMN_PODCAST_ID);

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getAllPodcasts()
     */
    @Override
    public List<Podcast> getAllPodcasts() {
        return getPodcastsForQuery(QUERY_ALL_PODCASTS);
    }

    private Podcast getPodcast(Cursor c) {
        Podcast p = new Podcast();

        p.setId(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_ID)));
        p.setUrl(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_URL)));
        p.setTitle(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_TITLE)));
        p.setDescription(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_DESCRIPTION)));
        p.setLogoUrl(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LOGO_URL)));
        p.setLastUpdate(c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LAST_UPDATE)));
        p.setLogoFilePath(c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_LOGO_FILE_PATH)));
        p.setLocalAdd(!c.isNull(c.getColumnIndex(QUERY_COLUMN_PODCAST_LOCAL_ADD)));
        p.setLocalDel(!c.isNull(c.getColumnIndex(QUERY_COLUMN_PODCAST_LOCAL_DEL)));

        return p;
    }

    @Override
    public int update(Podcast podcast) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = toContentValues(podcast);

        String selection = DatabaseHelper.COLUMN_PODCAST_ID + " = ?";
        String[] selectionArgs = {
            String.valueOf(podcast.getId())
        };

        int ret = db.update(DatabaseHelper.TABLE_PODCAST, values, selection, selectionArgs);

        return ret;
    }

    private static final String QUERY_PODCAST_BY_ID = String.format("%s where %s = ?;",
            QUERY_ALL_PODCASTS.substring(0, QUERY_ALL_PODCASTS.length() - 1),
            DatabaseHelper.COLUMN_PODCAST_ID);

    /**
     * @see at.ac.tuwien.detlef.db.PodcastDAO#getPodcastById(at.ac.tuwien.detlef
     *      .domain.Podcast)
     */
    @Override
    public Podcast getPodcastById(long podcastId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {
            String.valueOf(podcastId)
        };

        Cursor c = db.rawQuery(QUERY_PODCAST_BY_ID, selectionArgs);

        Podcast p = null;
        if (c.moveToFirst()) {
            do {
                p = getPodcast(c);
            } while (c.moveToNext());
        }
        c.close();
        return p;
    }

    private static final String QUERY_PODCAST_BY_URL = String.format("%s where %s = ?;",
            QUERY_ALL_PODCASTS.substring(0, QUERY_ALL_PODCASTS.length() - 1),
            DatabaseHelper.COLUMN_PODCAST_URL);

    @Override
    public Podcast getPodcastByUrl(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = {
            String.valueOf(url)
        };

        Cursor c = db.rawQuery(QUERY_PODCAST_BY_URL, selectionArgs);

        Podcast p = null;
        if (c.moveToFirst()) {
            do {
                p = getPodcast(c);
            } while (c.moveToNext());
        }
        c.close();
        return p;
    }

    @Override
    public int deleteAllPodcasts() {

        int numOfDeletedPocasts = 0;

        for (Podcast podcast : getAllPodcasts()) {
            deletePodcast(podcast);
            numOfDeletedPocasts++;
        }
        return numOfDeletedPocasts;
    }

    @Override
    public boolean localDeletePodcast(Podcast podcast) {
        if (podcast.isLocalAdd()) {
            return deletePodcast(podcast) > 0;
        }

        SQLiteDatabase db = null;
        try {
            deleteEpisodesForPodcast(podcast);

            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PODCAST_DEL_ID, podcast.getId());

            if (db.insert(DatabaseHelper.TABLE_PODCAST_LOCAL_DEL, null, values) == -1) {
                throw new SQLiteException("Failed to insert podcast into local del table");
            }

            podcast.setLocalDel(true);

            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }

        return false;
    }

    @Override
    public boolean setRemotePodcast(Podcast podcast) {
        SQLiteDatabase db = null;
        try {

            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            String selection = DatabaseHelper.COLUMN_PODCAST_ADD_ID + " = ?";
            String[] selectionArgs = {
                String.valueOf(podcast.getId())
            };

            db.delete(DatabaseHelper.TABLE_PODCAST_LOCAL_ADD, selection, selectionArgs);

            selection = DatabaseHelper.COLUMN_PODCAST_DEL_ID + " = ?";
            selectionArgs[0] = String.valueOf(podcast.getId());

            db.delete(DatabaseHelper.TABLE_PODCAST_LOCAL_DEL, selection, selectionArgs);

            podcast.setLocalAdd(false);
            podcast.setLocalDel(false);

            db.setTransactionSuccessful();

            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
        } finally {
            if (db != null && db.isOpen()) {
                db.endTransaction();
            }
        }

        return false;
    }

    private List<Podcast> getPodcastsForQuery(String query) {
        List<Podcast> podcasts = new ArrayList<Podcast>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                Podcast p = getPodcast(c);
                podcasts.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return podcasts;
    }

    private static final String QUERY_NON_DELETED_PODCASTS = String.format(
                "%s where %s is null;",
                QUERY_ALL_PODCASTS.substring(0, QUERY_ALL_PODCASTS.length() - 1),
                QUERY_COLUMN_PODCAST_LOCAL_DEL);

    @Override
    public List<Podcast> getNonDeletedPodcasts() {
        return getPodcastsForQuery(QUERY_NON_DELETED_PODCASTS);
    }

    private static final String QUERY_LOCALLY_ADDED_PODCASTS = String.format(
                "%s where %s not null;",
                QUERY_ALL_PODCASTS.substring(0, QUERY_ALL_PODCASTS.length() - 1),
                QUERY_COLUMN_PODCAST_LOCAL_ADD);

    @Override
    public List<Podcast> getLocallyAddedPodcasts() {
        return getPodcastsForQuery(QUERY_LOCALLY_ADDED_PODCASTS);
    }

    private static final String QUERY_LOCALLY_DELETED_PODCASTS = String.format(
                "%s where %s not null;",
                QUERY_ALL_PODCASTS.substring(0, QUERY_ALL_PODCASTS.length() - 1),
                QUERY_COLUMN_PODCAST_LOCAL_DEL);

    @Override
    public List<Podcast> getLocallyDeletedPodcasts() {
        return getPodcastsForQuery(QUERY_LOCALLY_DELETED_PODCASTS);
    }
}
