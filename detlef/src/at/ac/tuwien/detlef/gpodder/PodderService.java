package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

/**
 * GPodder download service; performs gpodder.net requests and HTTP downloads in the background.
 *
 * When bound, returns an {@link android.os.IBinder IBinder} which can be turned into a {@link
 * android.os.Messenger Messenger}. This messenger accepts the <tt>DO_</tt> message codes from
 * {@link MessageType} and responds with the respective <tt>_DONE</tt> or <tt>_FAILED</tt> message
 * codes. Messages without a {@link android.os.Message#replyTo replyTo} attribute are ignored.
 *
 * @author ondra
 */
public class PodderService extends Service {
    private static final String TAG = "PodderService";

    /** Lists the allowed URI schemes. */
    private static final String[] ALLOWED_SCHEMES = { "http", "https" };

    /** Block size for the byte array when downloading data. */
    private static final int BLOCK_SIZE = 4096;

    /** This service's communication endpoint. "Talk to the hand." */
    private Messenger theHand;

    /** Constructs a PodderService. */
    public PodderService() {
        Log.d(TAG, "PodderService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");
        HandlerThread ht = new HandlerThread(
                "PodderServiceHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
        ht.start();
        theHand = new Messenger(new IncomingHandler(this, ht.getLooper()));
    }

    /**
     * Checks whether the given scheme is allowed.
     * @param sch The scheme to check.
     * @return Whether the given scheme is allowed.
     */
    private static boolean validScheme(String sch) {
        Log.d(TAG, "validScheme()");
        if (sch == null) {
            return false;
        } else {
            for (String scheme : ALLOWED_SCHEMES) {
                if (scheme.equals(sch)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new "action failed" message.
     * @param msgCode The message code of this message.
     * @param reqCode The request code of the failed request.
     * @param errCode The error code documenting what went wrong.
     * @param errMsg The error description. For debugging purposes only.
     * @return A new message targeted at the sender of the given message.
     */
    private Message newFailedMessage(int msgCode, int reqCode, int errCode, String errMsg) {
        Log.d(TAG, "newFailedMessage()");
        Message ret = Message.obtain();
        ret.what = msgCode;
        ret.replyTo = this.theHand;

        Bundle data = new Bundle();
        data.putInt(MessageContentKey.REQCODE, reqCode);
        data.putInt(MessageContentKey.ERRCODE, errCode);
        data.putString(MessageContentKey.ERRMSG, errMsg);
        ret.setData(data);

        return ret;
    }

    /**
     * Sends a message to a specific receiver and ignores failures.
     * @param recipient The recipient of the message.
     * @param msg The message to send to the recipient.
     */
    private void fireAndForget(Messenger recipient, Message msg) {
        Log.d(TAG, "fireAndForget()");
        try {
            recipient.send(msg);
        } catch (RemoteException e) {
            // it's "fire and forget"
        }
    }

    /**
     * Fetches the request code ({@link MessageContentKey#REQCODE}) from the message.
     * @param msgData
     * @return
     */
    private int fetchRequestCode(Bundle msgData) {
        if (msgData == null || !msgData.containsKey(MessageContentKey.REQCODE)) {
            return -1;
        } else {
            return msgData.getInt(MessageContentKey.REQCODE);
        }
    }

    /**
     * Handles an HTTP download message.
     * @param msg The message that was sent.
     */
    private void handleHttpDownloadMessage(Message msg) {
        Log.d(TAG, "handleHttpDownloadMessage()");
        // fetch request code
        Bundle msgData = msg.getData();
        int reqCode = fetchRequestCode(msgData);

        // fetch URL
        Uri uri = Uri.parse(msgData.getString("URL"));

        if (!validScheme(uri.getScheme())) {
            fireAndForget(msg.replyTo, newFailedMessage(
                    MessageType.HTTP_DOWNLOAD_FAILED,
                    reqCode,
                    MessageErrorCode.INVALID_URL_SCHEME,
                    "invalid URL scheme: " + uri.getScheme()));
            return;
        }

        // connect
        HttpURLConnection conn;
        InputStream strm;
        try {
            conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
        } catch (MalformedURLException mue) {
            fireAndForget(msg.replyTo, newFailedMessage(
                    MessageType.HTTP_DOWNLOAD_FAILED,
                    reqCode,
                    MessageErrorCode.MALFORMED_URL,
                    "malformed URL"));
            return;
        } catch (IOException ioe) {
            fireAndForget(msg.replyTo, newFailedMessage(
                    MessageType.HTTP_DOWNLOAD_FAILED,
                    reqCode,
                    MessageErrorCode.IO_PROBLEM,
                    "I/O problem: " + ioe.getMessage()));
            return;
        }

        // fetch length
        int len = conn.getContentLength();

        // prepare status update message
        Message statusMsg = Message.obtain();
        statusMsg.what = MessageType.HTTP_DOWNLOAD_PROGRESS_STATUS;
        statusMsg.replyTo = this.theHand;
        Bundle statusData = new Bundle();
        statusData.putInt(MessageContentKey.REQCODE, reqCode);
        if (len != -1) {
            statusData.putInt(MessageContentKey.TOTALBYTES, len);
        }

        // read
        ByteRope br = new ByteRope();
        byte[] holder = new byte[BLOCK_SIZE];
        try {
            strm = conn.getInputStream();
            int read;

            while ((read = strm.read(holder)) > 0) {
                br.append(holder, 0, read);
                statusData.putInt(MessageContentKey.HAVEBYTES, br.length());
                statusMsg.setData(statusData);
                fireAndForget(msg.replyTo, statusMsg);
            }
        } catch (IOException ioe) {
            fireAndForget(msg.replyTo, newFailedMessage(
                    MessageType.HTTP_DOWNLOAD_FAILED,
                    reqCode,
                    MessageErrorCode.IO_PROBLEM,
                    "I/O problem: " + ioe.getMessage()));
            return;
        } finally {
            conn.disconnect();
        }

        // prepare reply
        Message ret = Message.obtain();
        ret.what = MessageType.HTTP_DOWNLOAD_DONE;
        ret.replyTo = this.theHand;
        Bundle data = new Bundle();
        data.putByteArray(MessageContentKey.DATA, br.toByteArray());
        data.putInt(MessageContentKey.REQCODE, reqCode);
        ret.setData(data);

        // send reply
        try {
            msg.replyTo.send(ret);
        } catch (RemoteException re) {
            fireAndForget(msg.replyTo, newFailedMessage(
                    MessageType.HTTP_DOWNLOAD_FAILED,
                    reqCode,
                    MessageErrorCode.SENDING_RESULT_FAILED,
                    "problem sending result: " + re.getMessage()));
            return;
        }
    }

    /**
     * Handles a heartbeat.
     * @param msg The message that was sent.
     */
    private void handleHeartbeatMessage(Message msg) {
        Log.d(TAG, "handleHeartbeatMessage()");

        int reqCode = fetchRequestCode(msg.getData());
        Message ret = Message.obtain();
        ret.what = MessageType.HEARTBEAT_DONE;
        ret.replyTo = this.theHand;
        Bundle data = new Bundle();
        data.putInt(MessageContentKey.REQCODE, reqCode);
        msg.setData(data);

        fireAndForget(msg.replyTo, ret);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return theHand.getBinder();
    }

    /** Contains the message types relevant to the {@link PodderService}. */
    public static class MessageType {
        /**
         * A request that an HTTP download be performed.
         *
         * The message data must contain a String value keyed {@link MessageContentKey#URL} which
         * stores the HTTP(S) URL of the file to download.
         */
        public static final int DO_HTTP_DOWNLOAD = 0x0001;

        /** A request that a heartbeat be provided. */
        public static final int DO_HEARTBEAT = 0x0002;

        /**
         * A response that an HTTP download completed successfully.
         *
         * Contains a byte array keyed {@link MessageContentKey#DATA} which contains the data
         * downloaded.
         */
        public static final int HTTP_DOWNLOAD_DONE = 0x1001;

        /** The response to the heartbeat. */
        public static final int HEARTBEAT_DONE = 0x1002;

        /**
         * A response informing about the progress of an HTTP download.
         *
         * Contains an integer keyed {@link MessageContentKey#HAVEBYTES} storing the number of
         * bytes that have been downloaded, as well as&mdash;if it is known&mdash;an integer keyed
         * {@link MessageContentKey#TOTALBYTES} storing the total number of bytes.
         */
        public static final int HTTP_DOWNLOAD_PROGRESS_STATUS = 0x1003;

        /** A response that an HTTP download failed. */
        public static final int HTTP_DOWNLOAD_FAILED = 0x2001;
    }

    /** Contains the message content keys relevant to the {@link PodderService}. */
    public static class MessageContentKey {
        /** This key stores a data byte array. */
        public static final String DATA = "DATA";

        /** This key stores an error code integer. */
        public static final String ERRCODE = "ERRCODE";

        /** This key stores an error message string. */
        public static final String ERRMSG = "ERRMSG";

        /**
         * This key stores a request code integer that is chosen by the requester and returned in
         * responses. The magical value -1 is used in replies when a request code was not specified
         * by the caller.
         */
        public static final String REQCODE = "REQCODE";

        /** This key stores an integer with the number of bytes already downloaded. */
        public static final String HAVEBYTES = "HAVEBYTES";

        /** This key stores an integer with the total number of bytes. */
        public static final String TOTALBYTES = "TOTALBYTES";

        /** This key stores a URL string. */
        public static final String URL = "URL";
    }

    /** Contains the error codes for failures reported by the {@link PodderService}. */
    public static class MessageErrorCode {
        /** Error code raised if the URL scheme is not allowed. */
        public static final int INVALID_URL_SCHEME = 1;

        /** Error code raised if there has been a problem with input/output. */
        public static final int IO_PROBLEM = 3;

        /** Error code raised if the URL is formatted incorrectly. */
        public static final int MALFORMED_URL = 2;

        /**
         * Error code raised if sending the request failed. This code is not sent by the service,
         * but may be sent by the plumbing layer (e.g. {@link GPodderSync}) if the message to the
         * service cannot be sent.
         */
        public static final int SENDING_REQUEST_FAILED = 5;

        /** Error code raised if sending the result failed. */
        public static final int SENDING_RESULT_FAILED = 4;
    }

    /** Handles incoming messages. */
    protected static class IncomingHandler extends Handler {
        private static final String TAG = "PodderService.IncomingHandler";

        /**
         * Weak reference to the Podder Service manipulated by this object. This reference is weak
         * to prevent a cycle between {@link PodderService} and {@link IncomingHandler}.
         */
        private WeakReference<PodderService> srv;

        /**
         * Constructs a new {@link IncomingHandler} for the given {@link PodderService}.
         * @param ps The {@link PodderService} to which this handler belongs.
         * @param looper The {@link Looper} binding this handler to a thread.
         */
        public IncomingHandler(PodderService ps, Looper looper) {
            super(looper);
            Log.d(TAG, "IncomingHandler()");
            srv = new WeakReference<PodderService>(ps);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage()");

            PodderService ps = srv.get();
            if (ps == null) {
                // they're all gone. we've been abandoned.
                Log.e(TAG, "PodderService reference was nulled");
                return;
            }

            if (msg.replyTo == null) {
                // well screw you then
                Log.e(TAG, "message has no replyTo attribute");
                return;
            }

            switch (msg.what) {
                case MessageType.DO_HTTP_DOWNLOAD:
                    ps.handleHttpDownloadMessage(msg);
                    break;
                case MessageType.DO_HEARTBEAT:
                    ps.handleHeartbeatMessage(msg);
                    break;
                default:
                    // I do not know this message
                    Log.e(TAG, "unknown message " + msg.what);
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
