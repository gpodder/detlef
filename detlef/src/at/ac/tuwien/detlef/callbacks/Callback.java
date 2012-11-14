package at.ac.tuwien.detlef.callbacks;


/**
 * An Interface for callbacks.
 * 
 * This is used to help handle Activity-lifecycles and UI-callbacks. The callback's receiver
 * (usually an Activity) has to call registerReceiver() to let the callback know that it may deliver
 * events and where to deliver them. In case the receiver Object may become invalid (i.e. the
 * Activity is destroyed due to a configuration change) it has to call unregisterReceiver() first.
 * It is up to the implementation of the callback whether or not events arriving while the
 * receiver is not available are delivered when registerReceiver() is called.
 * 
 * The Receiver will usually extend the callback to do the actual work. An implementing class should
 * always be in its own file or a static inner class. Otherwise an instance will retain a pointer
 * to its enclosing class during creation.
 *
 * @param <Receiver> The type of the receiver. Usually this will be an Activity.
 */
public interface Callback<Receiver> {
    /**
     * Tell the callback that the receiver is now available and pass a reference to it.
     * @param rcv The receiver.
     */
    void registerReceiver(Receiver rcv);

    /**
     * Tell the callback that it can't access the receiver now.
     * 
     * It is up to the specific implementation what happens with events arriving while the receiver
     * is unavailable.
     */
    void unregisterReceiver();

    /**
     * This should be called before the callback is first used.
     * 
     * This method performs some initialization which only needs to be done once.
     */
    void init();

    /**
     * This should be called when the callback is no longer used.
     * 
     * This method usually cleans up what has been done in init().
     */
    void destroy();
}
