package at.ac.tuwien.detlef.settings;

import at.ac.tuwien.detlef.domain.DeviceId;

/**
 * A device registrator registers a new {@link DeviceId} at gpodder.net.
 *
 * <p>
 *
 *
 * </p>
 *
 * @author moe
 *
 */
public interface DeviceRegistrator {

    /**
     * Register a new device at gpodder.net. This method is allowed to block, i.e. the
     * caller of this method has to take care about threading.
     *
     * @param deviceId
     * @return
     * @throws DeviceRegistratorException if
     */
    DeviceRegistrator registerNewDeviceId(DeviceId deviceId) throws DeviceRegistratorException;

}
