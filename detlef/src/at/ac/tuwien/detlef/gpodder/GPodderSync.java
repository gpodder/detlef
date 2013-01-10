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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceInterface;
import at.ac.tuwien.detlef.gpodder.responders.SyncResponder;

/**
 * This class facilitates background HTTP and gpodder.net transactions.
 * @author ondra
 */
public class GPodderSync {
    /** Logging tag. */
    private static final String TAG = "GPodderSync";

    /** Manages the connection to the service. */
    private ConMan conMan;

    /** The interface to the service. */
    private volatile PodderServiceInterface iface;

    /** Handles responses from the service. */
    private SyncResponder syncResponder;

    /** Stores the next unused request code. */
    private int nextReqCode;

    /** Maps request codes to handlers. */
    private SparseArray<ResultHandler<?> > reqs;

    /** Pauses while the service is being bound. */
    private Semaphore stoplight;

    /** Information about this client of the gpodder.net-compatible service. */
    private GpoNetClientInfo clientInfo;
    
    /**
     * Each request has to be launched in a thread other than the main thread.
     * Otherwise the application would deadlock in assureBind() as the thread receiving the
     * ServiceConnection callback would be blocked.
     * 
     * Thus each request to the PodderService is started on this dispatcher.
     */
    private final ExecutorService requestDispatcher;

