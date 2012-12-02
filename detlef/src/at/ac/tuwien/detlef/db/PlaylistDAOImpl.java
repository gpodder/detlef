
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
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.Episode;

public final class PlaylistDAOImpl implements PlaylistDAO, EpisodeDAO.OnEpisodeChangeListener {

    private static final String TAG = PlaylistDAOImpl.class.getName();
    private static final PlaylistDAOImpl INSTANCE = new PlaylistDAOImpl(Detlef.getAppContext());

    private final DatabaseHelper dbHelper;
    private final List<PlaylistDAO.OnPlaylistChangeListener> listeners =
            new ArrayList<PlaylistDAO.OnPlaylistChangeListener>();
    private final EpisodeDAO edao;

    /**
     * Returns the PlaylistDAOImpl singleton instance.
     */
    public static PlaylistDAOImpl i() {
        return INSTANCE;
    }

    public PlaylistDAOImpl(Context context) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            dbHelper = new DatabaseHelper(context);
            edao = EpisodeDAOImpl.i();
            edao.addEpisodeChangedListener(this);

            /* Take care of any pending database upgrades. */

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
        }
    }

    @Override
    public void addPlaylistChangedListener(PlaylistDAO.OnPlaylistChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePlaylistChangeListener(PlaylistDAO.OnPlaylistChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all the listeners of the added episode.
     * 
     * @param position The position of the new episode.
     * @param episode The episode that was added.
     */
    private void notifyListenersAdded(int position, Episode episode) {
        for (OnPlaylistChangeListener listener : listeners) {
            listener.onPlaylistEpisodeAdded(position, episode);
        }
    }

    /**
     * Notifies all the listeners of the removed episode.
     * 
     * @param position The former position of the removed episode.
     */
    private void notifyListenersRemoved(int position) {
        for (OnPlaylistChangeListener listener : listeners) {
            listener.onPlaylistEpisodeRemoved(position);
        }
    }

    /**
     * Notifies all the listeners of a move in the ordering.
     * 
     * @param firstPosition The former position of the episode.
     * @param secondPosition The late position of the episode.
     */
    private void notifyListenersChanged(int firstPosition, int secondPosition) {
        for (OnPlaylistChangeListener listener : listeners) {
            listener.onPlaylistEpisodePositionChanged(firstPosition, secondPosition);
        }
    }

    /**
     * Gets the next free ordering item.
     * 
     * @param db The db with which to perform the query.
     * @return The next free ordering item; at least 0.
     */
    private int getNextFreePosition(SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            Cursor cursor = db.rawQuery("SELECT IFNULL(MAX("
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + "), (-1)) FROM "
                    + DatabaseHelper.TABLE_PLAYLIST, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) + 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean addEpisodeToEndOfPlaylist(Episode episode) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                int nextPosition = getNextFreePosition(db);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_EPISODE, episode.getId());
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, nextPosition);

                long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
                if (id == -1) {
                    throw new SQLiteException("Failed to insert playlist item");
                }

                notifyListenersAdded(nextPosition, episode);
                return true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    @Override
    public boolean addEpisodeToBeginningOfPlaylist(Episode episode) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                shiftPositionsFromBy(0, 1, db);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_EPISODE, episode.getId());
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, 0);

                long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
                if (id == -1) {
                    throw new SQLiteException("Failed to insert playlist item");
                }

                notifyListenersAdded(0, episode);
                return true;
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                return false;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * Shifts the ordering by the specified amount, starting from the specified
     * position.
     * 
     * @param from All items >= from will be shifted.
     * @param by The amount by which the items will be shifted. Can be negative
     *            or positive.
     * @param db The db with which to perform the shift.
     */
    private void shiftPositionsFromBy(int from, int by, SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            String[] selectionArgs = {
                    String.valueOf(from)
            };
            String operator;
            if (by >= 0) {
                operator = "+";
            } else {
                operator = "-";
                by *= (-1);
            }
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_PLAYLIST + " SET "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + operator + by + " WHERE "
                    + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " >= ?", selectionArgs);
        }
    }

    @Override
    public ArrayList<Episode> getNonCachedEpisodes() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                ArrayList<Episode> allEpisodes = new ArrayList<Episode>();
                db = dbHelper.getReadableDatabase();
                Cursor c = db.rawQuery(
                        "SELECT "
                                + DatabaseHelper.COLUMN_PLAYLIST_EPISODE + " FROM "
                                + DatabaseHelper.TABLE_PLAYLIST + " ORDER BY "
                                + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " ASC", null);
                if (c.moveToFirst()) {
                    do {
                        Episode e = edao.getEpisode(c.getLong(0));
                        allEpisodes.add(e);
                    } while (c.moveToNext());
                }
                c.close();
                return allEpisodes;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * Removes an item from the playlist and shifts the ordering accordingly.
     * 
     * @param position The position of the episode to remove.
     * @param db The db with which to perform the operations.
     * @return The number of affected rows.
     */
    private int removePosition(int position, SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            int ret = 0;
            String selection = DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = ?";
            String[] selectionArgs = {
                    String.valueOf(position)
            };

            ret = db.delete(DatabaseHelper.TABLE_PLAYLIST, selection, selectionArgs);

            shiftPositionsFromBy(position, -1, db);
            notifyListenersRemoved(position);
            return ret;
        }
    }

    @Override
    public boolean removeEpisode(int position) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            int ret = 0;
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                ret = removePosition(position, db);
                db.close();
                return (ret == 1);
            } catch (Exception ex) {
                Log.e(getClass().getName(), ex.getMessage());
                return false;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    @Override
    public boolean moveEpisode(int firstPosition, int secondPosition) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            int ret = 0;
            try {
                db = dbHelper.getWritableDatabase();
                long id = getIdAt(firstPosition, db);

                shiftPositionsFromBy(firstPosition, -1, db);
                shiftPositionsFromBy(secondPosition, 1, db);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PLAYLIST_POSITION, secondPosition);

                ret = db.update(DatabaseHelper.TABLE_PLAYLIST, values,
                        DatabaseHelper.COLUMN_PLAYLIST_ID
                                + " = ?", new String[] {
                            String.valueOf(id)
                        });
                if (ret == 1) {
                    notifyListenersChanged(firstPosition, secondPosition);
                }
                return ret == 1;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * Gets the playlist database id at the specified playlist position.
     * 
     * @param position The position for which to get the id.
     * @param db The DB with which to perform the query.
     * @return The database ID.
     */
    private long getIdAt(int position, SQLiteDatabase db) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            String[] selectionArgs = {
                    String.valueOf(position)
            };
            Cursor c = db.rawQuery(
                    "SELECT "
                            + DatabaseHelper.COLUMN_PLAYLIST_ID + " FROM "
                            + DatabaseHelper.TABLE_PLAYLIST + " WHERE "
                            + DatabaseHelper.COLUMN_PLAYLIST_POSITION + " = ?", selectionArgs);
            long ret = -1;
            if (c.moveToFirst()) {
                ret = c.getLong(0);
            }
            c.close();
            return ret;
        }
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        // do nothing
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
        // not of interest
    }

    @Override
    public void onEpisodeDeleted(Episode episode) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                Set<Integer> positions = getPositionsOfEpisode(episode, db);
                for (int position : positions) {
                    removePosition(position, db);
                }
            } catch (Exception ex) {
                Log.e(getClass().getName(), ex.getMessage());
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * Gets all the position of a specified episode in the playlist.
     * 
     * @param episode The episode for which to search.
     * @param db The database with which to perform the search.
     * @return All the position of the specified episode in the playlist.
     */
    private Set<Integer> getPositionsOfEpisode(Episode episode, SQLiteDatabase db) {
        HashSet<Integer> ret = new HashSet<Integer>();
        String selection = DatabaseHelper.COLUMN_PLAYLIST_EPISODE + " = ?";
        String[] selectionArgs = {
                String.valueOf(episode.getId())
        };
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLIST, new String[] {
                DatabaseHelper.COLUMN_PLAYLIST_POSITION
        }, selection, selectionArgs, null, null, null);
        if (c.moveToFirst()) {
            do {
                ret.add(c.getInt(0));
            } while (c.moveToNext());
        }
        return ret;
    }

    /**
     * @return Returns true if there are no gaps in the playlist ordering in the
     *         DB. This is not of general interest and exists for testing
     *         purposes only.
     */
    public boolean checkNoGaps() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();
                List<Integer> positions = getAllPositions(db);
                for (int i = 0; i < (positions.size() - 1); i++) {
                    if (positions.get(i + 1) != (positions.get(i) + 1)) {
                        return false;
                    }
                }
                return true;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    /**
     * @param db The DB with which to perform the query.
     * @return A list of all occupied positions in the DB, including doubles and
     *         gaps.
     */
    private List<Integer> getAllPositions(SQLiteDatabase db) {
        List<Integer> ret = new ArrayList<Integer>();
        Cursor c = db.query(DatabaseHelper.TABLE_PLAYLIST, new String[] {
                DatabaseHelper.COLUMN_PLAYLIST_POSITION
        }, null, null, null, null, DatabaseHelper.COLUMN_PLAYLIST_POSITION);
        if (c.moveToFirst()) {
            do {
                ret.add(c.getInt(0));
            } while (c.moveToNext());
        }
        return ret;
    }

    // TODO @Joshi remove played episodes from playlist
    // TODO @Jakob stop download button doesn't change back to download

    @Override
    public void clearPlaylist() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            List<Episode> currentEpisodes = getNonCachedEpisodes();
            for (int i = 0; i < currentEpisodes.size(); i++) {
                removeEpisode(0);
            }
        }
    }
}
