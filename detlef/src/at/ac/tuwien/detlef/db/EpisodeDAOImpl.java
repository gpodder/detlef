
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
            values.put(DatabaseHelper.columnEpisodeAuthor, episode.getAuthor());
            values.put(DatabaseHelper.columnEpisodeDescription, episode.getDescription());
            values.put(DatabaseHelper.columnEpisodeFilesize, episode.getFileSize());
            values.put(DatabaseHelper.columnEpisodeGuid, episode.getGuid());
            values.put(DatabaseHelper.columnEpisodeLink, episode.getLink());
            values.put(DatabaseHelper.columnEpisodeMimetype, episode.getMimetype());
            values.put(DatabaseHelper.columnEpisodePodcast, episode.getPodcast().getId());
            values.put(DatabaseHelper.columnEpisodeReleased, episode.getReleased());
            values.put(DatabaseHelper.columnEpisodeTitle, episode.getTitle());
            values.put(DatabaseHelper.columnEpisodeUrl, episode.getUrl());
            values.put(DatabaseHelper.columnEpisodeFilePath, episode.getFilePath());
            values.put(DatabaseHelper.columnEpisodeState, episode.getState().toString());

            long id = db.insert(DatabaseHelper.tableEpisode, null, values);
            db.close();
            return id;
        } catch (Exception ex) {
            ex.toString();
        }
        return 0;

    }

    public int deleteEpisode(Episode episode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.columnEpisodeId + " = ?";
        String[] selectionArgs = {
                String.valueOf(episode.getId())
        };

        int ret = db.delete(DatabaseHelper.tableEpisode, selection, selectionArgs);
        db.close();
        return ret;
    }

    public List<Episode> getAllEpisodes() {
        return getEpisodesWhere(null, null);
    }

    public List<Episode> getEpisodes(Podcast podcast) {
        String selection = DatabaseHelper.columnEpisodePodcast + " = ?";
        String[] selectionArgs = {
                String.valueOf(podcast.getId())
        };
        return getEpisodesWhere(selection, selectionArgs);
    }

    private List<Episode> getEpisodesWhere(String selection, String[] selectionArgs) {
        List<Episode> allEpisodes = new ArrayList<Episode>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseHelper.columnEpisodeAuthor, DatabaseHelper.columnEpisodeDescription,
                DatabaseHelper.columnEpisodeFilesize, DatabaseHelper.columnEpisodeGuid,
                DatabaseHelper.columnEpisodeId, DatabaseHelper.columnEpisodeLink,
                DatabaseHelper.columnEpisodeMimetype, DatabaseHelper.columnEpisodePodcast,
                DatabaseHelper.columnEpisodeReleased, DatabaseHelper.columnEpisodeTitle,
                DatabaseHelper.columnEpisodeUrl, DatabaseHelper.columnEpisodeFilePath,
                DatabaseHelper.columnEpisodeState
        };

        Cursor c = db.query(DatabaseHelper.tableEpisode, projection, selection, // columns
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
        values.put(DatabaseHelper.columnEpisodeFilePath, episode.getFilePath());
        return updateFieldUsingEpisodeId(episode, values);
    }

    public int updateState(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.columnEpisodeState, episode.getState().toString());
        return updateFieldUsingEpisodeId(episode, values);
    }

    private int updateFieldUsingEpisodeId(Episode episode, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = DatabaseHelper.columnEpisodeId + " = ?";
        String[] selectionArgs = {
                String.valueOf(episode.getId())
        };

        int ret = db.update(DatabaseHelper.tableEpisode, values, selection, selectionArgs);
        db.close();
        return ret;
    }

}
