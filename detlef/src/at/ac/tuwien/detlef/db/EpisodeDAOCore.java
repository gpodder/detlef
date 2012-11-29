package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;


/**
 * Provides the "core" low-level DAO functionality that is concerned with episodes.
 * 
 * The reason for this to be in a separate class is that the functionality is used by
 * several classes (i.e. {@link EpisodeDAOImpl} and {@link SearchKeywordDb}).
 * 
 * @author moe
 */
public class EpisodeDAOCore {

    private final HashMap<Long, Episode> hashMapEpisode = new HashMap<Long, Episode>();
    
    private SQLiteOpenHelper dbHelper;

    private PodcastDAOImpl podcastDAO;

    public EpisodeDAOCore(Context context, PodcastDAOImpl pPodcastDAO) {
        dbHelper = new DatabaseHelper(context);
        podcastDAO = pPodcastDAO;
    }

    public List<Episode> getEpisodesWhere(String selection,
            String[] selectionArgs) {
        synchronized (DatabaseHelper.bigFrigginLock) {
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
                    DatabaseHelper.COLUMN_EPISODE_STATE,
                    DatabaseHelper.COLUMN_EPISODE_PLAYPOSITION,
                    DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE };

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
    }
    
    private Episode getEpisode(Cursor c) {
        long key = c.getLong(c.getColumnIndex(DatabaseHelper.COLUMN_EPISODE_ID));
        if (hashMapEpisode.containsKey(key)) {
            return hashMapEpisode.get(key);
        }

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
        String actionState = c.getString(c.getColumnIndex(
                DatabaseHelper.COLUMN_EPISODE_ACTIONSTATE));
        if (state != null) {
            e.setStorageState(StorageState.valueOf(state));
        }
        if (actionState != null) {
            e.setActionState(ActionState.valueOf(actionState));
        }

        hashMapEpisode.put(key, e);

        return e;
    }
    
}
