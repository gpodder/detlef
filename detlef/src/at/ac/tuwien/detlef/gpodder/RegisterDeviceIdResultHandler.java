package at.ac.tuwien.detlef.gpodder;

import android.app.Activity;
import android.app.Fragment;

/**
 *
 */
public abstract class RegisterDeviceIdResultHandler<Receiver extends Fragment> extends
BroadcastReceiverCallback<Receiver, RegisterDeviceIdResultHandler.PodcastSyncEvent> {
    /** Logging tag. */
    private static final String TAG = RegisterDeviceIdResultHandler.class.getCanonicalName();

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
        getRcv().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                e.deliver();
            }

        });
    }

    abstract static class PodcastSyncEvent
    extends BroadcastReceiverCallback.BroadcastReceiverEvent {
        protected final RegisterDeviceIdResultHandler<?> callback;

        PodcastSyncEvent(RegisterDeviceIdResultHandler<?> callback) {
            this.callback = callback;
        }
    }

    static class PodcastSyncEventError extends PodcastSyncEvent {
        private final GPodderException exception;

        PodcastSyncEventError(RegisterDeviceIdResultHandler<?> callback,
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

        PodcastSyncEventSuccess(RegisterDeviceIdResultHandler<?> callback) {
            super(callback);
        }

        @Override
        void deliver() {
            callback.handle();
        }

    }
}
