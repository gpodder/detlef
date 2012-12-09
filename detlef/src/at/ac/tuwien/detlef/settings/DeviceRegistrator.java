package at.ac.tuwien.detlef.settings;

import at.ac.tuwien.detlef.domain.DeviceId;

/**
 * A device registrator registers a new {@link DeviceId} against a gpodder.net compatible
 * service.
 *
 * @author moe
 *
 */
public interface DeviceRegistrator {

    /**
     * Registers a new device at gpodder.net. This method is allowed to block, i.e. the
     * caller of this method has to take care about threading.
     *
     * @param deviceId The {@link DeviceId} that shall be registered.
     * @return Fluent Interface but does not return any particular status.
     * @throws DeviceRegistratorException Indicates that something went wrong during registration
     *     and that the registration process was not successful.
     */
    DeviceRegistrator registerNewDeviceId(DeviceId deviceId) throws DeviceRegistratorException;

}
