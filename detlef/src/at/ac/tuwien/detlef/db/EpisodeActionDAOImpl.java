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

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.LocalEpisodeAction;
import at.ac.tuwien.detlef.domain.RemoteEpisodeAction;

import com.dragontek.mygpoclient.api.EpisodeAction;

/**
 * DAO for episode action access.
 */
public class EpisodeActionDAOImpl implements EpisodeActionDAO {
    private static final String TAG = EpisodeDAOImpl.class.getName();

    private static final EpisodeActionDAOImpl INSTANCE = new EpisodeActionDAOImpl(
            Detlef.getAppContext());

    private final DatabaseHelper dbHelper;

    /**
     * Returns the EpisodeActionDAOImpl singleton instance.
     */
    public static EpisodeActionDAOImpl i() {
        return INSTANCE;
    }

    protected EpisodeActionDAOImpl(Context context) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            dbHelper = new DatabaseHelper(context);

            /* Take care of any pending database upgrades. */

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
        }
    }

    @Override
    public boolean insertEpisodeAction(LocalEpisodeAction episodeAction) {
        if (episodeAction.getPodcast() == null) {
            return false;
        }

        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;
            try {
                db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_EPISODE_ACTION_ACTION,
                        episodeAction.getActionString());
                values.put(DatabaseHelper.COLUMN_EPISODE_ACTION_EPISODE_ID,
                        episodeAction.getEpisode());
                values.put(DatabaseHelper.COLUMN_EPISODE_ACTION_PODCAST, episodeAction.getPodcast()
                        .getId());

                db.beginTransaction();

                long id = db.insert(DatabaseHelper.TABLE_EPISODE_ACTION, null, values);
                if (id == -1) {
                    throw new SQLiteException("Episode action insert failed");
                }

                if (episodeAction.getAction() == Episode.ActionState.PLAY) {
                    values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_ID, id);
                    values.put(DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_STARTED,
                            episodeAction.getStarted());
                    values.put(DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_POSITION,
                            episodeAction.getPosition());
                    values.put(DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_TOTAL,
                            episodeAction.getTotal());

                    if (db.insert(DatabaseHelper.TABLE_EPISODE_PLAY_ACTION, null, values) == -1) {
                        throw new SQLiteException("Episode play action insert failed");
                    }
                }

                db.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
                return false;
            } finally {
                if ((db != null) && db.isOpen()) {
                    db.endTransaction();
                    db.close();
                }
            }

            return true;
        }
    }

    private static final String QUERY_ALL_EPISODE_ACTIONS = String.format("select "
            + "ea.%s, "
            + "p.%s, "
            + "ea.%s, "
            + "ea.%s, "
            + "ea.%s, "
            + "epa.%s, "
            + "epa.%s, "
            + "epa.%s "
            + "from %s ea left outer join %s epa on ea.%s = epa.%s join %s p on ea.%s = p.%s;",
            DatabaseHelper.COLUMN_EPISODE_ACTION_ID,
            DatabaseHelper.COLUMN_PODCAST_URL, DatabaseHelper.COLUMN_EPISODE_ACTION_EPISODE_ID,
            DatabaseHelper.COLUMN_EPISODE_ACTION_ACTION,
            DatabaseHelper.COLUMN_EPISODE_ACTION_TIMESTAMP,
            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_STARTED,
            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_POSITION,
            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_TOTAL, DatabaseHelper.TABLE_EPISODE_ACTION,
            DatabaseHelper.TABLE_EPISODE_PLAY_ACTION, DatabaseHelper.COLUMN_EPISODE_ACTION_ID,
            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_ID, DatabaseHelper.TABLE_PODCAST,
            DatabaseHelper.COLUMN_EPISODE_ACTION_PODCAST, DatabaseHelper.COLUMN_PODCAST_ID);

    @Override
    public List<RemoteEpisodeAction> getAllEpisodeActions() {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            List<RemoteEpisodeAction> episodeActions = new LinkedList<RemoteEpisodeAction>();

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery(QUERY_ALL_EPISODE_ACTIONS, null);
            DeviceId devId =
                    DependencyAssistant.getDependencyAssistant().getGpodderSettings().getDeviceId();
            if (devId == null) {
                return episodeActions;
            }

            if (c.moveToFirst()) {
                do {
                    RemoteEpisodeAction a = new RemoteEpisodeAction(
                            c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_ACTION_ID)),
                            c.getString(c.getColumnIndex(DatabaseHelper.COLUMN_PODCAST_URL)),
                            c.getString(c.getColumnIndex(
                                    DatabaseHelper.COLUMN_EPISODE_ACTION_EPISODE_ID)),
                            c.getString(c.getColumnIndex(
                                    DatabaseHelper.COLUMN_EPISODE_ACTION_ACTION)),
                            devId.toString(),
                            c.getString(c.getColumnIndex(
                                    DatabaseHelper.COLUMN_EPISODE_ACTION_TIMESTAMP)),
                            null, null, null);

                    if (a.action.equals(Episode.ActionState.PLAY.toString().toLowerCase())) {
                        a.started = c.getInt(c.getColumnIndex(
                            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_STARTED));
                        a.position = c.getInt(c.getColumnIndex(
                            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_POSITION));
                        a.total = c.getInt(c.getColumnIndex(
                            DatabaseHelper.COLUMN_EPISODE_PLAY_ACTION_TOTAL));
                    }

                    episodeActions.add(a);
                } while (c.moveToNext());
            }

            c.close();
            db.close();
            return episodeActions;
        }
    }

    @Override
    public boolean flushEpisodeActions(List<RemoteEpisodeAction> episodeActions) {
        synchronized (DatabaseHelper.BIG_FRIGGIN_LOCK) {
            SQLiteDatabase db = null;

            try {
                db = dbHelper.getWritableDatabase();
                String selection = DatabaseHelper.COLUMN_EPISODE_ACTION_ID + " = ?";
                
                for (RemoteEpisodeAction a : episodeActions) {
                    String[] selectionArgs = {
                            String.valueOf(a.getId())
                    };

                    db.delete(DatabaseHelper.TABLE_EPISODE_ACTION, selection, selectionArgs);
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage() != null ? ex.getMessage() : ex.toString());
                return false;
            } finally {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }

            return true;
        }
    }

}
