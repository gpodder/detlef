package at.ac.tuwien.detlef.download;

import java.io.IOException;
import java.util.HashMap;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class DetlefDownloadManager {

    private static final String TAG = DetlefDownloadManager.class.getName();

    /**
     * Active downloads stored by their download manager id.
     */
    private final HashMap<Long, Episode> activeDownloads = new HashMap<Long, Episode>();

    /**
     * The context used for getting application directories.
     */
    private final Context context;

    /**
     * THe {@link DownloadManager} responsible for downloading episode files.
     */
    private final DownloadManager downloadManager;

    public DetlefDownloadManager(Context context) {
        this.context = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void enqueue(Episode episode) throws IOException {
        if (!isExternalStorageWritable()) {
            throw new IOException("Cannot write to external storage");
        }

        Podcast podcast = episode.getPodcast();
        Uri uri = Uri.parse(episode.getUrl());

        /* We may need to change our naming policy in case of duplicates. However,
         * let's ignore this for now since it's simplest for us and the user.
         */
        String path = String.format("%s/%s", podcast.getTitle(), episode.getTitle());

        Request request = new Request(uri);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, path);
        request.allowScanningByMediaScanner();
        request.setTitle(episode.getTitle());
        request.setDescription(
                String.format("Downloading episode from podcast %s", podcast.getTitle()));

        long id = downloadManager.enqueue(request);
        activeDownloads.put(id, episode);

        Log.v(TAG, String.format("Enqueued download task %s", path));
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Called once a download has completed. Responsible for updating the internal
     * state and pushing episode changes to the database.
     * @param id The download id.
     */
    public void downloadComplete(long id) {
        Episode episode = activeDownloads.remove(id);
        if (episode == null) {
            Log.w(TAG, String.format("No active download found for id %d", id));
            return;
        }

        if (!isDownloadSuccessful(id)) {
            Log.w(TAG, String.format("Download for id %d did not complete successfully", id));
            return;
        }

        Uri uri = downloadManager.getUriForDownloadedFile(id);
        String path = uri.toString(); /* TODO: Should we update the filesize here? */

        episode.setFilePath(path);

        EpisodeDAOImpl dao = EpisodeDAOImpl.i(context);
        dao.updateFilePath(episode);

        Log.v(TAG, String.format("File %s downloaded successfully", path));
    }

    private boolean isDownloadSuccessful(long id) {
        Query query = new Query();
        query.setFilterById(id);

        Cursor c = downloadManager.query(query);
        if (!c.moveToFirst()) {
            return false;
        }

        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
        return (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex));
    }

}
