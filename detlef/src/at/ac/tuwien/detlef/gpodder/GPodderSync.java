package at.ac.tuwien.detlef.gpodder;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

/**
 * This class facilitates background HTTP and gpodder.net transactions.
 * @author ondra
 */
public class GPodderSync {
    /** Logging tag. */
    private static final String TAG = "PodderService";

    /** Activity on whose UI thread to perform callbacks. */
    private Activity activity;

    /** Manages the connection to the service. */
    private ConMan conMan;

    /** The messenger which handles incoming messages, mostly responses from the service. */
    private Messenger inMess;

    /** The messenger which handles outgoing messages to the service. */
    private volatile Messenger outMess;

    /** Stores the next unused request code. */
    private int nextReqCode;

    /** Maps request codes to handler pairs. */
    private SparseArray<MessageHandlerPair> reqs;

    /** The queue containing messages that must be processed once the bind has succeeded. */
    private Queue<Integer> msgQ;

    /**
     * Constructs a GPodderSync instance.
     * @param act The activity that has placed this request. Required to make sure the callback is
     * called on this activity's UI thread.
     */
    public GPodderSync(Activity act) {
        activity = act;
        inMess = new Messenger(new IncomingHandler(this));
        conMan = new ConMan(this);
        outMess = null;
        nextReqCode = 0;
        reqs = new SparseArray<MessageHandlerPair>();
        msgQ = new LinkedList<Integer>();
    }

    /**
     * Requests that the service perform an HTTP download job.
     * @param url URL of file to download.
     * @param handler Handler for callbacks.
     */
    public void addHttpDownloadJob(String url, HttpDownloadResultHandler handler) {
        // bind to the service
        if (!isBound()) {
            performBind();
        }

        // prepare message
        int reqCode;
        synchronized (this) {
            reqCode = nextReqCode++;
        }
        Message msg = Message.obtain();
        msg.replyTo = inMess;
        msg.what = PodderService.MessageType.DO_HTTP_DOWNLOAD;
        Bundle data = new Bundle();
        data.putString(PodderService.MessageContentKey.URL, url);
        data.putInt(PodderService.MessageContentKey.REQCODE, reqCode);
        msg.setData(data);

        registerAndSendMessage(msg, handler);
    }

    /**
     * Requests that the service perform an authentication check job.
     * @param username User name to use for authentication check.
     * @param password Password to use for authentication check.
     * @param hostname Hostname of gpodder.net-compatible web service.
     * @param handler
     */
    public void addAuthCheckJob(String username, String password, String hostname,
            AuthCheckResultHandler handler) {
        // bind to the service
        if (!isBound()) {
            performBind();
        }

        int reqCode;
        synchronized (this) {
            reqCode = nextReqCode++;
        }
        Message msg = Message.obtain();
        msg.replyTo = inMess;
        msg.what = PodderService.MessageType.DO_AUTHCHECK;
        Bundle data = new Bundle();
        data.putString(PodderService.MessageContentKey.USERNAME, username);
        data.putString(PodderService.MessageContentKey.PASSWORD, password);
        data.putString(PodderService.MessageContentKey.HOSTNAME, hostname);
        data.putInt(PodderService.MessageContentKey.REQCODE, reqCode);
        msg.setData(data);

        registerAndSendMessage(msg, handler);
    }

    /**
     * Returns whether the service is currently bound.
     * @return Whether the service is currently bound.
     */
    private boolean isBound() {
        return (outMess != null);
    }

