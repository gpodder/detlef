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


package at.ac.tuwien.detlef.download;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.download.DetlefDownloadManager.DownloadCallback;

public class DetlefDownloadManagerTest extends AndroidTestCase {

    private static final String url = "http://ondrahosek.dyndns.org/detlef.txt";
    private static final String podcastTitle = "TestPodcast";
    private static final String path = String.format("%s/%s", podcastTitle, new File(url).getName());

    private DetlefDownloadManager mgr;
    private final Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mgr = Singletons.i().getDownloadManager(mContext);

        getFile().delete();
    }

    private File getFile() {
        return new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), path);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEnqueue() throws IOException, InterruptedException {
        mgr.enqueue(new MockDownloadCallback());
        semaphore.acquire();

        File file = getFile();
        assertTrue(file.exists());
        assertTrue(file.isFile());
    }

    private class MockDownloadCallback implements DownloadCallback {

        @Override
        public void onStart(String path) {
            /* Nothing. */
        }

        @Override
        public void onCancel() {
            /* Nothing. */
        }

        @Override
        public void onError() {
            /* Nothing. */
        }

        @Override
        public void onFinish(Uri uri) {
            semaphore.release();
        }

        @Override
        public Uri getSource() {
            return Uri.parse(url);
        }

        @Override
        public String getDestinationDirType() {
            return Environment.DIRECTORY_MUSIC;
        }

        @Override
        public String getDestinationSubPath() {
            return String.format("%s/%s", podcastTitle, new File(url).getName());
        }

        @Override
        public String getTitle() {
            return podcastTitle;
        }

        @Override
        public String getDescription() {
            return String.format("Downloading episode from podcast %s", podcastTitle);
        }

        @Override
        public int getNotificationVisibility() {
            return DownloadManager.Request.VISIBILITY_VISIBLE;
        }

        @Override
        public Object getObject() {
            return null;
        }
    }
}
