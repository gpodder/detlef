package at.ac.tuwien.detlef.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper class which creates the database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final int VERSION = 2;

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

    /* Create statement for the podcast table. */
    static final String createPodcastTable =
            String.format("create table %s (" +
                    "%s integer primary key autoincrement, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s integer, " +
                    "%s text);",
                    TABLE_PODCAST, COLUMN_PODCAST_ID, COLUMN_PODCAST_URL, COLUMN_PODCAST_TITLE,
                    COLUMN_PODCAST_DESCRIPTION, COLUMN_PODCAST_LOGO_URL,
                    COLUMN_PODCAST_LAST_UPDATE, COLUMN_PODCAST_LOGO_FILE_PATH);

    /* Create statement for the episode table. */
    static final String createEpisodeTable =
            String.format("create table %s (" +
                    "%s integer primary key autoincrement, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s integer, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s text, " +
                    "%s integer not null, " +
                    "foreign key (%s) references %s (%s) on delete cascade);",
                    TABLE_EPISODE, COLUMN_EPISODE_ID, COLUMN_EPISODE_GUID, COLUMN_EPISODE_TITLE,
                    COLUMN_EPISODE_DESCRIPTION, COLUMN_EPISODE_RELEASED, COLUMN_EPISODE_LINK,
                    COLUMN_EPISODE_AUTHOR, COLUMN_EPISODE_URL, COLUMN_EPISODE_MIMETYPE,
                    COLUMN_EPISODE_FILESIZE, COLUMN_EPISODE_FILEPATH, COLUMN_EPISODE_STATE,
                    COLUMN_EPISODE_PODCAST, COLUMN_EPISODE_PODCAST, TABLE_PODCAST,
                    COLUMN_PODCAST_ID);

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createPodcastTable);
        db.execSQL(createEpisodeTable);

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
        // TODO: What should we do on upgrade? For now, drop and recreate all tables.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCAST);
        onCreate(db);

    }
}
