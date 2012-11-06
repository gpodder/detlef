package at.ac.tuwien.detlef.settings;

/**
 * Holds the settings for Gpodder.net.
 *
 * @author moe
 */
public interface GpodderSettings {

    /**
     * @return The user name
     */
    String getUsername();

    /**
     * @return The password
     */
    String getPassword();

    /**
     * @return The name that is used to identify this device in gpodder.net
     */
    String getDevicename();

    /**
     * This is used to indicate whether the value of
     * {@link Gpodder#getDevicename()} has been determined automatically from
     * the user name. If so, the device name gets updated if the user name is
     * udpated.
     *
     * @return true, if the value from {@link Gpodder#getDevicename()} is the
     *         default value, false otherwise.
     */
    boolean isDefaultDevicename();

}
