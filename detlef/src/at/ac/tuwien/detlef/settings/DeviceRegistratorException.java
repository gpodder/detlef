package at.ac.tuwien.detlef.settings;

import at.ac.tuwien.detlef.domain.DeviceId;

/**
 * Exception that is thrown by {@link DeviceRegistrator} if something goes wrong while
 * registering a {@link DeviceId} at gpodder.net.
 */
public class DeviceRegistratorException extends Exception {

    /** Serial version UID. */
    private static final long serialVersionUID = 9184106121058752104L;
}