    /**
     * Constructs a GPodderSync instance.
     * @param sr The handler which will take care of any threading/synchronization concerns.
     */
    public GPodderSync(SyncResponder sr) {
        Log.d(TAG, "GPodderSync");
        conMan = new ConMan(this);
        iface = null;
        syncResponder = sr;
        syncResponder.setGpoSync(this);
        nextReqCode = 0;
        reqs = new SparseArray<ResultHandler<?> >();
        stoplight = new Semaphore(1);
        clientInfo = new GpoNetClientInfo();
        clientInfo.setHostname(
            DependencyAssistant.getDependencyAssistant().getGpodderSettings().getApiHostname()
        );
        requestDispatcher = Executors.newSingleThreadExecutor();
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
     * Sets the username used for access to gpodder.net-compatible services.
     * @param newUsername The new username.
     */
    public void setUsername(String newUsername) {
        clientInfo.setUsername(newUsername);
    }

    /**
     * Sets the password used for access to gpodder.net-compatible services.
     * @param newPassword The new password.
     */
    public void setPassword(String newPassword) {
        clientInfo.setPassword(newPassword);
    }

    /**
     * Sets the hostname of the gpodder.net-compatible service to use.
     * @param newHostname The hostname of the gpodder.net-compatible service to use.
     */
    public void setHostname(String newHostname) {
        clientInfo.setHostname(newHostname);
    }

    /**
     * Sets the device name used for access to gpodder.net-compatible services.
     * @param newDeviceName The new device name.
     */
    public void setDeviceName(String newDeviceName) {
        clientInfo.setDeviceId(newDeviceName);
    }

    /**
     * Remove the request with id reqId.
     * Synchronized against reqs.
     * @param reqId
     */
    public void removeReq(int reqId) {
        synchronized (reqs) {
            reqs.remove(reqId);
        }
    }

    /**
     * Return the ResultHandler for reqId.
     * Synchronized against reqs.
     * @param reqId
     * @return
     */
    public ResultHandler<?> getReq(int reqId) {
        synchronized (reqs) {
            return reqs.get(reqId);
        }
    }

    /**
     * Inserts the handler into reqs at reqCode.
     * Synchronized against reqs.
     * @param reqCode
     * @param handler
     */
    private void appendReq(int reqCode, ResultHandler<?> handler) {
        synchronized (reqs) {
            reqs.append(reqCode, handler);
        }
    }

    /**
     * Requests that the service perform an HTTP download job.
     *
     * The service will perform an HTTP GET request on the given URL.
     *
     * This type of HTTP download passes the downloaded data as a byte array to the callback. For
     * moderately huge files, you should use {@link #addHttpDownloadToFileJob(String, String,
     * NoDataResultHandler)}.
     *
     * @param url The URL of the file to download.
     * @param handler A handler for callbacks.
     */
    public void addHttpDownloadJob(final String url, final HttpDownloadResultHandler<?> handler) {
        Log.d(TAG, "addHttpDownloadJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                // bind to the service
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.httpDownload(syncResponder, reqCode, url);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                    return;
                }
                appendReq(reqCode, handler);
            }
        });
    }

    /**
     * Requests that the service perform an HTTP download-to-file job.
     *
     * The service will perform an HTTP GET request on the given URL and store the result into the
     * file at the given path. The file will be created if it doesn't exist and overwritten if it
     * does.
     *
     * To download small files without storing them in the file system first, you should use {@link
     * #addHttpDownloadJob(String, HttpDownloadResultHandler)}.
     *
     * @param url The URL of the file to download.
     * @param localfn The local file name into which to store the downloaded file.
     * @param handler A handler for callbacks.
     */
    public void addHttpDownloadToFileJob(final String url, final String localfn,
            final NoDataResultHandler<?> handler) {
        Log.d(TAG, "addHttpDownloadToFileJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.httpDownloadToFile(syncResponder, reqCode, url, localfn);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                    return;
                }
                appendReq(reqCode, handler);
            }
        });
    }

    /**
     * Requests that the service perform an authentication check job.
     *
     * The service will attempt to log into the given gpodder.net-compatible web service using the
     * specified username and password and calls back whether this was successful or not. Since this
     * is not a day-to-day operation, the stored credentials are neither used nor modified by this
     * method; the hostname, however, is.
     *
     * @param authUsername User name to use for authentication check.
     * @param authPassword Password to use for authentication check.
     * @param handler A handler for callbacks.
     */
    public void addAuthCheckJob(String authUsername, String authPassword,
            final NoDataResultHandler<?> handler) {
        Log.d(TAG, "addAuthCheckJob");

        final GpoNetClientInfo tempClientInfo = new GpoNetClientInfo();
        tempClientInfo.setHostname(clientInfo.getHostname());
        tempClientInfo.setUsername(authUsername);
        tempClientInfo.setPassword(authPassword);

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.authCheck(syncResponder, reqCode, tempClientInfo);
                } catch (RemoteException rex) {
                    handler.handleFailure(
                        PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                        rex.toString()
                    );
                    iface = null;
                    return;
                }
                appendReq(reqCode, handler);
            }
        });
    }

    /**
     * Requests that the service perform a podcast list download job.
     *
     * The service will log in using the credentials previously set by calls to {@link
     * #setUsername(String)} and {@link #setPassword(String)}.
     *
     * @param handler A handler for callbacks.
     */
    public void addDownloadPodcastListJob(final StringListResultHandler<?> handler) {
        Log.d(TAG, "addDownloadPodcastListJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.downloadPodcastList(syncResponder, reqCode, clientInfo);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                    return;
                }
                appendReq(reqCode, handler);
            }
        });
    }

    /**
     * Requests that the service performs a podcast search job.
     *
     * @param handler A handler for callbacks.
     * @param query The search query.
     */
    public void addSearchPodcastsJob(final PodcastListResultHandler<?> handler,
            final String query) {
        Log.d(TAG, "addSearchPodcastsJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.searchPodcasts(syncResponder, reqCode, clientInfo, query);
                    appendReq(reqCode, handler);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                }
            }
        });
    }

    public void addGetToplistJob(final PodcastListResultHandler<?> handler) {
        Log.d(TAG, "addGetToplistJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.getToplist(syncResponder, reqCode, clientInfo);
                    appendReq(reqCode, handler);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                }
            }
        });
    }

    public void addGetSuggestionsJob(final PodcastListResultHandler<?> handler) {
        Log.d(TAG, "addGetSuggestionsJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.getSuggestions(syncResponder, reqCode, clientInfo);
                    appendReq(reqCode, handler);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
                    iface = null;
                }
            }
        });
    }

    /**
     * Requests that the service performs a subscription update job.
     *
     * @param handler A handler for callbacks.
     * @param changes The changes to submit to the service.
     */
    public void addUpdateSubscriptionsJob(final PushSubscriptionChangesResultHandler<?> handler,
            final EnhancedSubscriptionChanges changes) {
        Log.d(TAG, "addUpdateSubscriptionsJob");

        requestDispatcher.execute(new Runnable() {
            @Override
            public void run() {
                assureBind();

                int reqCode = nextReqCode();
                try {
                    iface.updateSubscriptions(syncResponder, reqCode, clientInfo, changes);
                    appendReq(reqCode, handler);
                } catch (RemoteException rex) {
                    handler.handleFailure(PodderService.ErrorCode.SENDING_REQUEST_FAILED,
                            rex.toString());
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
}
