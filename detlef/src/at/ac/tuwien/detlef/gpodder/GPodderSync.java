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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceInterface;
import at.ac.tuwien.detlef.gpodder.responders.SyncResponder;

/**
 * This class facilitates background HTTP and gpodder.net transactions.
 *
 * @author ondra
 */
public class GPodderSync {
    /** Logging tag. */
    private static final String TAG = "GPodderSync";

    /** Manages the connection to the service. */
    private final ConMan conMan;

    /** The interface to the service. */
    private volatile PodderServiceInterface iface;

    /** Handles responses from the service. */
    private final SyncResponder syncResponder;

    /** Stores the next unused request code. */
    private int nextReqCode;

    /** Maps request codes to handlers. */
    private final SparseArray < ResultHandler<? >> reqs;

    /** Pauses while the service is being bound. */
    private final Semaphore stoplight;

    /** Information about this client of the gpodder.net-compatible service. */
    private final GpoNetClientInfo clientInfo;

    /**
     * Each request has to be launched in a thread other than the main thread.
     * Otherwise the application would deadlock in assureBind() as the thread
     * receiving the ServiceConnection callback would be blocked. Thus each
     * request to the PodderService is started on this dispatcher.
     */
    private final ExecutorService requestDispatcher;

    /**
     * Constructs a GPodderSync instance.
     *
     * @param sr The handler which will take care of any
     *            threading/synchronization concerns.
     */
    public GPodderSync(SyncResponder sr) {
        Log.d(TAG, "GPodderSync");
        conMan = new ConMan(this);
        iface = null;
        syncResponder = sr;
        syncResponder.setGpoSync(this);
        nextReqCode = 0;
        reqs = new SparseArray < ResultHandler<? >> ();
        stoplight = new Semaphore(1);
        clientInfo = new GpoNetClientInfo();
        clientInfo.setHostname(
            Singletons.i().getGpodderSettings().getApiHostname()
        );
        requestDispatcher = Executors.newSingleThreadExecutor();
    }

    /**
     * Returns the next request code in sequence.
     *
     * @return The next request code in sequence.
     */
    private int nextReqCode() {
        synchronized (this) {
            return nextReqCode++;
        }
    }

    /**
     * Sets the username used for access to gpodder.net-compatible services.
     *
     * @param newUsername The new username.
     */
    public void setUsername(String newUsername) {
        clientInfo.setUsername(newUsername);
    }

    /**
     * Sets the password used for access to gpodder.net-compatible services.
     *
     * @param newPassword The new password.
     */
    public void setPassword(String newPassword) {
        clientInfo.setPassword(newPassword);
    }

    /**
     * Sets the hostname of the gpodder.net-compatible service to use.
     *
     * @param newHostname The hostname of the gpodder.net-compatible service to
     *            use.
     */
    public void setHostname(String newHostname) {
        clientInfo.setHostname(newHostname);
    }

    /**
     * Sets the device name used for access to gpodder.net-compatible services.
     *
     * @param newDeviceName The new device name.
     */
    public void setDeviceName(String newDeviceName) {
        clientInfo.setDeviceId(newDeviceName);
    }

    /**
     * Remove the request with id reqId. Synchronized against reqs.
     *
     * @param reqId
     */
    public void removeReq(int reqId) {
        synchronized (reqs) {
            reqs.remove(reqId);
        }
    }

    /**
     * Return the ResultHandler for reqId. Synchronized against reqs.
     *
     * @param reqId
     * @return
     */
    public ResultHandler<?> getReq(int reqId) {
        synchronized (reqs) {
            return reqs.get(reqId);
        }
    }

    /**
     * Inserts the handler into reqs at reqCode. Synchronized against reqs.
     *
     * @param reqCode
     * @param handler
     */
    private void appendReq(int reqCode, ResultHandler<?> handler) {
        synchronized (reqs) {
            reqs.append(reqCode, handler);
        }
    }

    /**
     * Adds a "get podcast info" job for a specific URL.
     *
     * @param url The url to get the podcast info from.
     */
    public void addGetPodcastInfoJob(final PodcastResultHandler<?> handler,
                                     final List<String> urls) {
        Log.d(TAG, "addGetPodcastInfoJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.getPodcastInfo(syncResponder, reqCode, clientInfo, urls);
                    appendReq(reqCode, handler);
                } catch (RemoteException rex) {
                    Log.d(TAG, "getPodcastInfo failure");
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                                          rex.getMessage());
                    iface = null;
                }
            }
        });
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
            syncResponder.startAndBindService(conMan);

            // wait for another ticket
            stoplight.acquireUninterruptibly();
        }

        // return the ticket
        stoplight.release();
    }

    /**
     * Handles the connection the the service.
     *
     * @author ondra
     */
    private static class ConMan implements ServiceConnection {
        /** The GPodderSync to whom this connection manager belongs. */
        private final WeakReference<GPodderSync> gps;

        /**
         * Constructs an instance of ConMan.
         *
         * @param gposync {@link GPodderSync} to whom this connection manager
         *            belongs.
         */
        public ConMan(GPodderSync gposync) {
            gps = new WeakReference<GPodderSync>(gposync);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // freakin' finally
            gps.get().iface = PodderServiceInterface.Stub.asInterface(service);
            gps.get().stoplight.release();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // hmm, this is not good
            gps.get().iface = null;
        }
    }

    public GpoNetClientInfo getClientInfo() {
        return clientInfo;
    }
}
