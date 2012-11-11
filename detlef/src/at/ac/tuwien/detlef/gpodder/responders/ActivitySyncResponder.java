package at.ac.tuwien.detlef.gpodder.responders;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.HttpDownloadResultHandler;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
import at.ac.tuwien.detlef.gpodder.PodderService;
import at.ac.tuwien.detlef.gpodder.ResultHandler;
import at.ac.tuwien.detlef.gpodder.StringListResultHandler;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;

/**
 * Responds to callbacks from the {@link PodderService} by performing the specified actions on an
 * activity's main thread.
 * @author ondra
 */
public class ActivitySyncResponder extends SyncResponder {
    /**
     * The activity in whose context to spawn the service and on whose UI thread to launch
     * callbacks.
     */
    private Activity act;

    /**
     * Create a new Activity-based responder.
     * @param act The activity to which this responder is bound.
     */
    public ActivitySyncResponder(Activity act) {
        super();
        this.act = act;
    }

    public void httpDownloadSucceeded(int reqId, final ParcelableByteArray data)
            throws RemoteException {
        final HttpDownloadResultHandler hdrh =
                (HttpDownloadResultHandler) getGps().getReqs().get(reqId);

        act.runOnUiThread(new Runnable() {
            public void run() {
                hdrh.handleSuccess(data.getArray());
            }
        });

        getGps().getReqs().remove(reqId);
    }

    @Override
    public void handleNoDataSuccess(int reqId) throws RemoteException {
        final NoDataResultHandler ndrh =
                (NoDataResultHandler) getGps().getReqs().get(reqId);

        act.runOnUiThread(new Runnable() {
            public void run() {
                ndrh.handleSuccess();
            }
        });

        getGps().getReqs().remove(reqId);
    }

    @Override
    public void handleGenericFailure(int reqId, final int errCode, final String errStr)
            throws RemoteException {
        final ResultHandler rh = getGps().getReqs().get(reqId);

        act.runOnUiThread(new Runnable() {
            public void run() {
                rh.handleFailure(errCode, errStr);
            }
        });

        getGps().getReqs().remove(reqId);
    }

    public void httpDownloadProgress(int reqId, final int haveBytes, final int totalBytes)
            throws RemoteException {
        final HttpDownloadResultHandler hdrh =
                (HttpDownloadResultHandler) getGps().getReqs().get(reqId);

        act.runOnUiThread(new Runnable() {
            public void run() {
                hdrh.handleProgress(haveBytes, totalBytes);
            }
        });

        // don't remove request yet
    }

    public void heartbeatSucceeded(int reqId) throws RemoteException {
        // FIXME: currently no external callback
    }

    public void downloadPodcastListSucceeded(int reqId, final List<String> podcasts)
            throws RemoteException {
        final StringListResultHandler slrh =
                (StringListResultHandler) getGps().getReqs().get(reqId);

        act.runOnUiThread(new Runnable() {
            public void run() {
                slrh.handleSuccess(podcasts);
            }
        });
    }

    @Override
    public void startAndBindService(ServiceConnection sconn) {
        // bind the service from this activity
        Intent intent = new Intent();
        intent.setClass(act, PodderService.class);
        act.startService(intent);
        act.bindService(intent, sconn, 0);
    }

    @Override
    public void stopService() {
        Intent intent = new Intent();
        intent.setClass(act, PodderService.class);
        act.stopService(intent);
    }

}
