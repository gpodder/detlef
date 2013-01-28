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

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;

/**
 * EpisodePersistence contains static helper methods for handling
 * persistence-related functionalities related to episodes, such as download
 * management and episode file deletion.
 */
public final class PodcastImgPersistance {

    private static final String TAG = PodcastImgPersistance.class.getName();

    private PodcastImgPersistance() {
        /* Non-instantiable. */
    }

    /**
     * Begins downloading the current podcast image.
     */
    public static void download(Podcast podcast) throws IOException {
        try {
            getDownloadManager().enqueue(podcast);
        } catch (IOException e) {
            Log.e(TAG, "IOException while trying to download: ", e);
            throw e;
        }
    }

    /**
     * Cancels active downloads of the current podcast img (if one exists).
     */
    public static void cancelDownload(Podcast podcast) {
        getDownloadManager().cancel(podcast);
    }

    /**
     * Delete the specified podcast img file from disk. Automatically cancels
     * any ongoing downloads.
     */
    public static void delete(Podcast podcast) {
        cancelDownload(podcast);

        File file = new File(podcast.getLogoFilePath());
        file.delete();
    }

    private static DetlefDownloadManager getDownloadManager() {
        return Singletons.i().getDownloadManager(getContext());
    }

    private static Context getContext() {
        return Detlef.getAppContext();
    }

}
