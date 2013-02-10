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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.EpisodePersistence;
import at.ac.tuwien.detlef.domain.LocalEpisodeAction;
import at.ac.tuwien.detlef.domain.Podcast;

public final class SimpleEpisodeDAO implements EpisodeDAO {

    private static final String TAG = SimpleEpisodeDAO.class.getName();

    private final DatabaseHelper dbHelper;
    private final PodcastDAO podcastDAO;
    private final Set<EpisodeDAO.OnEpisodeChangeListener> listeners =
        new HashSet<EpisodeDAO.OnEpisodeChangeListener>();

    public SimpleEpisodeDAO(Context context) {
        dbHelper = Singletons.i().getDatabaseHelper();
        podcastDAO = Singletons.i().getPodcastDAO();

        /* Take care of any pending database upgrades. */

        dbHelper.getWritableDatabase();
    }

    /**
     * @see EpisodeDAO#insertEpisode(Episode)
     */
    @Override
    public Episode insertEpisode(Episode episode) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = toContentValues(episode);

            long id = db.insert(DatabaseHelper.TABLE_EPISODE, null, values);
            if (id == -1) {
                throw new SQLiteException("Episode insert failed");
            }

            episode.setId(id);
            notifyListenersAdded(episode);

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
            return null;
        }

        return episode;
    }

    private ContentValues toContentValues(Episode episode) {
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
        values.put(DatabaseHelper.COLUMN_EPISODE_PLAYPOSITION, episode.getPlayPosition());
        if (episode.getActionState() == null) {
            values.putNull(DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE);
        } else {
            values.put(DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE,
                       episode.getActionState().toString());
        }
        return values;
    }

    /**
     * @see EpisodeDAO#deleteEpisode(Episode)
     */
    @Override
    public int deleteEpisode(Episode episode) {
        EpisodePersistence.delete(episode);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = {
            String.valueOf(episode.getId())
        };

        int ret = db.delete(DatabaseHelper.TABLE_EPISODE, selection, selectionArgs);

        notifyListenersDeleted(episode);

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
    public Episode getEpisode(long id) {
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = {
            String.valueOf(id)
        };
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
        String[] selectionArgs = {
            String.valueOf(podcast.getId())
        };
        return getEpisodesWhere(selection, selectionArgs);
    }

    /**
     * @see EpisodeDAO#update(Episode)
     */
    @Override
    public int update(Episode episode) {
        ContentValues values = toContentValues(episode);
        SQLiteDatabase db = null;
        int rows = 0;

        /* We need the old state of the episode in order to deduct current changes
         * for episode actions. */

        Episode oldEpisode = getEpisode(episode.getId());

        try {
            db = dbHelper.getWritableDatabase();

            String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
            String[] selectionArgs = {
                String.valueOf(episode.getId())
            };

            rows = db.update(DatabaseHelper.TABLE_EPISODE, values, selection, selectionArgs);

            /* Trigger download and play position episode actions if applicable. */

            EpisodeActionDAO epDao = Singletons.i().getEpisodeActionDAO();
            if (episode.getStorageState() == Episode.StorageState.DOWNLOADED &&
                    oldEpisode.getStorageState() != episode.getStorageState()) {
                LocalEpisodeAction action = new LocalEpisodeAction(episode.getPodcast(),
                        episode.getUrl(), Episode.ActionState.DOWNLOAD, null, null, null);
                epDao.insertEpisodeAction(action);
            }

            if (oldEpisode.getPlayPosition() != episode.getPlayPosition()) {
                LocalEpisodeAction action = new LocalEpisodeAction(episode.getPodcast(),
                        episode.getUrl(), Episode.ActionState.PLAY, null,
                        episode.getPlayPosition() / 1000,
                        null);
                epDao.insertEpisodeAction(action);
            }

            notifyListenersChanged(episode);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }

        return rows;
    }

    @Override
    public void addEpisodeChangedListener(EpisodeDAO.OnEpisodeChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEpisodeChangedListener(EpisodeDAO.OnEpisodeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersChanged(Episode episode) {
        for (EpisodeDAO.OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeChanged(episode);
        }
    }

    private void notifyListenersAdded(Episode episode) {
        for (EpisodeDAO.OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeAdded(episode);
        }
    }

    private void notifyListenersDeleted(Episode episode) {
        for (EpisodeDAO.OnEpisodeChangeListener listener : listeners) {
            listener.onEpisodeDeleted(episode);
        }
    }

    @Override
    public Episode getEpisodeByUrlOrGuid(String url, String guid) {
        String selection = DatabaseHelper.COLUMN_EPISODE_URL + " = ? OR "
                           + DatabaseHelper.COLUMN_EPISODE_GUID + " = ?";
        String[] selectionArgs = {
            url, guid
        };
        List<Episode> episodes = getEpisodesWhere(selection, selectionArgs);
        if (episodes.size() > 0) {
            return episodes.get(0);
        }
        return null;
    }

    private List<Episode> getEpisodesWhere(String selection,
                                           String[] selectionArgs) {
        List<Episode> allEpisodes = new ArrayList<Episode>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
            DatabaseHelper.COLUMN_EPISODE_AUTHOR,
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
            DatabaseHelper.COLUMN_EPISODE_STATE,
            DatabaseHelper.COLUMN_EPISODE_PLAYPOSITION,
            DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE
        };

        Cursor c =
            db.query(DatabaseHelper.TABLE_EPISODE, projection, selection, // columns
                     // for
                     // where
                     // clause
                     selectionArgs, // values for where clause
                     null, // group
                     null, // filter by row group
                     DatabaseHelper.COLUMN_EPISODE_RELEASED + " DESC" // sort order
                    );

        if (c.moveToFirst()) {
            do {
                Episode e = getEpisode(c);
                allEpisodes.add(e);
            } while (c.moveToNext());
        }
        c.close();
        return allEpisodes;
    }

    private Episode getEpisode(Cursor c) {
        long key = c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_ID));

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
        e.setPlayPosition(c.getInt(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_PLAYPOSITION)));
        String state = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_STATE));
        String aState = c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE));
        if (state != null) {
            e.setStorageState(StorageState.valueOf(state));
        }
        if (aState != null) {
            e.setActionState(ActionState.valueOf(aState));
        }

        return e;
    }

}
