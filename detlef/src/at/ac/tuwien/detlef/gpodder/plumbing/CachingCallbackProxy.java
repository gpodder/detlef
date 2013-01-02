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


package at.ac.tuwien.detlef.gpodder.plumbing;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.os.IBinder;
import android.os.RemoteException;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Caches calls to a callback object. If the call fails, the call is cached and can be re-sent,
 * optionally to a different callback object, on request.
 *
 * Note that no immediate delivery is attempted; the call is always placed in the queue. Right after
 * placing a call in the queue, an attempt is made to process the queue (as if {@link #resend()}
 * were called); this behavior can be changed using {@link #setPassive(boolean)}. (In passive mode,
 * the queue is only processed after explicit calls to {@link #resend()}).
 */
public class CachingCallbackProxy implements PodderServiceCallback {
    /** The target to proxy calls to. */
    private PodderServiceCallback target;

    /** A queue of messages to send. */
    private Queue<CachedCallback> queuedMessages;

    /** False if a re-send should be attempted anytime a new message is queued. */
    private boolean passive;

    /**
     * Construct a caching callback proxy.
     * @param cb The callback to which to forward requests.
     */
    public CachingCallbackProxy(PodderServiceCallback cb) {
        target = cb;
        queuedMessages = new LinkedList<CachedCallback>();
        passive = false;
    }

    /**
     * Set a new target for this proxy.
     */
    public void setTarget(PodderServiceCallback cb) {
        target = cb;
    }

    /**
     * Try re-sending queued messages.
     */
    public void resend() {
        Queue<CachedCallback> newQ = new LinkedList<CachedCallback>();
        CachedCallback ccb;

        while ((ccb = queuedMessages.poll()) != null) {
            if (!ccb.resend(target)) {
                newQ.add(ccb);
            }
        }

        // queue the rest for later
        queuedMessages = newQ;
    }

    /**
     * Try re-sending queued messages if passive is false.
     */
    private void resendUnlessPassive() {
        if (!passive && target != null) {
            resend();
        }
    }

    /**
     * Sets whether the proxy should not attempt processing queued messages until {@link #resend()}
     * is called, even if a new message is added.
     *
     * @param shouldBePassive Whether the proxy should be passive.
     */
    public void setPassive(boolean shouldBePassive) {
        passive = shouldBePassive;
    }

    /**
     * Whether the proxy should not attempt processing queued messages until {@link #resend()} is
     * called, even if a new message is added.
     * @return Whether the proxy should be passive.
     */
    public boolean isPassive() {
        return passive;
    }

    /**
     * Return how many messages are waiting.
     */
    public int countWaiting() {
        return queuedMessages.size();
    }

    @Override
    public IBinder asBinder() {
        return target.asBinder();
    }

    @Override
    public void httpDownloadSucceeded(final int reqId, final ParcelableByteArray data)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.httpDownloadSucceeded(reqId, data);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void httpDownloadToFileSucceeded(final int reqId) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.httpDownloadToFileSucceeded(reqId);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void httpDownloadFailed(final int reqId, final int errCode, final String errStr)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.httpDownloadFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void httpDownloadProgress(final int reqId, final int haveBytes, final int totalBytes)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.httpDownloadProgress(reqId, haveBytes, totalBytes);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void heartbeatSucceeded(final int reqId) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.heartbeatSucceeded(reqId);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void authCheckSucceeded(final int reqId) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.authCheckSucceeded(reqId);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void gponetLoginFailed(final int reqId, final int errCode, final String errStr)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.gponetLoginFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void downloadPodcastListSucceeded(final int reqId, final List<String> podcasts)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.downloadPodcastListSucceeded(reqId, podcasts);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void downloadPodcastListFailed(final int reqId, final int errCode, final String errStr)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.downloadPodcastListFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void downloadChangesSucceeded(final int reqId, final EnhancedSubscriptionChanges chgs)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.downloadChangesSucceeded(reqId, chgs);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void downloadChangesFailed(final int reqId, final int errCode, final String errStr)
            throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.downloadChangesFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void searchPodcastsSucceeded(final int reqId, final List<Podcast> results) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.searchPodcastsSucceeded(reqId, results);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void searchPodcastsFailed(final int reqId, final int errCode, final String errStr) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.searchPodcastsFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();

    }

    @Override
    public void getToplistSucceeded(final int reqId, final List<Podcast> results) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.getToplistSucceeded(reqId, results);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void getToplistFailed(final int reqId, final int errCode, final String errStr) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.getToplistFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void getSuggestionsSucceeded(final int reqId, final List<Podcast> results) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.getSuggestionsSucceeded(reqId, results);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void getSuggestionsFailed(final int reqId, final int errCode, final String errStr) throws RemoteException {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.getSuggestionsFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void updateSubscriptionsSucceeded(final int reqId, final long timestamp) {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.updateSubscriptionsSucceeded(reqId, timestamp);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    @Override
    public void updateSubscriptionsFailed(final int reqId, final int errCode, final String errStr) {
        queuedMessages.add(new CachedCallback() {
            @Override
            public boolean resend(PodderServiceCallback cb) {
                try {
                    cb.updateSubscriptionsFailed(reqId, errCode, errStr);
                } catch (RemoteException rex) {
                    return false;
                }
                return true;
            }
        });
        resendUnlessPassive();
    }

    /**
     * A callback cached for later processing.
     * @author ondra
     */
    protected interface CachedCallback {
        /**
         * Try to re-send this call to the given callback object.
         * @param cb The callback object to which to re-send the call.
         * @return Whether the re-sending was successfull
         */
        boolean resend(PodderServiceCallback cb);
    }
}
