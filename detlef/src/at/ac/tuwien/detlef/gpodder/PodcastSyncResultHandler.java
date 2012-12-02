package at.ac.tuwien.detlef.gpodder;

import android.app.Activity;

/**
 * A class to handle replies from the PullSubscriptionsAsyncTask.
 *
 * The Receiver has to be registered with registerReceiver() before use and unregistered with
 * unregisterReceiver() once the Receiver is destroyed.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * PodcastSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class PodcastSyncResultHandler<Receiver extends Activity> extends
        BroadcastReceiverCallback<Receiver, PodcastSyncResultHandler.PodcastSyncEvent> {

    /**
     * This has to be implemented by the user and is called when the Task got a subscription update.
     * @param changes The subscription changes.
     */
    public abstract void handle();

    /**
     * This has to be implemented by the user and is called when the Task encountered an error.
     * @param e An Exception describing what went wrong.
     */
    public abstract void handleFailure(GPodderException e);

    @Override
    protected void deliverEvent(final PodcastSyncEvent e) {
        getRcv().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                e.deliver();
            }

        });
    }

    abstract static class PodcastSyncEvent
    extends BroadcastReceiverCallback.BroadcastReceiverEvent {
        protected final PodcastSyncResultHandler<?> callback;

        PodcastSyncEvent(PodcastSyncResultHandler<?> callback) {
            this.callback = callback;
        }
    }

    static class PodcastSyncEventError extends PodcastSyncEvent {
        private final GPodderException exception;

        PodcastSyncEventError(PodcastSyncResultHandler<?> callback,
                GPodderException exception) {
            super(callback);
            this.exception = exception;
        }

        @Override
        void deliver() {
            callback.handleFailure(exception);
        }

    }

    static class PodcastSyncEventSuccess extends PodcastSyncEvent {

        PodcastSyncEventSuccess(PodcastSyncResultHandler<?> callback) {
            super(callback);
        }

        @Override
        void deliver() {
            callback.handle();
        }

    }
}
