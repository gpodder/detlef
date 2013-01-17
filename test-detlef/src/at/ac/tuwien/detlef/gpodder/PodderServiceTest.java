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


package at.ac.tuwien.detlef.gpodder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.test.FlakyTest;
import android.test.ServiceTestCase;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceInterface;

/**
 * Tests the {@link PodderService}.
 * @author ondra
 */
public class PodderServiceTest extends ServiceTestCase<PodderService> {

    /** Fake application for these test cases. */
    private static class FakeApplication extends MockApplication {
    }

    private static final int RESPONDED_AUTHCHECK = 0;
    private static final int RESPONDED_HEARTBEAT = 1;
    private static final int RESPONDED_HTTP_DOWNLOAD = 2;
    private static final int RESPONDED_HTTP_DOWNLOAD_TO_FILE = 3;
    private static final int RESPONDED_DOWNLOAD_PODCAST_LIST = 4;
    private static final int RESPONDED_DOWNLOAD_CHANGES = 5;
    private static final int RESPONDED_PODCAST_SEARCH = 6;
    private static final int RESPONDED_SUBSCRIPTION_UPDATE = 7;
    private static final int RESPONDED_GET_SUGGESTIONS = 8;
    private static final int RESPONDED_GET_TOPLIST = 9;

    /** Handles responses from the service. */
    private static class IncomingHandler extends PodderServiceCallback.Stub {

        /** Reference to the test instance. */
        private final WeakReference<PodderServiceTest> wrpst;

        /**
         * Construct a handler.
         * @param pst The test instance for whom to handle responses.
         */
        public IncomingHandler(final PodderServiceTest pst) {
            wrpst = new WeakReference<PodderServiceTest>(pst);
        }

