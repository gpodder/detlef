package at.ac.tuwien.detlef.gpodder;

import android.app.Activity;

/**
 * A class to handle replies from the PullFeedAsyncTask.
 *
 * The Receiver has to be registered with registerReceiver() before use and unregistered with
 * unregisterReceiver() once the Receiver is destroyed.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * FeedSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class FeedSyncResultHandler<Receiver extends Activity> extends
        BroadcastReceiverCallback<Receiver, FeedSyncResultHandler.FeedSyncEvent> {

    /**
     * This has to be implemented by the user and is called when the Task is done.
     */
    public abstract void handle();

    /**
     * This has to be implemented by the user and is called when the Task encountered an error.
     * @param e An Exception describing what went wrong.
     */
    public abstract void handleFailure(GPodderException e);

    @Override
    protected void deliverEvent(final FeedSyncEvent e) {
        getRcv().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                e.deliver();
            }

        });
    }

    abstract static class FeedSyncEvent
    extends BroadcastReceiverCallback.BroadcastReceiverEvent {
        protected final FeedSyncResultHandler<?> callback;

        FeedSyncEvent(FeedSyncResultHandler<?> callback) {
            this.callback = callback;
        }
    }

    static class FeedSyncEventError extends FeedSyncEvent {
        private final GPodderException exception;

        FeedSyncEventError(FeedSyncResultHandler<?> callback,
                GPodderException exception) {
            super(callback);
            this.exception = exception;
        }

        @Override
        void deliver() {
            callback.handleFailure(exception);
        }

    }

    static class FeedSyncEventSuccess extends FeedSyncEvent {
        FeedSyncEventSuccess(FeedSyncResultHandler<?> callback) {
            super(callback);
        }

        @Override
        void deliver() {
            callback.handle();
        }

    }
}
