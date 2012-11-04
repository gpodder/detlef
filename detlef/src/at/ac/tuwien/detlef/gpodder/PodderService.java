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
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

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

    /** Lists the allowed URI schemes. */
    private static final String[] ALLOWED_SCHEMES = { "http", "https" };

    /** Block size for the byte array when downloading data. */
    private static final int BLOCK_SIZE = 4096;

    /** This service's communication endpoint. "Talk to the hand." */
    private final Messenger theHand;

    /** Constructs a PodderService. */
    public PodderService() {
        theHand = new Messenger(new IncomingHandler(this));
    }

    /**
     * Checks whether the given scheme is allowed.
     * @param sch The scheme to check.
     * @return Whether the given scheme is allowed.
     */
    private static boolean validScheme(String sch) {
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
     * @param code The message code of this message.
     * @param errMsg The error description. For debugging purposes only.
     * @return A new message targeted at the sender of the given message.
     */
    private Message newFailedMessage(int code, String errMsg) {
        Message ret = Message.obtain();
        ret.what = code;
        ret.replyTo = this.theHand;

        Bundle data = new Bundle();
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
        try {
            recipient.send(msg);
        } catch (RemoteException e) {
            // it's "fire and forget"
        }
    }

    /**
     * Handles an HTTP download message.
     * @param msg The message that was sent.
     */
    private void handleHttpDownloadMessage(Message msg) {
        // fetch URL
        Bundle msgData = msg.getData();
        Uri uri = Uri.parse(msgData.getString("URL"));

        if (!validScheme(uri.getScheme())) {
            fireAndForget(msg.replyTo, newFailedMessage(MessageType.HTTP_DOWNLOAD_FAILED, "invalid URL scheme: " + uri.getScheme()));
            return;
        }

        // connect
        HttpURLConnection conn;
        InputStream strm;
        try {
            conn = (HttpURLConnection)new URL(uri.toString()).openConnection();
        } catch (MalformedURLException mue) {
            fireAndForget(msg.replyTo, newFailedMessage(MessageType.HTTP_DOWNLOAD_FAILED, "malformed URL"));
            return;
        } catch (IOException ioe) {
            fireAndForget(msg.replyTo, newFailedMessage(MessageType.HTTP_DOWNLOAD_FAILED, "I/O problem: " + ioe.getMessage()));
            return;
        }

        // read
        ByteRope br = new ByteRope();
        byte[] holder = new byte[BLOCK_SIZE];
        try {
            strm = conn.getInputStream();
            int read;

            while ((read = strm.read(holder)) > 0)
            {
                br.append(holder, 0, read);
            }
        } catch (IOException ioe) {
            fireAndForget(msg.replyTo, newFailedMessage(MessageType.HTTP_DOWNLOAD_FAILED, "I/O problem: " + ioe.getMessage()));
            return;
        } finally {
            conn.disconnect();
        }

        // prepare reply
        Message ret = Message.obtain();
        ret.what = MessageType.DO_HTTP_DOWNLOAD;
        ret.replyTo = this.theHand;
        Bundle data = new Bundle();
        data.putByteArray(MessageContentKey.DATA, br.toByteArray());
        ret.setData(data);

        // send reply
        try {
            msg.replyTo.send(ret);
        } catch (RemoteException re) {
            fireAndForget(msg.replyTo, newFailedMessage(MessageType.HTTP_DOWNLOAD_FAILED, "problem sending result: " + re.getMessage()));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
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

        /** A response that an HTTP download completed successfully. */
        public static final int HTTP_DOWNLOAD_DONE = 0x1001;

        /** A response that an HTTP download failed. */
        public static final int HTTP_DOWNLOAD_FAILED = 0x2001;
    }

    /** Contains the message content keys relevant to the {@link PodderService}. */
    public static class MessageContentKey {
        /** This key stores a URL string. */
        public static final String URL = "URL";

        /** This key stores an error message string. */
        public static final String ERRMSG = "ERRMSG";

        /** This key stores a data byte array. */
        public static final String DATA = "DATA";
    }

    /** Handles incoming messages. */
    protected static class IncomingHandler extends Handler {
        /**
         * Weak reference to the Podder Service manipulated by this object. This reference is weak
         * to prevent a cycle between {@link PodderService} and {@link IncomingHandler}.
         */
        private WeakReference<PodderService> srv;

        /**
         * Constructs a new {@link IncomingHandler} for the given {@link PodderService}.
         * @param ps The {@link PodderService} to which this handler belongs.
         */
        public IncomingHandler(PodderService ps) {
            srv = new WeakReference<PodderService>(ps);
        }

        @Override
        public void handleMessage(Message msg) {
            PodderService ps = srv.get();
            if (ps == null) {
                // they're all gone. we've been abandoned.
                return;
            }

            if (msg.replyTo == null) {
                // well screw you then
                // FIXME: log or throw exception
                return;
            }

            switch (msg.what) {
                case MessageType.DO_HTTP_DOWNLOAD:
                    ps.handleHttpDownloadMessage(msg);
                    break;
                default:
                    // I do not know this message
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
