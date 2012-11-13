package at.ac.tuwien.detlef.gpodder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.client.HttpResponseException;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceInterface;

import com.dragontek.mygpoclient.simple.SimpleClient;

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

    /** HTTP Forbidden error code. */
    private static final int HTTP_STATUS_FORBIDDEN = 401;

    /** The inter-process communication handler. */
    private IpcHandler handler;

    /** Constructs a PodderService. */
    public PodderService() {
        Log.d(TAG, "PodderService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");
        handler = new IpcHandler();
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

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return handler;
    }

    /**
     * Performs an HTTP download.
     * @param cb HTTP callback for error cases.
     * @param reqId Request ID as passed by the remote caller.
     * @param url URL of the file to download.
     * @param handler HTTP download handler.
     * @return Whether the operation was successful.
     */
    protected static boolean performHttpDownload(PodderServiceCallback cb, int reqId, String url,
            HttpDownloadHandler hdh) throws RemoteException {
        Log.d(TAG, "performHttpDownload() on " + Thread.currentThread().getId());

        // fetch URL
        Uri uri = Uri.parse(url);
        if (!validScheme(uri.getScheme())) {
            Log.w(TAG, "invalid URI scheme");
            cb.httpDownloadFailed(reqId, ErrorCode.INVALID_URL_SCHEME,
                    "invalid URI scheme: " + uri.getScheme());
            return false;
        }

        // connect
        HttpURLConnection conn;
        InputStream strm;
        try {
            conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
        } catch (MalformedURLException mue) {
            Log.w(TAG, "malformed URL");
            cb.httpDownloadFailed(reqId, ErrorCode.MALFORMED_URL,
                    "malformed URL: " + uri.toString());
            return false;
        } catch (IOException ioe) {
            Log.w(TAG, "openConnection IOException: " + ioe.getMessage());
            cb.httpDownloadFailed(reqId, ErrorCode.IO_PROBLEM,
                    "I/O problem: " + ioe.getMessage());
            return false;
        }

        // fetch length
        int len = conn.getContentLength();

        // send over
        hdh.lengthKnown(len);

        // read
        int gotBytes = 0;
        byte[] holder = new byte[BLOCK_SIZE];
        try {
            strm = conn.getInputStream();
            int read;

            while ((read = strm.read(holder)) > 0) {
                gotBytes += read;
                if (!hdh.byteChunkDownloaded(holder, read)) {
                    // assume the handler has taken care of the right callback
                    return false;
                }
                cb.httpDownloadProgress(reqId, gotBytes, len);
            }
        } catch (IOException ioe) {
            Log.w(TAG, "read IOException: " + ioe.getMessage());
            cb.httpDownloadFailed(reqId, ErrorCode.IO_PROBLEM,
                    "I/O problem: " + ioe.getMessage());
            return false;
        } finally {
            conn.disconnect();
        }

        return true;
    }

    /**
     * Performs a login to a gpodder.net-compatible service.
     * @param cb Callback for error cases.
     * @param reqId Request ID as passed by the remote caller.
     * @param cinfo Information about the client of the gpodder.net-compatible service.
     * @return The gpodder.net client, or <tt>null</tt> if the login failed.
     */
    protected static SimpleClient performGpoLogin(PodderServiceCallback cb, int reqId,
            GpoNetClientInfo cinfo) throws RemoteException {

        SimpleClient sc = new SimpleClient(cinfo.getUsername(), cinfo.getPassword(),
                cinfo.getHostname());
        boolean ok;
        try {
            ok = sc.authenticate(cinfo.getUsername(), cinfo.getPassword());
        } catch (HttpResponseException hre) {
            Log.d(TAG, "performGpoLogin HttpResponseException: " + hre.getMessage());
            if (hre.getStatusCode() == HTTP_STATUS_FORBIDDEN) {
                // authentication simply failed
                cb.gponetLoginFailed(reqId, ErrorCode.AUTHENTICATION_FAILED,
                        "authentication failed");
            } else {
                Log.w(TAG, "performGpoLogin HTTP response " + hre.getStatusCode());
                cb.gponetLoginFailed(reqId, ErrorCode.UNEXPECTED_HTTP_RESPONSE,
                        "unexpected response " + hre.getStatusCode());
            }
            return null;
        } catch (IOException ioe) {
            Log.w(TAG, "performGpoLogin IOException: " + ioe.getMessage());
            cb.gponetLoginFailed(reqId, ErrorCode.IO_PROBLEM,
                    "I/O problem during authentication: " + ioe.getMessage());
            return null;
        }

        if (!ok) {
            cb.gponetLoginFailed(reqId, ErrorCode.AUTHENTICATION_FAILED, "authentication failed");
        }

        return sc;
    }

    /** Contains the error codes for failures reported by the {@link PodderService}. */
    public static class ErrorCode {
        /** Error code raised if authentication fails. */
        public static final int AUTHENTICATION_FAILED = 6;

        /** Error code raised if a file was not found. */
        public static final int FILE_NOT_FOUND = 7;

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

        /** Error code raised if an HTTP response with an unexpected code has been received. */
        public static final int UNEXPECTED_HTTP_RESPONSE = 9;

        /** Error code raised if the error is unknown. */
        public static final int UNKNOWN_ERROR = 8;
    }

    /**
     * Acts upon the whims of {@link PodderService#performHttpDownload(PodderServiceCallback,
     * String, HttpDownloadHandler)}.
     */
    protected interface HttpDownloadHandler {
        /**
         * Called either when the length of the file becomes known, or when it can only be
         * determined by the end of the stream.
         * @param len The length of the stream if known, or -1 if unknowable.
         * @throws RemoteException May be thrown if/when a callback fails.
         */
        void lengthKnown(int len) throws RemoteException;

        /**
         * Called when a chunk of bytes has been downloaded successfully.
         * @param chunk Chunk of bytes downloaded.
         * @param len Number of bytes downloaded (chunk might be larger for efficiency reasons).
         * @return Whether to continue downloading. If you return false, no further callback will
         * be sent and {@link PodderService#performHttpDownload(PodderServiceCallback, int, String,
         * HttpDownloadHandler)} will return false.
         * @throws RemoteException May be thrown if/when a callback fails.
         */
        boolean byteChunkDownloaded(byte[] chunk, int len) throws RemoteException;
    }

    /** Handles incoming messages, mostly requests from {@link GPodderSync}. */
    protected static class IpcHandler extends PodderServiceInterface.Stub {
        private static final String TAG = "PodderService.IpcHandler";

        public void authCheck(PodderServiceCallback cb, int reqId, GpoNetClientInfo cinfo)
                throws RemoteException {
            Log.d(TAG, "authCheck() on " + Thread.currentThread().getId());

            // try authenticating
            SimpleClient sc = performGpoLogin(cb, reqId, cinfo);

            if (sc != null) {
                cb.authCheckSucceeded(reqId);
            }
        }

        public void downloadPodcastList(PodderServiceCallback cb, int reqId, GpoNetClientInfo cinfo)
                throws RemoteException {
            Log.d(TAG, "downloadPodcastList() on " + Thread.currentThread().getId());

            // try authenticating
            SimpleClient sc = performGpoLogin(cb, reqId, cinfo);
            if (sc == null) {
                return;
            }

            List<String> casts;
            try {
                casts = sc.getSubscriptions(cinfo.getDeviceId());
            } catch (IOException ioe) {
                Log.w(TAG, "getSubscriptions IOException: " + ioe.getMessage());
                cb.downloadPodcastListFailed(reqId, ErrorCode.IO_PROBLEM,
                        "I/O problem: " + ioe.getMessage());
                return;
            }

            if (casts != null) {
                cb.downloadPodcastListSucceeded(reqId, casts);
            } else {
                cb.downloadPodcastListFailed(reqId, ErrorCode.UNKNOWN_ERROR,
                        "something went wrong");
            }
        }

        public void heartbeat(PodderServiceCallback cb, int reqId) throws RemoteException {
            Log.d(TAG, "heartbeat() on " + Thread.currentThread().getId());
            cb.heartbeatSucceeded(reqId);
        }

        public void httpDownload(PodderServiceCallback cb, int reqId, String url)
                throws RemoteException {
            Log.d(TAG, "httpDownload() on " + Thread.currentThread().getId());

            final ByteRope rope = new ByteRope();
            boolean ok = performHttpDownload(cb, reqId, url, new HttpDownloadHandler() {

                public void lengthKnown(int len) {
                    // do nothing of interest
                }

                public boolean byteChunkDownloaded(byte[] chunk, int len) {
                    rope.append(chunk, 0, len);
                    return true;
                }
            });

            if (ok) {
                // good news, everyone!
                ParcelableByteArray pba = new ParcelableByteArray(rope.toByteArray());
                cb.httpDownloadSucceeded(reqId, pba);
            }
        }

        public void httpDownloadToFile(final PodderServiceCallback cb, final int reqId, String url,
                String localfn) throws RemoteException {
            Log.d(TAG, "httpDownloadToFile() on " + Thread.currentThread().getId());

            // open fire
            final FileOutputStream fos;
            try {
                fos = new FileOutputStream(localfn);
            } catch (FileNotFoundException fnfe) {
                Log.w(TAG, "FileOutputStream c'tor FileNotFoundException: " + fnfe.getMessage());
                cb.httpDownloadFailed(reqId, ErrorCode.FILE_NOT_FOUND, "file not found");
                return;
            }

            boolean ok = performHttpDownload(cb, reqId, url, new HttpDownloadHandler() {

                public void lengthKnown(int len) {
                    // do nothing of interest
                }

                public boolean byteChunkDownloaded(byte[] chunk, int len) throws RemoteException {
                    try {
                        fos.write(chunk, 0, len);
                    } catch (IOException e) {
                        Log.w(TAG, "FileOutputStream write IOException: " + e.getMessage());
                        cb.httpDownloadFailed(reqId, ErrorCode.IO_PROBLEM, e.getMessage());
                        return false;
                    }
                    return true;
                }
            });

            // cease fire
            try {
                fos.close();
            } catch (IOException ioe) {
                if (ok) {
                    cb.httpDownloadFailed(reqId, ErrorCode.IO_PROBLEM, ioe.getMessage());
                    return;
                }
                // if !ok, we have other problems to worry about already
            }

            if (ok) {
                // phew.
                cb.httpDownloadToFileSucceeded(reqId);
            }
        }
    }
}
