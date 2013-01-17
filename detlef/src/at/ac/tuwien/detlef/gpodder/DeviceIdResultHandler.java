package at.ac.tuwien.detlef.gpodder;

import at.ac.tuwien.detlef.domain.DeviceId;

/**
 *
 */
public interface DeviceIdResultHandler<Receiver> extends ResultHandler<Receiver> {

    /**
     * This has to be implemented by the user and is called when the Task got a subscription update.
     * @param changes The subscription changes.
     */
    void handleSuccess(DeviceId deviceId);

    static class DeviceIdSuccessEvent implements ResultEvent {
        private final DeviceIdResultHandler<?> cb;
        private final DeviceId deviceId;

        public DeviceIdSuccessEvent(DeviceIdResultHandler<?> cb, DeviceId deviceId) {
            this.cb = cb;
            this.deviceId = deviceId;
        }

        @Override
        public void deliver() {
            cb.handleSuccess(deviceId);
        }
    }
}
