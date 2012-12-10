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


package at.ac.tuwien.detlef.gpodder.responders;

import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.HttpDownloadResultHandler;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
import at.ac.tuwien.detlef.gpodder.PodcastListResultHandler;
import at.ac.tuwien.detlef.gpodder.PodderService;
import at.ac.tuwien.detlef.gpodder.PushSubscriptionChangesResultHandler;
import at.ac.tuwien.detlef.gpodder.ResultHandler;
import at.ac.tuwien.detlef.gpodder.StringListResultHandler;
import at.ac.tuwien.detlef.gpodder.SubscriptionChangesResultHandler;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;

/**
 * Responds to callbacks from the {@link PodderService} by simply executing them on any thread,
 * pausing the execution of another thread until such a callback is processed.
 * @author ondra
 */
public class SynchronousSyncResponder extends SyncResponder {
    /** Makes this whole shebang synchronous. */
    private Semaphore stoplight;

    /** The context in which the service should be started/stopped. */
    private Context ctx;

    /**
     * Constructs a synchronous responder.
     * @param context The context in which the service should be started/stopped.
     */
    public SynchronousSyncResponder(Context context) {
        stoplight = new Semaphore(0);
        ctx = context;
    }

    /**
     * Blocks until the service has responded.
     */
    public void waitForCompletion() {
        stoplight.acquireUninterruptibly();
    }

    /**
     * Blocks until the service has responded or the thread is interrupted.
     * @throws InterruptedException The thread has been interrupted while waiting.
     */
    public void waitInterruptiblyForCompletion() throws InterruptedException {
        stoplight.acquire();
    }

    public void httpDownloadSucceeded(int reqId, ParcelableByteArray data) throws RemoteException {
        final HttpDownloadResultHandler<?> hdrh =
                (HttpDownloadResultHandler<?>) getGps().getReq(reqId);
        hdrh.sendEvent(new HttpDownloadResultHandler.HttpSuccessEvent(hdrh, data.getArray()));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    public void httpDownloadProgress(int reqId, int haveBytes, int totalBytes)
            throws RemoteException {
        final HttpDownloadResultHandler<?> hdrh =
                (HttpDownloadResultHandler<?>) getGps().getReq(reqId);
        hdrh.sendEvent(new HttpDownloadResultHandler.HttpProgressEvent(hdrh, haveBytes,
                totalBytes));
        // don't remove request yet
        stoplight.release();
    }

    public void heartbeatSucceeded(int reqId) throws RemoteException {
        // FIXME: currently no external callback
    }

    public void downloadPodcastListSucceeded(int reqId, List<String> podcasts)
            throws RemoteException {
        final StringListResultHandler<?> slrh =
                (StringListResultHandler<?>) getGps().getReq(reqId);
        slrh.sendEvent(new StringListResultHandler.StringListSuccessEvent(slrh, podcasts));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void handleGenericFailure(int reqId, int errCode, String errStr) throws RemoteException {
        final ResultHandler<?> rh = getGps().getReq(reqId);
        rh.sendEvent(new ResultHandler.GenericFailureEvent(rh, errCode, errStr));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void handleNoDataSuccess(int reqId) throws RemoteException {
        final NoDataResultHandler<?> ndrh = (NoDataResultHandler<?>) getGps().getReq(reqId);
        ndrh.sendEvent(new NoDataResultHandler.NoDataSuccessEvent(ndrh));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void downloadChangesSucceeded(int reqId, final EnhancedSubscriptionChanges chgs)
            throws RemoteException {
        final SubscriptionChangesResultHandler<?> scrh =
                (SubscriptionChangesResultHandler<?>) getGps().getReq(reqId);
        scrh.sendEvent(new SubscriptionChangesResultHandler.SubscriptionChangesSuccessEvent(scrh,
                chgs));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void searchPodcastsSucceeded(int reqId, List<Podcast> results) throws RemoteException {
        final PodcastListResultHandler<?> plrh =
                (PodcastListResultHandler<?>) getGps().getReq(reqId);
        plrh.sendEvent(new PodcastListResultHandler.PodcastListSuccessEvent(plrh, results));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void getToplistSucceeded(int reqId, List<Podcast> results) throws RemoteException {
        final PodcastListResultHandler<?> plrh =
                (PodcastListResultHandler<?>) getGps().getReq(reqId);
        plrh.sendEvent(new PodcastListResultHandler.PodcastListSuccessEvent(plrh, results));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void getSuggestionsSucceeded(int reqId, List<Podcast> results) throws RemoteException {
        final PodcastListResultHandler<?> plrh =
                (PodcastListResultHandler<?>) getGps().getReq(reqId);
        plrh.sendEvent(new PodcastListResultHandler.PodcastListSuccessEvent(plrh, results));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void updateSubscriptionsSucceeded(int reqId, long timestamp) throws RemoteException {
        final PushSubscriptionChangesResultHandler<?> pscrh =
                (PushSubscriptionChangesResultHandler<?>) getGps().getReq(reqId);
        pscrh.sendEvent(new PushSubscriptionChangesResultHandler
                .PushSubscriptionChangesSuccessEvent(pscrh, timestamp, null));
        getGps().removeReq(reqId);
        stoplight.release();
    }

    @Override
    public void startAndBindService(ServiceConnection sconn) {
        Intent intent = new Intent();
        intent.setClass(ctx, PodderService.class);
        ctx.startService(intent);
        ctx.bindService(intent, sconn, 0);
    }

    @Override
    public void stopService() {
        Intent intent = new Intent();
        intent.setClass(ctx, PodderService.class);
        ctx.stopService(intent);
    }

}
