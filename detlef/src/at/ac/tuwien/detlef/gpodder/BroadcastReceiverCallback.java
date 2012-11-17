package at.ac.tuwien.detlef.gpodder;

import at.ac.tuwien.detlef.callbacks.ReliableCallback;

public abstract class BroadcastReceiverCallback<Receiver, Event extends
BroadcastReceiverCallback.BroadcastReceiverEvent> extends ReliableCallback<Receiver, Event> {

    private Receiver rcv = null;

    protected Receiver getRcv() {
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
    public final void init() {
        /* nothing to do */
    }

    @Override
    public final void destroy() {
        /* nothing to do */
    }

    @Override
    protected final boolean isReady() {
        return rcv != null;
    }

    @Override
    protected void deliverEvent(Event e) {
        e.deliver();
    }

    final synchronized void sendEvent(Event e) {
        if (!isReady()) {
            queueEvent(e);
            return;
        }

        deliverEvent(e);
    }

    abstract static class BroadcastReceiverEvent {
        abstract void deliver();
    }
}
