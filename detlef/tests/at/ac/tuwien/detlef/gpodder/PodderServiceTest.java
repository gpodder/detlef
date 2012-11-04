package at.ac.tuwien.detlef.gpodder;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

    private static class FakeApplication extends MockApplication {
    }

    private static class IncomingHandler extends Handler {
        private WeakReference<PodderServiceTest> wrpst;

        public IncomingHandler(PodderServiceTest pst) {
            wrpst = new WeakReference<PodderServiceTest>(pst);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("IncomingHandler", "handleMessage()");
            try {
                wrpst.get().msgWhat = msg.what;

                wrpst.get().lock.lock();
                wrpst.get().waiter.signal();
            } finally {
                wrpst.get().lock.unlock();
            }
        }
    }

    private final ReentrantLock lock;
    private final Condition waiter;
    private final Messenger mess;
    private int msgWhat;

    public PodderServiceTest() {
        super(PodderService.class);
        Log.d("PodderServiceTest@" + this.hashCode(), "c'tor");
        lock = new ReentrantLock();
        waiter = lock.newCondition();
        mess = new Messenger(new IncomingHandler(this));
        msgWhat = -1;
        setApplication(new FakeApplication());
    }

    /**
     * Test whether the service can be started.
     */
    @SmallTest
    public void testStart() {
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
    public void testBind() {
        Log.d("PodderServiceTest@" + this.hashCode(), "testBind()");
        performBind();
    }

    /**
     * Test whether the service can reply to a heartbeat.
     */
    @MediumTest
    public void testHeartbeat() throws RemoteException, InterruptedException {
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
}
