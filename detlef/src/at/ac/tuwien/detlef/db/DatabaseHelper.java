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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 * DatabaseHelper class which creates the database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final int VERSION = 11;

    static final Object BIG_FRIGGIN_LOCK = new Object();

    public static final String DB_NAME = "detlefDB";

    /* Podcast table. */
    public static final String TABLE_PODCAST = "Podcast";
    public static final String COLUMN_PODCAST_ID = "_ID";
    public static final String COLUMN_PODCAST_URL = "url";
    public static final String COLUMN_PODCAST_TITLE = "title";
    public static final String COLUMN_PODCAST_DESCRIPTION = "description";
    public static final String COLUMN_PODCAST_LOGO_URL = "logoUrl";
    public static final String COLUMN_PODCAST_LAST_UPDATE = "lastUpdate";
    public static final String COLUMN_PODCAST_LOGO_FILE_PATH = "logoFilePath";
    public static final String COLUMN_PODCAST_LOGO_FILE_DOWNLOADED = "logoFileDownloaded";

    /* Episode table. */
    public static final String TABLE_EPISODE = "Episode";
    public static final String COLUMN_EPISODE_ID = "_ID";
    public static final String COLUMN_EPISODE_GUID = "guid";
    public static final String COLUMN_EPISODE_TITLE = "title";
    public static final String COLUMN_EPISODE_DESCRIPTION = "description";
    public static final String COLUMN_EPISODE_RELEASED = "released";
    public static final String COLUMN_EPISODE_LINK = "link";
    public static final String COLUMN_EPISODE_AUTHOR = "author";
    public static final String COLUMN_EPISODE_URL = "url";
    public static final String COLUMN_EPISODE_MIMETYPE = "mimetype";
    public static final String COLUMN_EPISODE_FILESIZE = "filesize";
    public static final String COLUMN_EPISODE_PODCAST = "podcast";
    public static final String COLUMN_EPISODE_FILEPATH = "filePath";
    public static final String COLUMN_EPISODE_STATE = "state";
    public static final String COLUMN_EPISODE_PLAYPOSITION = "playposition";
    public static final String COLUMN_EPISODE_ACTIONSTATE = "actionState";

    /* Episode Action table. */
    public static final String TABLE_EPISODE_ACTION = "Episode_actions";
    public static final String COLUMN_EPISODE_ACTION_ID = "_ID";
    public static final String COLUMN_EPISODE_ACTION_PODCAST = "podcast";
    public static final String COLUMN_EPISODE_ACTION_EPISODE_ID = "episode";
    public static final String COLUMN_EPISODE_ACTION_ACTION = "action";
    public static final String COLUMN_EPISODE_ACTION_TIMESTAMP = "timestamp";

    /* Episode Play Action table. */
    public static final String TABLE_EPISODE_PLAY_ACTION = "Episode_play_actions";
    public static final String COLUMN_EPISODE_PLAY_ACTION_ID = "action_id";
    public static final String COLUMN_EPISODE_PLAY_ACTION_STARTED = "started";
    public static final String COLUMN_EPISODE_PLAY_ACTION_POSITION = "position";
    public static final String COLUMN_EPISODE_PLAY_ACTION_TOTAL = "total";

    /* Playlist table. */
    public static final String TABLE_PLAYLIST = "Playlist";
    public static final String COLUMN_PLAYLIST_ID = "_ID";
    public static final String COLUMN_PLAYLIST_EPISODE = "episode";
    public static final String COLUMN_PLAYLIST_POSITION = "position";

    /*
     * Locally deleted Podcasts These are still on the gpodder service but have
     * been removed locally.
     */
    public static final String TABLE_PODCAST_LOCAL_DEL = "Podcast_local_del";
    public static final String COLUMN_PODCAST_DEL_ID = "_ID";

    /*
     * Locally added Podcasts These have been added locally but have not yet
     * been synchronized to the gpodder service.
     */
    public static final String TABLE_PODCAST_LOCAL_ADD = "Podcast_local_add";
    public static final String COLUMN_PODCAST_ADD_ID = "_ID";

    public static final String EPISODE_RELEASED_INDEX = "Episode_Released_Index";

    /* Create statement for the podcast table. */
    static final String CREATE_PODCAST_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s text not null, "
                    + "%s text not null, "
                    + "%s text, "
                    + "%s text, "
                    + "%s integer, "
                    + "%s text,"
                    + "%s integer);",
                    TABLE_PODCAST, COLUMN_PODCAST_ID, COLUMN_PODCAST_URL, COLUMN_PODCAST_TITLE,
                    COLUMN_PODCAST_DESCRIPTION, COLUMN_PODCAST_LOGO_URL,
                    COLUMN_PODCAST_LAST_UPDATE, COLUMN_PODCAST_LOGO_FILE_PATH,
                    COLUMN_PODCAST_LOGO_FILE_DOWNLOADED);

    /* Create statement for the episode table. */
    static final String CREATE_EPISODE_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s text, " // guid
                    + "%s text not null, " // title
                    + "%s text, " // description
                    + "%s integer, " // released
                    + "%s text, " // link
                    + "%s text, " // author
                    + "%s text not null, " // url
                    + "%s text, " // mimetype
                    + "%s integer, " // filesize
                    + "%s text, " // filepath
                    + "%s text, " // state
                    + "%s integer not null, " // podcast
                    + "%s integer, " // playposition
                    + "%s string, " // action state
                    + "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_EPISODE, COLUMN_EPISODE_ID, COLUMN_EPISODE_GUID, COLUMN_EPISODE_TITLE,
                    COLUMN_EPISODE_DESCRIPTION, COLUMN_EPISODE_RELEASED, COLUMN_EPISODE_LINK,
                    COLUMN_EPISODE_AUTHOR, COLUMN_EPISODE_URL, COLUMN_EPISODE_MIMETYPE,
                    COLUMN_EPISODE_FILESIZE, COLUMN_EPISODE_FILEPATH, COLUMN_EPISODE_STATE,
                    COLUMN_EPISODE_PODCAST, COLUMN_EPISODE_PLAYPOSITION,
                    COLUMN_EPISODE_ACTIONSTATE,
                    COLUMN_EPISODE_PODCAST, TABLE_PODCAST, COLUMN_PODCAST_ID);

    /* Create statement for the episode action table. */
    static final String CREATE_EPISODE_ACTION_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s integer, "
                    + "%s text, "
                    + "%s text, "
                    + "%s text, "
                    + "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_EPISODE_ACTION, COLUMN_EPISODE_ACTION_ID, COLUMN_EPISODE_ACTION_PODCAST,
                    COLUMN_EPISODE_ACTION_EPISODE_ID, COLUMN_EPISODE_ACTION_ACTION,
                    COLUMN_EPISODE_ACTION_TIMESTAMP, COLUMN_EPISODE_ACTION_PODCAST,
                    TABLE_PODCAST, COLUMN_PODCAST_ID);

    /* Create statement for the episode play action table. */
    static final String CREATE_EPISODE_PLAY_ACTION_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s integer, "
                    + "%s integer, "
                    + "%s integer, "
                    + "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_EPISODE_PLAY_ACTION, COLUMN_EPISODE_PLAY_ACTION_ID,
                    COLUMN_EPISODE_PLAY_ACTION_STARTED, COLUMN_EPISODE_PLAY_ACTION_POSITION,
                    COLUMN_EPISODE_PLAY_ACTION_TOTAL,
                    COLUMN_EPISODE_PLAY_ACTION_ID, TABLE_EPISODE_ACTION, COLUMN_EPISODE_ACTION_ID);

    /* Create statement for the playlist table. */
    static final String CREATE_PLAYLIST_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key autoincrement, "
                    + "%s integer, "
                    + "%s integer);",
                    TABLE_PLAYLIST, COLUMN_PLAYLIST_ID, COLUMN_PLAYLIST_EPISODE,
                    COLUMN_PLAYLIST_POSITION, COLUMN_PLAYLIST_EPISODE, TABLE_EPISODE,
                    COLUMN_EPISODE_ID); // no foreign key! we handle this
                                        // ourselves.

    /* Create statement for the Podcast local delete table. */
    static final String CREATE_PODCAST_LOCAL_DEL_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key,"
                    + "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_PODCAST_LOCAL_DEL, COLUMN_PODCAST_DEL_ID, COLUMN_PODCAST_DEL_ID,
                    TABLE_PODCAST, COLUMN_PODCAST_ID);

    /* Create statement for the Podcast local add table. */
    static final String CREATE_PODCAST_LOCAL_ADD_TABLE =
            String.format("create table %s ("
                    + "%s integer primary key,"
                    + "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_PODCAST_LOCAL_ADD, COLUMN_PODCAST_ADD_ID, COLUMN_PODCAST_ADD_ID,
                    TABLE_PODCAST, COLUMN_PODCAST_ID);

    /* index on episode released column needed for sorting */
    static final String CREATE_EPISODE_RELEASED_INDEX =
            String.format("create index %s ON %s "
                    + "( %s DESC );",
                    EPISODE_RELEASED_INDEX, TABLE_EPISODE, COLUMN_EPISODE_RELEASED);

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PODCAST_TABLE);
        db.execSQL(CREATE_EPISODE_TABLE);
        db.execSQL(CREATE_EPISODE_ACTION_TABLE);
        db.execSQL(CREATE_EPISODE_PLAY_ACTION_TABLE);
        Log.d(getClass().getName(), "Creating playlist table");
        db.execSQL(CREATE_PLAYLIST_TABLE);
        db.execSQL(CREATE_PODCAST_LOCAL_DEL_TABLE);
        db.execSQL(CREATE_PODCAST_LOCAL_ADD_TABLE);
        db.execSQL(CREATE_EPISODE_RELEASED_INDEX);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: What should we do on upgrade? For now, drop and recreate all
        // tables.
        db.execSQL("DROP INDEX IF EXISTS " + EPISODE_RELEASED_INDEX);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCAST_LOCAL_DEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCAST_LOCAL_ADD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE_PLAY_ACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE_ACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCAST);

        GpodderSettings settings = DependencyAssistant.getDependencyAssistant().getGpodderSettings(
                Detlef.getAppContext());
        settings.setLastUpdate(0);
        DependencyAssistant.getDependencyAssistant().getGpodderSettingsDAO(Detlef.getAppContext())
                .writeSettings(settings);
        onCreate(db);
    }
}
