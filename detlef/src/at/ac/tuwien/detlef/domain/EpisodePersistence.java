
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

    public static void download(Episode episode) throws IOException {
        try {
            getDownloadManager().enqueue(episode);
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to download: ", e);
            throw e;
        }
    }

    public static void cancelDownload(Episode episode) {
        getDownloadManager().cancel(episode);
    }

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
