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
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.download.DetlefDownloadManager.DownloadCallback;
import at.ac.tuwien.detlef.gpodder.events.SubscriptionsChangedEvent;
import de.greenrobot.event.EventBus;

/**
 * PodcastPersistence contains static helper methods for handling
 * persistence-related functionalities related to podcasts, such as download
 * management and logo file deletion.
 */
public final class PodcastPersistence {

    private static final String TAG = PodcastPersistence.class.getName();

    private PodcastPersistence() {
        /* Non-instantiable. */
    }

    /**
     * Begins downloading the current podcast image.
     */
    public static void download(Podcast podcast) throws IOException {
        try {
            getDownloadManager().enqueue(new PodcastLogoDownloadCallback(podcast));
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to download: ", e);
            throw e;
        }
    }

    /**
     * Cancels active downloads of the current podcast image (if one exists).
     */
    public static void cancelDownload(Podcast podcast) {
        getDownloadManager().cancel(podcast);
    }

    /**
     * Delete the specified podcast image file from disk. Automatically cancels
     * any ongoing downloads.
     */
    public static void delete(Podcast podcast) {
        cancelDownload(podcast);

        if (podcast.getLogoFilePath() != null) {
            File file = new File(podcast.getLogoFilePath());
            file.delete();
            podcast.setLogoFilePath(null);
        }
    }

    private static DetlefDownloadManager getDownloadManager() {
        return Singletons.i().getDownloadManager(getContext());
    }

    private static Context getContext() {
        return Detlef.getAppContext();
    }

    private static class PodcastLogoDownloadCallback implements DownloadCallback {

        private final Podcast podcast;

        public PodcastLogoDownloadCallback(Podcast podcast) {
            this.podcast = podcast;

            /* Ensure that the gallery does not pick up our podcast logo images. */

            try {
                File file = new File(
                    Detlef.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    ".nomedia");
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                Log.w(TAG, "Could not create .nomedia file");
            }
        }

        @Override
        public void onStart(String path) {
            /* Nothing. */
        }

        @Override
        public void onCancel() {
            podcast.setLogoFilePath(null);
            Singletons.i().getPodcastDAO().update(podcast);
            EventBus.getDefault().post(new SubscriptionsChangedEvent());
        }

        @Override
        public void onError() {
            podcast.setLogoFilePath(null);
            Singletons.i().getPodcastDAO().update(podcast);
            EventBus.getDefault().post(new SubscriptionsChangedEvent());
        }

        @Override
        public void onFinish(Uri uri) {
            podcast.setLogoFilePath(uri.getPath());
            Singletons.i().getPodcastDAO().update(podcast);
            EventBus.getDefault().post(new SubscriptionsChangedEvent());
        }

        @Override
        public Uri getSource() {
            return Uri.parse(podcast.getLogoUrl());
        }

        @Override
        public String getDestinationDirType() {
            return Environment.DIRECTORY_PICTURES;
        }

        @Override
        public String getDestinationSubPath() {
            return String.format("%s/%s", removeUnwantedCharacters(podcast.getTitle()),
                                 removeUnwantedCharacters(new File(getSource().toString()).getName()));
        }

        @Override
        public String getTitle() {
            return podcast.getTitle();
        }

        @Override
        public String getDescription() {
            return String.format("Downloading podcast icon from podcast %s", podcast.getTitle());
        }

        @Override
        public int getNotificationVisibility() {
            return DownloadManager.Request.VISIBILITY_HIDDEN;
        }

        @Override
        public Object getObject() {
            return podcast;
        }

        private static String removeUnwantedCharacters(String path) {
            for (char unwantedChar : new char[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*', '=', ' ' }) {
                path = path.replace(unwantedChar, '_');
            }

            return path;
        }

    }

}
