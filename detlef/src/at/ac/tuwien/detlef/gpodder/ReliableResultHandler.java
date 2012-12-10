package at.ac.tuwien.detlef.gpodder;

import android.os.Bundle;
import at.ac.tuwien.detlef.callbacks.ReliableCallback;

public abstract class ReliableResultHandler<Receiver> extends ReliableCallback<Receiver,
ResultHandler.ResultEvent> implements ResultHandler<Receiver> {
    private Receiver rcv = null;

    /** A bundle to pass extra data. */
    private Bundle bundle = new Bundle();

    /**
     * Returns the currently registered receiver or null.
     * @return
     */
    protected final Receiver getRcv() {
        return rcv;
    }

    @Override
    public final synchronized void registerReceiver(Receiver rcv) {
        if (isReady()) {
            unregisterReceiver();
        }

        this.rcv = rcv;

        super.registerReceiver(rcv);
    }

    @Override
    public final synchronized void unregisterReceiver() {
        if (isReady()) {
            rcv = null;
        }
    }

    @Override
    public void init() {
        /* nothing to do */
    }

    @Override
    public void destroy() {
        /* nothing to do */
    }

    /**
     * Order the callback to deliver the event e.
     * 
     * If the receiver is not ready, the event is queued.
     * 
     * @param e The event to deliver.
     */
    @Override
    public final synchronized void sendEvent(ResultEvent e) {
        if (!isReady()) {
            queueEvent(e);
            return;
        }

        deliverEvent(e);
    }

    @Override
    protected final boolean isReady() {
        return rcv != null;
    }

    @Override
    protected void deliverEvent(ResultEvent e) {
        e.deliver();
    }

    public ReliableResultHandler<Receiver> setBundle(Bundle pBundle) {
        bundle = pBundle;
        return this;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
