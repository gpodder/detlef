package at.ac.tuwien.detlef.callbacks;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * Just as the name suggests, the ReliableCallback is a callback that makes sure all events it
 * receives are relayed to the receiver in the order in which they arrive.
 * 
 * To do this all events arriving while the receiver is unavailable are stored in a list (with
 * queueEvent()) and sent once the receiver calls registerReceiver() again.
 * 
 * @param <Receiver> The type of the receiver.
 * @param <Event> A type which encapsulates all data relevant to an event. These events are stored
 * with queueEvent() when the receiver is not ready and delivered with deliverEvent otherwise.
 */
public abstract class ReliableCallback<Receiver, Event> implements Callback<Receiver> {
    /** The List storing any not jet delivered events. */
    private final LinkedList<Event> pendingEvents = new LinkedList<Event>();

    /**
     * Sends any pending events.
     * 
     * This is simply here, so you don't have to write these three lines in the subclass.
     */
    @Override
    public void registerReceiver(Receiver rcv) {
        if (isReady()) {
            deliverPendingEvents();
        }
    }

    /**
     * This determines whether the receiver is ready for a new event or not.
     * 
     * If not, any arriving event should be queued.
     * 
     * @return True when the receiver is ready, false otherwise.
     */
    protected abstract boolean isReady();

    /**
     * Stores an event in the queue.
     * 
     * @param e The event to store.
     */
    protected void queueEvent(Event e) {
        synchronized (pendingEvents) {
            pendingEvents.add(e);
        }
    }

    /**
     * Delivers all pending events.
     * 
     * This should only be called after the receiver successfully called registerReceiver().
     */
    protected void deliverPendingEvents() {
        synchronized (pendingEvents) {
            Iterator<Event> i = pendingEvents.iterator();
            while (i.hasNext()) {
                deliverEvent(i.next());
                i.remove();
            }
        }
    }

    /**
     * Deliver the given event to the receiver.
     * 
     * @param e The event.
     */
    protected abstract void deliverEvent(Event e);
}
