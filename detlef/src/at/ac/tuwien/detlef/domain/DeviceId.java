package at.ac.tuwien.detlef.domain;

import java.util.regex.Pattern;

/**
 * Represents a gpodder.net device id. This class is guaranteed to be consistent and immutable,
 * i.e. no instance can exist that that is not a formally valid device id representation and
 * no instance may change its device id during its lifetime.
 *
 * <p>From the
 * <a href="http://wiki.gpodder.org/wiki/Web_Services/API_2">Gpodder API 2 Spec</a>:
 * </p>
 *
 * <p><cite>"Devices are used throughout the API to identify a device / a client application.
 * A device ID can be any string matching the regular expression "[\w.-]+". The client
 * application MUST generate a string to be used as its device ID, and SHOULD ensure that it
 * is unique within the user account. A good approach is to combine the application name and
 * the name of the host it is running on."</cite></p>
 *
 * <p><cite>"If two applications share a device ID, this might cause subscriptions to be
 * overwritten on the server side. While it is possible to retrieve a list of devices and
 * their IDs from the server, this SHOULD NOT be used to let a user select an existing device
 * ID."</cite></p>
 *
 * @author moe
 *
 */
public class DeviceId {

    /**
     * The regular expression that determines if a given device id is valid or not.
     */
    public static final String DEVICE_ID_REGEX = "[\\w.-]+";

    private final String deviceId;

    /**
     *
     * @param pDeviceId This device's id that must match the format {@value #DEVICE_ID_REGEX}.
     * @throws IllegalArgumentException If pDeviceId is not a valid device id.
     *
     */
    public DeviceId(String pDeviceId) {

        if (!Pattern.matches(DEVICE_ID_REGEX, pDeviceId)) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid device id provided: %s does not match pattern %s",
                    pDeviceId,
                    DEVICE_ID_REGEX
                )
            );
        }

        deviceId = pDeviceId;
    }

    /**
     * @return The device id that was set in {@link #DeviceId(String)} and is guaranteed to match
     *     {@value #DEVICE_ID_REGEX}.
     */
    public String toString() {
        return deviceId;
    }
}