        @Override
        public void gponetLoginFailed(int reqId, int errCode, String errStr) throws RemoteException {
            fail("Login failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void authCheckSucceeded(int reqId) throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_AUTHCHECK;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void heartbeatSucceeded(int reqId) throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_HEARTBEAT;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void httpDownloadFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("HTTP download failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void httpDownloadProgress(int reqId, int haveBytes, int totalBytes)
                throws RemoteException {
            // ignore this message
        }

        @Override
        public void httpDownloadSucceeded(int reqId, ParcelableByteArray data)
                throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_HTTP_DOWNLOAD;
            wrpst.get().str = new String(data.getArray());
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void httpDownloadToFileSucceeded(int reqId) throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_HTTP_DOWNLOAD_TO_FILE;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void downloadPodcastListFailed(int reqId, int errCode,
                String errStr) throws RemoteException {
            fail("podcast list download failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void downloadPodcastListSucceeded(int reqId, List<String> podcasts)
                throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_DOWNLOAD_PODCAST_LIST;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void downloadChangesFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("podcast changes download failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void downloadChangesSucceeded(int reqId,
                EnhancedSubscriptionChanges chgs) throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_DOWNLOAD_CHANGES;
            wrpst.get().stoplight.release();
        }

        @Override
        public void searchPodcastsSucceeded(int reqId, List<Podcast> results)
                throws RemoteException {
            wrpst.get().podcasts = results;
            wrpst.get().msgWhat = RESPONDED_PODCAST_SEARCH;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void searchPodcastsFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("podcast search failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void getToplistSucceeded(int reqId, List<Podcast> results)
                throws RemoteException {
            wrpst.get().podcasts = results;
            wrpst.get().msgWhat = RESPONDED_GET_TOPLIST;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void getToplistFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("toplist retrieval failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void getSuggestionsSucceeded(int reqId, List<Podcast> results)
                throws RemoteException {
            wrpst.get().podcasts = results;
            wrpst.get().msgWhat = RESPONDED_GET_SUGGESTIONS;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void getSuggestionsFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("suggestion retrieval failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void updateSubscriptionsSucceeded(int reqId, long timestamp) throws RemoteException {
            wrpst.get().msgWhat = RESPONDED_SUBSCRIPTION_UPDATE;
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void updateSubscriptionsFailed(int reqId, int errCode, String errStr)
                throws RemoteException {
            fail("subscription update failed: " + errStr);
            wrpst.get().reqId = reqId;
            wrpst.get().stoplight.release();
        }

        @Override
        public void getPodcastInfoSucceeded(int reqId, List<Podcast> result) throws RemoteException {
            /* Unused. */
        }

        @Override
        public void getPodcastInfoFailed(int reqId, int ioProblem, String message)
                throws RemoteException {
            /* Unused. */
        }
    }

    /** Semaphore to pause test execution while waiting for an answer. */
    private final Semaphore stoplight;

    /** Handles responses from the service. */
    private final IncomingHandler handler;

    /** Stores the message type of the latest response. */
    private int msgWhat;

    /** Stores the request ID of the latest response. */
    private int reqId;

    /** Stores a random number generator. */
    private final Random rng;

    /** Stores the contents of the downloaded HTTP file. */
    private String str;

    /** Stores the result of the podcast search. */
    private List<Podcast> podcasts;

    /** Constructs a new PodderServiceTest. */
    public PodderServiceTest() {
        super(PodderService.class);
        Log.d("PodderServiceTest@" + this.hashCode(), "c'tor");

        stoplight = new Semaphore(0);
        handler = new IncomingHandler(this);
        msgWhat = -1;
        reqId = -1;
        rng = new Random();
        str = null;
        setApplication(new FakeApplication());
    }

    /**
     * Test whether the service can be started.
     */
    @SmallTest
    public final void testStart() {
        Log.d("PodderServiceTest@" + this.hashCode(), "testStart()");

        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), PodderService.class);
        startService(startIntent);
        assertNotNull(getService());
    }

    /**
     * Performs a bind to the service, launching it if necessary.
     * @return A messenger to send a message to the service.
     */
    private PodderServiceInterface performBind() {
        Log.d("PodderServiceTest@" + this.hashCode(), "performBind()");

        Intent bindIntent = new Intent();
        bindIntent.setClass(getContext(), PodderService.class);
        IBinder bdr = bindService(bindIntent);
        assertNotNull(bdr);
        return PodderServiceInterface.Stub.asInterface(bdr);
    }

    /**
     * Test whether the service can be bound to.
     */
    @MediumTest
    public final void testBind() {
        Log.d("PodderServiceTest@" + this.hashCode(), "testBind()");
        performBind();
    }

    /**
     * Test whether the service can reply to a heartbeat.
     * @throws RemoteException Something went wrong communicating with the service.
     * @throws InterruptedException Waiting for the quasi-semaphore was interrupted.
     */
    @MediumTest
    public final void testHeartbeat() throws RemoteException, InterruptedException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testHeartbeat()");

        PodderServiceInterface psi = performBind();
        int rid = rng.nextInt();

        psi.heartbeat(handler, rid);

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_HEARTBEAT, msgWhat);
        assertEquals(rid, reqId);
    }

    /**
     * Test whether the service can download an HTTP file and return it.
     * @throws RemoteException Something went wrong communicating with the service.
     * @throws InterruptedException Waiting for the quasi-semaphore was interrupted.
     */
    @FlakyTest
    public final void testHttpDownload() throws RemoteException, InterruptedException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testHttpDownload()");

        PodderServiceInterface psi = performBind();
        int rid = rng.nextInt();

        psi.httpDownload(handler, rid, "http://ondrahosek.dyndns.org/detlef.txt");

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_HTTP_DOWNLOAD, msgWhat);
        assertEquals(rid, reqId);
        assertEquals("Non, Detlef, je ne regrette rien.\n", str);
    }

    /**
     * Test whether the service can download an HTTP file into a local file.
     * @throws RemoteException Something went wrong communicating with the service.
     * @throws InterruptedException Waiting for the quasi-semaphore was interrupted.
     */
    @FlakyTest
    public final void testHttpDownloadToFile() throws RemoteException, InterruptedException,
    IOException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testHttpDownloadToFile()");

        PodderServiceInterface psi = performBind();
        int rid = rng.nextInt();
        ByteRope br = new ByteRope();

        File f = File.createTempFile("httpdown", ".tmp", null);
        try {
            psi.httpDownloadToFile(handler, rid, "http://ondrahosek.dyndns.org/detlef.txt",
                    f.getAbsolutePath());

            stoplight.acquireUninterruptibly();

            assertEquals(RESPONDED_HTTP_DOWNLOAD_TO_FILE, msgWhat);
            assertEquals(rid, reqId);

            // read the file again
            FileInputStream fis = new FileInputStream(f);
            try {
                byte[] holder = new byte[1024];
                int read;
                while ((read = fis.read(holder, 0, holder.length)) >= 0) {
                    br.append(holder, 0, read);
                }
            } finally {
                fis.close();
            }
        } finally {
            f.delete();
        }

        String quoi = new String(br.toByteArray());

        assertEquals("Non, Detlef, je ne regrette rien.\n", quoi);
    }

    /**
     * Test whether the podcast search successfully returns the solid steel podcast
     * when searching for "solid steel".
     * @throws RemoteException
     */
    @SmallTest
    public final void testPodcastSearchWithResult() throws RemoteException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testPodcastSearch()");

        PodderServiceInterface psi = performBind();
        GpoNetClientInfo ci = getClientInfo();

        int rid = rng.nextInt();
        psi.searchPodcasts(handler, rid, ci, "solid+steel");

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_PODCAST_SEARCH, msgWhat);
        assertEquals(rid, reqId);