    /**
     * Registers the message for later handling and sends it to the service.
     * @param message Message to send.
     * @param handler Handler to use to respond to problems or responses.
     */
    private void registerAndSendMessage(Message message, final ResultHandler handler) {
        MessageHandlerPair mhp = new MessageHandlerPair(message, handler);
        int reqCode = message.getData().getInt(PodderService.MessageContentKey.REQCODE);
        reqs.append(reqCode, mhp);

        if (isBound()) {
            try {
                outMess.send(message);
            } catch (final RemoteException rex) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        handler.handleFailure(
                                PodderService.MessageErrorCode.SENDING_REQUEST_FAILED,
                                rex.getMessage());
                    }
                });
                reqs.delete(reqCode);
            }
        } else {
            synchronized (this) {
                msgQ.add(reqCode);
            }
        }

    }

    /**
     * Processes the queue. Call once the service is bound.
     */
    private void processQueue() {
        for (;;) {
            int key;
            final MessageHandlerPair mhp;

            synchronized (this) {
                if (msgQ.isEmpty()) {
                    return;
                }

                key = msgQ.remove();
            }

            mhp = reqs.get(key);

            try {
                outMess.send(mhp.msg);
            } catch (final RemoteException rex) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        mhp.getH().handleFailure(
                                PodderService.MessageErrorCode.SENDING_REQUEST_FAILED,
                                rex.getMessage());
                    }
                });
                reqs.delete(key);
            }
        }
    }

    /**
     * Performs a bind to the service.
     */
    private void performBind() {
        Intent intent = new Intent();
        intent.setClass(activity, PodderService.class);
        activity.startService(intent);
        activity.bindService(intent, conMan, 0);
    }

    /**
     * Contains a message and a handler.
     * @author ondra
     */
    protected static class MessageHandlerPair {
        /** The message. */
        private Message msg;

        /** The result handler. */
        private ResultHandler h;

        public ResultHandler getH() {
            return h;
        }

        public void setH(ResultHandler h) {
            this.h = h;
        }

        public Message getMsg() {
            return msg;
        }

        public void setMsg(Message msg) {
            this.msg = msg;
        }

        /**
         * Constructs a message-handler pair.
         * @param message The message.
         * @param rhandler The handler.
         */
        public MessageHandlerPair(Message message, ResultHandler rhandler) {
            msg = message;
            h = rhandler;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result *= prime;
            if (h != null) {
                result += h.hashCode();
            }
            result *= prime;
            if (msg != null) {
                result += msg.hashCode();
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MessageHandlerPair other = (MessageHandlerPair) obj;
            if (h == null) {
                if (other.h != null) {
                    return false;
                }
            } else if (!h.equals(other.h)) {
                return false;
            }
            if (msg == null) {
                if (other.msg != null) {
                    return false;
                }
            } else if (!msg.equals(other.msg)) {
                return false;
            }
            return true;
        }

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
            gps.get().outMess = new Messenger(service);
            gps.get().processQueue();
        }

        public void onServiceDisconnected(ComponentName name) {
            // hmm, this is not good
            gps.get().outMess = null;
        }
    }

    /**
     * Handles incoming messages, mostly responses from the service.
     * @author ondra
     */
    protected static class IncomingHandler extends Handler {
        /** The GPodderSync to whom this connection manager belongs. */
        private WeakReference<GPodderSync> gps;

        /**
         * Constructs an instance of IncomingHandler.
         * @param gposync {@link GPodderSync} to whom this handler belongs.
         */
        public IncomingHandler(GPodderSync gposync) {
            gps = new WeakReference<GPodderSync>(gposync);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            int reqCode = data.getInt(PodderService.MessageContentKey.REQCODE);
            MessageHandlerPair mhp = gps.get().reqs.get(reqCode);
            Activity activity = gps.get().activity;

            switch (msg.what) {
                case PodderService.MessageType.HTTP_DOWNLOAD_DONE:
                {
                    final HttpDownloadResultHandler hdrh = (HttpDownloadResultHandler) mhp.h;
                    final byte[] bs = data.getByteArray(PodderService.MessageContentKey.DATA);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            hdrh.handleSuccess(bs);
                        }
                    });

                    gps.get().reqs.delete(reqCode);
                    break;
                }
                case PodderService.MessageType.HTTP_DOWNLOAD_FAILED:
                case PodderService.MessageType.AUTHCHECK_FAILED:
                {
                    final ResultHandler rh = mhp.h;
                    final int errCode = data.getInt(PodderService.MessageContentKey.ERRCODE);
                    final String errMsg = data.getString(PodderService.MessageContentKey.ERRMSG);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            rh.handleFailure(errCode, errMsg);
                        }
                    });
                    break;
                }
                case PodderService.MessageType.HTTP_DOWNLOAD_PROGRESS_STATUS:
                {
                    final HttpDownloadResultHandler hdrh = (HttpDownloadResultHandler) mhp.h;
                    final int haveBytes = data.getInt(PodderService.MessageContentKey.HAVEBYTES);
                    final int totalBytes = data.getInt(PodderService.MessageContentKey.TOTALBYTES);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            hdrh.handleProgress(haveBytes, totalBytes);
                        }
                    });
                    break;
                }
                case PodderService.MessageType.AUTHCHECK_DONE:
                {
                    final AuthCheckResultHandler acrh = (AuthCheckResultHandler) mhp.h;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            acrh.handleSuccess();
                        }
                    });
                    break;
                }
                default:
                    Log.e(TAG, "Unknown message type: " + msg.what);
            }
        }
    }
}
