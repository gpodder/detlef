package at.ac.tuwien.detlef.gpodder;

import android.app.Fragment;
import at.ac.tuwien.detlef.domain.DeviceId;

/**
 *
 */
public abstract class RegisterDeviceIdResultHandler<Receiver extends Fragment> extends
BroadcastReceiverCallback<Receiver, RegisterDeviceIdResultHandler.PodcastSyncEvent> {

    private DeviceId devideId;

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
        private final RegisterDeviceIdResultHandler<?> callback;

        PodcastSyncEvent(RegisterDeviceIdResultHandler<?> callback) {
            this.callback = callback;
        }

        public RegisterDeviceIdResultHandler<?> getCallback() {
            return callback;
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
            getCallback().handleFailure(exception);
        }

    }

    static class PodcastSyncEventSuccess extends PodcastSyncEvent {

        PodcastSyncEventSuccess(RegisterDeviceIdResultHandler<?> callback) {
            super(callback);
        }

        @Override
        void deliver() {
            getCallback().handle();
        }

    }

    public void setDeviceId(DeviceId pDeviceId) {
        devideId = pDeviceId;

    }

    public DeviceId getDeviceId() {
        return devideId;
    }

    
}