        /* Ensure the results contain the solid steel podcast. */

        boolean containsPodcast = false;
        for (Podcast p : podcasts) {
            if (p.getTitle().equals("Solid Steel")) {
                containsPodcast = true;
                break;
            }
        }

        assertTrue("Search results do not contain expected podcast", containsPodcast);
    }

    /**
     * Test whether the toplist retrieval returns results successfully.
     * @throws RemoteException
     */
    @SmallTest
    public final void testToplistRetrieval() throws RemoteException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testToplistRetrieval()");

        PodderServiceInterface psi = performBind();
        GpoNetClientInfo ci = getClientInfo();

        int rid = rng.nextInt();
        psi.getToplist(handler, rid, ci);

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_GET_TOPLIST, msgWhat);
        assertEquals(rid, reqId);
        assertTrue(!podcasts.isEmpty());
    }

    /**
     * Test whether the suggestion retrieval returns results successfully.
     * @throws RemoteException
     */
    @SmallTest
    public final void testSuggestionRetrieval() throws RemoteException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testSuggestionRetrieval()");

        PodderServiceInterface psi = performBind();
        GpoNetClientInfo ci = getClientInfo();

        int rid = rng.nextInt();
        psi.getSuggestions(handler, rid, ci);

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_GET_SUGGESTIONS, msgWhat);
        assertEquals(rid, reqId);
        assertTrue(!podcasts.isEmpty());
    }

    /**
     * Test whether the podcast search successfully returns without result.
     * @throws RemoteException
     */
    @SmallTest
    public final void testPodcastSearchWithoutResult() throws RemoteException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testPodcastSearchWithoutResult()");

        PodderServiceInterface psi = performBind();
        GpoNetClientInfo ci = getClientInfo();

        int rid = rng.nextInt();
        psi.searchPodcasts(handler, rid, ci, "as;ldfhas.dsda");

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_PODCAST_SEARCH, msgWhat);
        assertEquals(rid, reqId);
        assertTrue("Search results are not empty", podcasts.isEmpty());
    }

    /**
     * Test whether the subscription update successfully adds a podcast.
     * @throws RemoteException
     */
    @SmallTest
    public final void testUpdateSubscriptionAddPodcast() throws RemoteException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testUpdateSubscriptionAddPodcast()");

        PodderServiceInterface psi = performBind();
        GpoNetClientInfo ci = getClientInfo();

        List<Podcast> podcasts = new ArrayList<Podcast>();
        podcasts.add(new Podcast().setUrl("http://this.is.a.test.com"));
        EnhancedSubscriptionChanges changes = new EnhancedSubscriptionChanges(podcasts, new ArrayList<Podcast>(), 0);

        int rid = rng.nextInt();
        psi.updateSubscriptions(handler, rid, ci, changes);

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_SUBSCRIPTION_UPDATE, msgWhat);
        assertEquals(rid, reqId);
    }

    private GpoNetClientInfo getClientInfo() {
        GpoNetClientInfo ci = new GpoNetClientInfo();

        ci.setUsername("detlef");
        ci.setPassword("detlef");
        ci.setHostname("gpodder.net");

        return ci;
    }

    @FlakyTest
    public final void testGpodderAuth() throws RemoteException, InterruptedException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testGpodderAuth()");

        PodderServiceInterface psi = performBind();
        int rid = rng.nextInt();
        GpoNetClientInfo ci = getClientInfo();

        psi.authCheck(handler, rid, ci);

        stoplight.acquireUninterruptibly();

        assertEquals(RESPONDED_AUTHCHECK, msgWhat);
        assertEquals(rid, reqId);
    }
}
