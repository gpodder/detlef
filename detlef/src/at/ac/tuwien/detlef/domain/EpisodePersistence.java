
package at.ac.tuwien.detlef.domain;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;

/**
 * EpisodePersistence contains static helper methods for handling
 * persistence-related functionalities related to episodes, such as download
 * management and episode file deletion.
 */
public final class EpisodePersistence {

    private static final String TAG = EpisodePersistence.class.getName();

    private EpisodePersistence() {
        /* Non-instantiable. */
    }

    /**
     * Begins downloading the current episode, setting its storage state to
     * DOWNLOADING. On completion, the episode's state is updated to DOWNLOADED.
     * Interested parties are notified through EpisodeDAO listeners.
     * @param episode The episode to download (episode != null, episode.getUrl() != null).
     */
    public static void download(Episode episode) throws IOException {
        try {
            getDownloadManager().enqueue(episode);
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to download: ", e);
            throw e;
        }
    }

    /**
     * Cancels active downloads of the current episode (if one exists),
     * and sets its storage state to NOT_ON_DEVICE. Does not fail if there
     * is no active download.
     * @param episode The episode whose download to cancel.
     */
    public static void cancelDownload(Episode episode) {
        getDownloadManager().cancel(episode);
    }

    /**
     * Delete the specified episode file from disk. Automatically cancels
     * any ongoing downloads (see {@link EpisodePersistence#cancelDownload(Episode)}).
     * Never fails, even if the episode is currently not stored on the device and if
     * there is no active download.
     * @param episode The episode to delete.
     */
    public static void delete(Episode episode) {
        cancelDownload(episode);

        File file = new File(episode.getFilePath());
        file.delete();
    }

    private static DetlefDownloadManager getDownloadManager() {
        return DependencyAssistant.getDependencyAssistant().getDownloadManager(getContext());
    }

    private static Context getContext() {
        return Detlef.getAppContext();
    }

}
