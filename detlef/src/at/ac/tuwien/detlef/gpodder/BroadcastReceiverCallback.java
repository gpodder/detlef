package at.ac.tuwien.detlef.gpodder;

import at.ac.tuwien.detlef.callbacks.ReliableCallback;

/**
 * This is a convenience class designed to make it easier to implement a ReliableCallback.
 * 
 * It allows the implementor to access the receiver via the getRcv() method.
 * 
 * Creation or cleanup code (init() and destroy()) is not allowed. The register-,
 * unregisterReceiver() and isReady() methods are already fully implemented. An event can
 * be sent to the callback via sendEvent(), where it is queued or delivered accordingly.
 * 
 * To make things more modular the Event may implement it's own deliver() method, which is
 * called upon delivery.
 * 
 * @param <Receiver> The type of receiver.
 * @param <Event> The type of event.
 */
public abstract class BroadcastReceiverCallback<Receiver, Event extends
BroadcastReceiverCallback.BroadcastReceiverEvent> extends ReliableCallback<Receiver, Event> {

    private Receiver rcv = null;

    /**
     * Returns the currently registered receiver or null.
     * @return
     */
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

    /**
     * Order the callback to deliver the event e.
     * 
     * If the receiver is not ready, the event is queued.
     * 
     * @param e The event to deliver.
     */
    final synchronized void sendEvent(Event e) {
        if (!isReady()) {
            queueEvent(e);
            return;
        }

        deliverEvent(e);
    }

    abstract static class BroadcastReceiverEvent {
        /**
         * Called upon delivery.
         */
        abstract void deliver();
    }
}
