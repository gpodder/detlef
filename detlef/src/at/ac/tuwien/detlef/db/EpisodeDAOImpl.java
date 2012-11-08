
package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.State;
import at.ac.tuwien.detlef.domain.Podcast;

public class EpisodeDAOImpl implements EpisodeDAO {

    private DatabaseHelper dbHelper;

    private Context context;

    public EpisodeDAOImpl(Context context) {
        dbHelper = new DatabaseHelper(context);
        this.context = context;
    }

    public long insertEpisode(Episode episode) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_EPISODE_AUTHOR, episode.getAuthor());
            values.put(DatabaseHelper.COLUMN_EPISODE_DESCRIPTION, episode.getDescription());
            values.put(DatabaseHelper.COLUMN_EPISODE_FILESIZE, episode.getFileSize());
            values.put(DatabaseHelper.COLUMN_EPISODE_GUID, episode.getGuid());
            values.put(DatabaseHelper.COLUMN_EPISODE_LINK, episode.getLink());
            values.put(DatabaseHelper.COLUMN_EPISODE_MIMETYPE, episode.getMimetype());
            values.put(DatabaseHelper.COLUMN_EPISODE_PODCAST, episode.getPodcast().getId());
            values.put(DatabaseHelper.COLUMN_EPISODE_RELEASED, episode.getReleased());
            values.put(DatabaseHelper.COLUMN_EPISODE_TITLE, episode.getTitle());
            values.put(DatabaseHelper.COLUMN_EPISODE_URL, episode.getUrl());
            values.put(DatabaseHelper.COLUMN_EPISODE_FILEPATH, episode.getFilePath());
            values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode.getState().toString());

            long id = db.insert(DatabaseHelper.TABLE_EPISODE, null, values);
            db.close();
            return id;
        } catch (Exception ex) {
            ex.toString();
        }
        return 0;

    }

    public int deleteEpisode(Episode episode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(episode.getId())
        };

        int ret = db.delete(DatabaseHelper.TABLE_EPISODE, selection, selectionArgs);
        db.close();
        return ret;
    }

    public List<Episode> getAllEpisodes() {
        return getEpisodesWhere(null, null);
    }

    public List<Episode> getEpisodes(Podcast podcast) {
        String selection = DatabaseHelper.COLUMN_EPISODE_PODCAST + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcast.getId())
        };
        return getEpisodesWhere(selection, selectionArgs);
    }

    private List<Episode> getEpisodesWhere(String selection, String[] selectionArgs) {
        List<Episode> allEpisodes = new ArrayList<Episode>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.COLUMN_EPISODE_AUTHOR, DatabaseHelper.COLUMN_EPISODE_DESCRIPTION,
                DatabaseHelper.COLUMN_EPISODE_FILESIZE, DatabaseHelper.COLUMN_EPISODE_GUID,
                DatabaseHelper.COLUMN_EPISODE_ID, DatabaseHelper.COLUMN_EPISODE_LINK,
                DatabaseHelper.COLUMN_EPISODE_MIMETYPE, DatabaseHelper.COLUMN_EPISODE_PODCAST,
                DatabaseHelper.COLUMN_EPISODE_RELEASED, DatabaseHelper.COLUMN_EPISODE_TITLE,
                DatabaseHelper.COLUMN_EPISODE_URL, DatabaseHelper.COLUMN_EPISODE_FILEPATH,
                DatabaseHelper.COLUMN_EPISODE_STATE
        };

        Cursor c = db.query(DatabaseHelper.TABLE_EPISODE, projection, selection, // columns
                // for
                // where
                // clause
                selectionArgs, // values for where clause
                null, // group
                null, // filter by row group
                null // sort order
                );

        if (c.moveToFirst()) {
            PodcastDAO pdao = new PodcastDAOImpl(context);
            do {
                Episode e = new Episode();
                e.setAuthor(c.getString(0));
                e.setDescription(c.getString(1));
                e.setFileSize(c.getString(2));
                e.setGuid(c.getString(3));
                e.setId(c.getLong(4));
                e.setLink(c.getString(5));
                e.setMimetype(c.getString(6));
                e.setPodcast(pdao.getPodcastById(c.getLong(7)));
                e.setReleased(c.getLong(8));
                e.setTitle(c.getString(9));
                e.setUrl(c.getString(10));
                e.setFilePath(c.getString(11));
                e.setState(State.valueOf(c.getString(12)));
                allEpisodes.add(e);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return allEpisodes;
    }

    public int updateFilePath(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EPISODE_FILEPATH, episode.getFilePath());
        return updateFieldUsingEpisodeId(episode, values);
    }

    public int updateState(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EPISODE_STATE, episode.getState().toString());
        return updateFieldUsingEpisodeId(episode, values);
    }

    private int updateFieldUsingEpisodeId(Episode episode, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DatabaseHelper.COLUMN_EPISODE_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(episode.getId())
        };

        int ret = db.update(DatabaseHelper.TABLE_EPISODE, values, selection, selectionArgs);
        db.close();
        return ret;
    }

}
