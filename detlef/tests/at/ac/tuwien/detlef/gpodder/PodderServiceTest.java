package at.ac.tuwien.detlef.gpodder;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.FlakyTest;
import android.test.ServiceTestCase;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

/**
 * Tests the {@link PodderService}.
 * @author ondra
 */
public class PodderServiceTest extends ServiceTestCase<PodderService> {

    /** Fake application for these test cases. */
    private static class FakeApplication extends MockApplication {
    }

    /** Handles responses from the service. */
    private static class IncomingHandler extends Handler {
        /** Reference to the test instance. */
        private WeakReference<PodderServiceTest> wrpst;

        /**
         * Construct a handler.
         * @param pst The test instance for whom to handle responses.
         */
        public IncomingHandler(final PodderServiceTest pst) {
            wrpst = new WeakReference<PodderServiceTest>(pst);
        }

        @Override
        public void handleMessage(final Message msg) {
            Log.d("IncomingHandler", "handleMessage()");
            wrpst.get().msgWhat = msg.what;

            Bundle d = msg.getData();
            if (!d.containsKey(PodderService.MessageContentKey.REQCODE)) {
                Log.i("NOREQCODE", "message type is " + msg.what);
                fail("Message from service did not contain any request code.");
            }

            switch (msg.what) {
                case PodderService.MessageType.HTTP_DOWNLOAD_DONE:
                    wrpst.get().str = new String(msg.getData().getByteArray(
                            PodderService.MessageContentKey.DATA));
                    break;
                case PodderService.MessageType.HTTP_DOWNLOAD_FAILED:
                    fail("HTTP download failed: " + msg.getData().getString(
                            PodderService.MessageContentKey.ERRMSG));
                    break;
                case PodderService.MessageType.HEARTBEAT_DONE:
                    break;
                case PodderService.MessageType.AUTHCHECK_DONE:
                    break;
                case PodderService.MessageType.AUTHCHECK_FAILED:
                    fail("Auth check failed: " + msg.getData().getString(
                            PodderService.MessageContentKey.ERRMSG));
                    break;
                case PodderService.MessageType.HTTP_DOWNLOAD_PROGRESS_STATUS:
                    // ignore this message
                    return;
                default:
                    fail("unexpected message: " + msg.what);
                    break;
            }

            try {
                wrpst.get().lock.lock();
                wrpst.get().waiter.signal();
            } finally {
                wrpst.get().lock.unlock();
            }
        }
    }

    /** Lock required to use {@link #waiter}. */
    private final ReentrantLock lock;

    /** Quasi-semaphore to pause test execution while waiting for an answer. */
    private final Condition waiter;

    /** Messenger which receives responses from the service. */
    private final Messenger mess;

    /** Stores the message type of the latest response. */
    private int msgWhat;

    /** Stores the contents of the downloaded HTTP file. */
    private String str;

    /** Constructs a new PodderServiceTest. */
    public PodderServiceTest() {
        super(PodderService.class);
        Log.d("PodderServiceTest@" + this.hashCode(), "c'tor");

        lock = new ReentrantLock();
        waiter = lock.newCondition();
        mess = new Messenger(new IncomingHandler(this));
        msgWhat = -1;
        str = null;
        setApplication(new FakeApplication());
    }

    /**
     * Test whether the service can be started.
     */
    @SmallTest
    public final void testStart() {
        Log.d("PodderServiceTest@" + this.hashCode(), "testStart()");
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), PodderService.class);
        startService(startIntent);
        assertNotNull(getService());
    }

    /**
     * Performs a bind to the service, launching it if necessary.
     * @return A messenger to send a message to the service.
     */
    private Messenger performBind() {
        Log.d("PodderServiceTest@" + this.hashCode(), "performBind()");
        Intent bindIntent = new Intent();
        bindIntent.setClass(getContext(), PodderService.class);
        IBinder bdr = bindService(bindIntent);
        assertNotNull(bdr);
        return new Messenger(bdr);
    }

    /**
     * Test whether the service can be bound to.
     */
    @MediumTest
    public final void testBind() {
        Log.d("PodderServiceTest@" + this.hashCode(), "testBind()");
        performBind();
    }

    /**
     * Test whether the service can reply to a heartbeat.
     * @throws RemoteException Something went wrong communicating with the service.
     * @throws InterruptedException Waiting for the quasi-semaphore was interrupted.
     */
    @MediumTest
    public final void testHeartbeat() throws RemoteException, InterruptedException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testHeartbeat()");
        try {
            lock.lock();

            Messenger msr = performBind();
            Message msg = Message.obtain();
            msg.what = PodderService.MessageType.DO_HEARTBEAT;
            msg.replyTo = mess;
            msr.send(msg);

            waiter.await();
        } finally {
            lock.unlock();
        }

        assertEquals(msgWhat, PodderService.MessageType.HEARTBEAT_DONE);
    }

    /**
     * Test whether the service can download an HTTP file and return it.
     * @throws RemoteException Something went wrong communicating with the service.
     * @throws InterruptedException Waiting for the quasi-semaphore was interrupted.
     */
    @FlakyTest
    public final void testHttpDownload() throws RemoteException, InterruptedException {
        Log.d("PodderServiceTest@" + this.hashCode(), "testHttpDownload()");
        try {
            lock.lock();

            Bundle data = new Bundle();
            data.putString(
                    PodderService.MessageContentKey.URL,
                    "http://ondrahosek.dyndns.org/detlef.txt");

            Messenger msr = performBind();
            Message msg = Message.obtain();
            msg.what = PodderService.MessageType.DO_HTTP_DOWNLOAD;
            msg.setData(data);
            msg.replyTo = mess;
            msr.send(msg);

            waiter.await();
        } finally {
            lock.unlock();
        }

        assertEquals("Non, Detlef, je ne regrette rien.\n", str);
    }

    @FlakyTest
    public final void testGpodderAuth() throws RemoteException, InterruptedException {
        // FIXME: re-enable once our virtual GPodder instance is up and running
        if (false) {
            Log.d("PodderServiceTest@" + this.hashCode(), "testGpodderAuth()");
            try {
                lock.lock();

                Bundle data = new Bundle();
                data.putString(PodderService.MessageContentKey.USERNAME, "UnitTest");
                data.putString(PodderService.MessageContentKey.PASSWORD, "FahrenheitSucksCelsiusRules");
                data.putString(PodderService.MessageContentKey.HOSTNAME, "example.org");

                Messenger msr = performBind();
                Message msg = Message.obtain();
                msg.what = PodderService.MessageType.DO_AUTHCHECK;
                msg.setData(data);
                msg.replyTo = mess;
                msr.send(msg);

                waiter.await();
            } finally {
                lock.unlock();
            }
        }
    }
}
