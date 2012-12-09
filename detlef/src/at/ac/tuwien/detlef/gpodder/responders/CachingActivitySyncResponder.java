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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
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
import at.ac.tuwien.detlef.gpodder.ResultHandler;
import at.ac.tuwien.detlef.gpodder.StringListResultHandler;
import at.ac.tuwien.detlef.gpodder.SubscriptionChangesResultHandler;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;

/**
 * Responds to callbacks from the {@link PodderService} by performing the
 * specified actions on an activity's main thread. Allows re-linking to a new
 * activity if the previous one disappears unexpectedly.
 *
 * @author ondra
 */
public class CachingActivitySyncResponder extends SyncResponder {
    /**
     * The activity on whose UI thread to launch callbacks.
     */
    private WeakReference<Activity> act;

    /**
     * The context in which to start the service.
     */
    private Context ctx;

    /**
     * List of waiting callbacks.
     */
    private Queue<Runnable> waitings;

    /**
     * Create a new caching, Activity-based responder.
     *
     * @param ctx The context in which to start and stop the service. The
     *            general application context is recommended here.
     * @param act The activity on whose main thread the callbacks will be
     *            executed.
     */
    public CachingActivitySyncResponder(Context ctx, Activity act) {
        super();
        this.act = new WeakReference<Activity>(act);
        this.ctx = ctx;
        this.waitings = new ArrayDeque<Runnable>();
    }

    /**
     * Unregister the current activity from receiving callbacks.
     */
    public void unregisterActivity() {
        act.clear();
    }

    /**
     * Register a new activity to receive callbacks.
     *
     * @param a The activity on whose main thread the callbacks will be executed
     *            from now on.
     */
    public void registerActivity(Activity a) {
        act = new WeakReference<Activity>(a);
    }

    /**
     * Deliver the pending callbacks to the currently-registered activity.
     */
    public void deliverPendingCallbacks() {
        if (act.get() == null) {
            return;
        }

        while (!waitings.isEmpty()) {
            Runnable r = waitings.remove();
            act.get().runOnUiThread(r);
        }
    }

    @Override
    public void httpDownloadSucceeded(int reqId, final ParcelableByteArray data)
            throws RemoteException {
        final HttpDownloadResultHandler<?> hdrh =
                (HttpDownloadResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                hdrh.handleSuccess(data.getArray());
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void handleNoDataSuccess(int reqId) throws RemoteException {
        final NoDataResultHandler<?> ndrh =
                (NoDataResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                ndrh.handleSuccess();
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void handleGenericFailure(int reqId, final int errCode, final String errStr)
            throws RemoteException {
        final ResultHandler<?> rh = getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                rh.handleFailure(errCode, errStr);
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void httpDownloadProgress(int reqId, final int haveBytes, final int totalBytes)
            throws RemoteException {
        final HttpDownloadResultHandler<?> hdrh =
                (HttpDownloadResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                hdrh.handleProgress(haveBytes, totalBytes);
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        // don't remove request yet
    }

    @Override
    public void heartbeatSucceeded(int reqId) throws RemoteException {
        // FIXME: currently no external callback
    }

    @Override
    public void downloadPodcastListSucceeded(int reqId, final List<String> podcasts)
            throws RemoteException {
        final StringListResultHandler<?> slrh =
                (StringListResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                slrh.handleSuccess(podcasts);
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void downloadChangesSucceeded(int reqId, final EnhancedSubscriptionChanges chgs)
            throws RemoteException {
        final SubscriptionChangesResultHandler<?> scrh =
                (SubscriptionChangesResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                scrh.handleSuccess(chgs);
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void searchPodcastsSucceeded(int reqId, final List<Podcast> results)
            throws RemoteException {
        final PodcastListResultHandler<?> plrh =
                (PodcastListResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                plrh.handleSuccess(results);
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void updateSubscriptionsSucceeded(int reqId) throws RemoteException {
        final NoDataResultHandler<?> ndrh = (NoDataResultHandler<?>) getGps().getReq(reqId);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                ndrh.handleSuccess();
            }
        };

        if (act.get() == null) {
            waitings.add(r);
        } else {
            act.get().runOnUiThread(r);
        }

        getGps().removeReq(reqId);
    }

    @Override
    public void startAndBindService(ServiceConnection sconn) {
        // bind the service from the application
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
