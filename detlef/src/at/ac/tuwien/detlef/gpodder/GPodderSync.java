package at.ac.tuwien.detlef.gpodder;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceInterface;

/**
 * This class facilitates background HTTP and gpodder.net transactions.
 * @author ondra
 */
public class GPodderSync {
    /** Logging tag. */
    private static final String TAG = "GPodderSync";

    /** Activity on whose UI thread to perform callbacks. */
    private Activity activity;

    /** Manages the connection to the service. */
    private ConMan conMan;

    /** The interface to the service. */
    private volatile PodderServiceInterface iface;

    /** Handles responses from the service. */
    private IpcHandler responseHandler;

    /** Stores the next unused request code. */
    private int nextReqCode;

    /** Maps request codes to handlers. */
    private SparseArray<ResultHandler> reqs;

    /** Pauses while the service is being bound. */
    private Semaphore stoplight;

    /**
     * Constructs a GPodderSync instance.
     * @param act The activity that has placed this request. Required to make sure the callback is
     * called on this activity's UI thread.
     */
    public GPodderSync(Activity act) {
        Log.d(TAG, "GPodderSync");
        activity = act;
        conMan = new ConMan(this);
        iface = null;
        responseHandler = new IpcHandler(this);
        nextReqCode = 0;
        reqs = new SparseArray<ResultHandler>();
        stoplight = new Semaphore(1);
    }

    /**
     * Returns the next request code in sequence.
     * @return The next request code in sequence.
     */
    private int nextReqCode() {
        synchronized (this) {
            return nextReqCode++;
        }
    }

    /**
     * Requests that the service perform an HTTP download job.
     *
     * The service will perform an HTTP GET request on the given URL.
     *
     * @param url The URL of the file to download.
     * @param handler A handler for callbacks.
     */
    public void addHttpDownloadJob(String url, HttpDownloadResultHandler handler) {
        Log.d(TAG, "addHttpDownloadJob");

        // bind to the service
        assureBind();

        int reqCode = nextReqCode();
        try {
            iface.httpDownload(responseHandler, reqCode, url);
        } catch (RemoteException rex) {
            handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED, rex.toString());
            iface = null;
            return;
        }
        reqs.append(reqCode, handler);
    }

    /**
     * Requests that the service perform an authentication check job.
     *
     * The service will attempt to log into the given gpodder.net-compatible web service using the
     * given username and password and calls back whether this was successful or not.
     *
     * @param username User name to use for authentication check.
     * @param password Password to use for authentication check.
     * @param hostname Hostname of gpodder.net-compatible web service at which to attempt
     * authentication.
     * @param handler A handler for callbacks.
     */
    public void addAuthCheckJob(String username, String password, String hostname,
            NoDataResultHandler handler) {
        Log.d(TAG, "addAuthCheckJob");

        assureBind();

        int reqCode = nextReqCode();
        try {
            iface.authCheck(responseHandler, reqCode, username, password, hostname);
        } catch (RemoteException rex) {
            handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED, rex.toString());
            iface = null;
            return;
        }
        reqs.append(reqCode, handler);
    }

    /**
     * Assures that the service is bound.
     */
    private void assureBind() {
        Log.d(TAG, "assureBind");

        // take a ticket
        stoplight.acquireUninterruptibly();

        if (iface == null) {
            // we must bind
            Intent intent = new Intent();
            intent.setClass(activity, PodderService.class);
            activity.startService(intent);
            activity.bindService(intent, conMan, 0);

            // wait for another ticket
            stoplight.acquireUninterruptibly();
        }

        // return the ticket
        stoplight.release();
    }

    /**
     * Handles the connection the the service.
     * @author ondra
     */
    protected static class ConMan implements ServiceConnection {
        /** The GPodderSync to whom this connection manager belongs. */
        private WeakReference<GPodderSync> gps;

        /**
         * Constructs an instance of ConMan.
         * @param gposync {@link GPodderSync} to whom this connection manager belongs.
         */
        public ConMan(GPodderSync gposync) {
            gps = new WeakReference<GPodderSync>(gposync);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // freakin' finally
            gps.get().iface = PodderServiceInterface.Stub.asInterface(service);
            gps.get().stoplight.release();
        }

        public void onServiceDisconnected(ComponentName name) {
            // hmm, this is not good
            gps.get().iface = null;
        }
    }

    /** Handles incoming messages, mostly responses from the service. */
    protected static class IpcHandler extends PodderServiceCallback.Stub {
        /** The GPodderSync to whom this IPC handler belongs. */
        private WeakReference<GPodderSync> gps;

        public IpcHandler(GPodderSync gposync) {
            gps = new WeakReference<GPodderSync>(gposync);
        }

        public void authCheckFailed(int reqId, final int errCode, final String errStr)
                throws RemoteException {
            final ResultHandler rh = gps.get().reqs.get(reqId);

            gps.get().activity.runOnUiThread(new Runnable() {
                public void run() {
                    rh.handleFailure(errCode, errStr);
                }
            });
        }

        public void authCheckSucceeded(int reqId) throws RemoteException {
            final NoDataResultHandler ndrh = (NoDataResultHandler) gps.get().reqs.get(reqId);

            gps.get().activity.runOnUiThread(new Runnable() {
                public void run() {
                    ndrh.handleSuccess();
                }
            });
        }

        public void heartbeatSucceeded(int reqId) throws RemoteException {
            // FIXME: currently no external callback
        }

        public void httpDownloadFailed(int reqId, final int errCode, final String errStr)
                throws RemoteException {
            final HttpDownloadResultHandler hdrh =
                    (HttpDownloadResultHandler) gps.get().reqs.get(reqId);

            gps.get().activity.runOnUiThread(new Runnable() {
                public void run() {
                    hdrh.handleFailure(errCode, errStr);
                }
            });
        }

        public void httpDownloadProgress(int reqId, final int haveBytes, final int totalBytes)
                throws RemoteException {
            final HttpDownloadResultHandler hdrh =
                    (HttpDownloadResultHandler) gps.get().reqs.get(reqId);

            gps.get().activity.runOnUiThread(new Runnable() {
                public void run() {
                    hdrh.handleProgress(haveBytes, totalBytes);
                }
            });
        }

        public void httpDownloadSucceeded(int reqId, final ParcelableByteArray data)
                throws RemoteException {
            final HttpDownloadResultHandler hdrh =
                    (HttpDownloadResultHandler) gps.get().reqs.get(reqId);

            gps.get().activity.runOnUiThread(new Runnable() {
                public void run() {
                    hdrh.handleSuccess(data.getArray());
                }
            });
        }
    }
}
