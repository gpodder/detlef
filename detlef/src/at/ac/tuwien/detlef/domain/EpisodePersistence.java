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



package at.ac.tuwien.detlef.domain;

import java.io.File;
import java.io.IOException;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.download.DetlefDownloadManager.DownloadCallback;

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
            getDownloadManager().enqueue(new EpisodeDownloadCallback(episode));
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
        episode.setFilePath(null);
        episode.setStorageState(StorageState.NOT_ON_DEVICE);

        cancelDownload(episode);

        if (episode.getFilePath() != null) {
            File file = new File(episode.getFilePath());
            file.delete();
        }
    }

    private static DetlefDownloadManager getDownloadManager() {
        return Singletons.i().getDownloadManager(getContext());
    }

    private static Context getContext() {
        return Detlef.getAppContext();
    }

    private static class EpisodeDownloadCallback implements DownloadCallback {

        private final Episode episode;
        private final Podcast podcast;

        public EpisodeDownloadCallback(Episode episode) {
            this.episode = episode;
            this.podcast = episode.getPodcast();
        }

        @Override
        public void onStart(String path) {
            episode.setFilePath(path);
            episode.setStorageState(StorageState.DOWNLOADING);

            Singletons.i().getEpisodeDAO().update(episode);
        }

        @Override
        public void onCancel() {
            episode.setFilePath(null);
            episode.setStorageState(StorageState.NOT_ON_DEVICE);

            Singletons.i().getEpisodeDAO().update(episode);
        }

        @Override
        public void onError() {
            episode.setFilePath(null);
            episode.setStorageState(StorageState.NOT_ON_DEVICE);

            Singletons.i().getEpisodeDAO().update(episode);
        }

        @Override
        public void onFinish(Uri uri) {
            episode.setFilePath(uri.getPath());
            episode.setStorageState(StorageState.DOWNLOADED);

            Singletons.i().getEpisodeDAO().update(episode);

            Toast.makeText(Detlef.getAppContext(),
                           String.format("Download complete: %s", episode.getTitle()),
                           Toast.LENGTH_SHORT).show();
        }

        @Override
        public Uri getSource() {
            return Uri.parse(episode.getUrl());
        }

        @Override
        public String getDestinationDirType() {
            return Environment.DIRECTORY_MUSIC;
        }

        @Override
        public String getDestinationSubPath() {
            return String.format("%s/%s", removeUnwantedCharacters(podcast.getTitle()),
                                 removeUnwantedCharacters(new File(getSource().getPath()).getName()));
        }

        @Override
        public String getTitle() {
            return episode.getTitle();
        }

        @Override
        public String getDescription() {
            return String.format("Downloading episode from podcast %s", podcast.getTitle());
        }

        @Override
        public int getNotificationVisibility() {
            return DownloadManager.Request.VISIBILITY_VISIBLE;
        }

        @Override
        public Object getObject() {
            return episode;
        }

        private static String removeUnwantedCharacters(String path) {
            for (char unwantedChar : new char[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*', '=', ' ' }) {
                path = path.replace(unwantedChar, '_');
            }

            return path;
        }
    }


}
