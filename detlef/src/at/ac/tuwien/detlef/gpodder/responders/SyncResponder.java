package at.ac.tuwien.detlef.gpodder.responders;

import java.lang.ref.WeakReference;

import android.content.ServiceConnection;
import android.os.RemoteException;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.PodderService;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;

/**
 * A prototype of a responder to answers from the {@link PodderService} and how they should be
 * forwarded to the user of {@link GPodderSync}.
 * @author ondra
 */
public abstract class SyncResponder extends PodderServiceCallback.Stub {

    /**
     * The GPodderSync parent.
     */
    private WeakReference<GPodderSync> gps;

    public SyncResponder() {
        gps = null;
    }

    protected GPodderSync getGps() {
        return gps.get();
    }

    /**
     * Sets the {@link GPodderSync} object which is used to manage the service connection.
     * Called by {@link GPodderSync} itself.
     * @param gpos The GPodderSync object to use.
     */
    public void setGpoSync(GPodderSync gpos) {
        gps = new WeakReference<GPodderSync>(gpos);
    }

    /**
     * Handles the case that an operation, any operation, failed.
     * @param reqId The request ID as passed by {@link GPodderSync}.
     * @param errCode The error code returned by the service.
     * @param errStr The error message returned by the service.
     */
    public abstract void handleGenericFailure(int reqId, int errCode, String errStr)
            throws RemoteException;

    /**
     * Handles the case that an operation that returns no data succeeded.
     * @param reqId The request ID as passed by {@link GPodderSync}.
     */
    public abstract void handleNoDataSuccess(int reqId)
            throws RemoteException;

    /**
     * Starts the service and initiates a bind to it.
     * @param sconn Service connection manager object, supplied by {@link GPodderSync}.
     */
    public abstract void startAndBindService(ServiceConnection sconn);

    /**
     * Stops the service.
     */
    public abstract void stopService();

    public void downloadPodcastListFailed(int reqId, int errCode, String errStr)
            throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }

    public void httpDownloadFailed(int reqId, int errCode, String errStr) throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }

    public void authCheckSucceeded(int reqId) throws RemoteException {
        handleNoDataSuccess(reqId);
    }

    public void httpDownloadToFileSucceeded(int reqId) throws RemoteException {
        handleNoDataSuccess(reqId);
    }

    public void gponetLoginFailed(int reqId, int errCode, String errStr) throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }

    @Override
    public void downloadChangesFailed(int reqId, int errCode, String errStr)
            throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }

    public void searchPodcastsFailed(int reqId, int errCode, String errStr) throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }

    @Override
    public void updateSubscriptionsFailed(int reqId, int errCode, String errStr)
            throws RemoteException {
        handleGenericFailure(reqId, errCode, errStr);
    }
}
