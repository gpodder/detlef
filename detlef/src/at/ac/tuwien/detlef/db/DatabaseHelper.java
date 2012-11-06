package at.ac.tuwien.detlef.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper class which creates the database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final int version = 1;

    public static final String dbName = "detlefDB";

    // podcast table
    public static final String tablePodcast = "Podcast";

    public static final String columnPodcastId = "_ID";

    public static final String columnPodcastUrl = "url";

    public static final String columnPodcastTitle = "title";

    public static final String columnPodcastDescription = "description";

    public static final String columnPodcastLogoUrl = "logoUrl";

    public static final String columnPodcastLastUpdate = "lastUpdate";

    public static final String columnPodcastLogoFilePath = "logoFilePath";

    // episode table
    public static final String tableEpisode = "Episode";

    public static final String columnEpisodeId = "_ID";

    public static final String columnEpisodeGuid = "guid";

    public static final String columnEpisodeTitle = "title";

    public static final String columnEpisodeDescription = "description";

    public static final String columnEpisodeReleased = "released";

    public static final String columnEpisodeLink = "link";

    public static final String columnEpisodeAuthor = "author";

    public static final String columnEpisodeUrl = "url";

    public static final String columnEpisodeMimetype = "mimetype";

    public static final String columnEpisodeFilesize = "filesize";

    public static final String columnEpisodePodcast = "podcast";

    public static final String columnEpisodeFilePath = "filePath";

    public static final String columnEpisodeState = "state";

    // create statement for the podcast table
    static final String createPodcastTable = "CREATE TABLE " + tablePodcast
            + " (" + columnPodcastId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + columnPodcastUrl + " TEXT, " + columnPodcastTitle + " TEXT, "
            + columnPodcastLogoUrl + " TEXT, " + columnPodcastLastUpdate + " INTEGER, "
            + columnPodcastLogoFilePath + " TEXT);";

    // create statement for the episode table
    static final String createEpisodeTable = "CREATE TABLE " + tableEpisode
            + " (" + columnEpisodeId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + columnEpisodeGuid + " TEXT, " + columnEpisodeTitle + " TEXT, "
            + columnEpisodeDescription + " TEXT, " + columnEpisodeReleased
            + " INTEGER, " + columnEpisodeLink + " TEXT, "
            + columnEpisodeAuthor + " TEXT, " + columnEpisodeUrl + " TEXT, "
            + columnEpisodeMimetype + " TEXT, " + columnEpisodeFilesize
            + columnEpisodeFilePath + " TEXT, " + columnEpisodeState + " Text, "
            + " INTEGER NOT NULL, FOREIGN KEY (" + columnEpisodePodcast
            + ") REFERENCES " + tablePodcast + "(" + columnPodcastId
            + ") ON DELETE CASCADE);";

    public DatabaseHelper(Context context) {
        super(context, dbName, null, version);
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
        // TODO: what should we do on upgrade? i think nothing yet ... has to be
        // considered
        // when updating
        // db.execSQL("DROP TABLE IF EXISTS " + tableEpisode);
        // db.execSQL("DROP TABLE IF EXISTS " + tablePodcast);
        // onCreate(db);

    }
}
