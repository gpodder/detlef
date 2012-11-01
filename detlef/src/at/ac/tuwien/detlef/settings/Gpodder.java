package at.ac.tuwien.detlef.settings;
/**
 * Holds the settings for Gpodder.net.
 * @author moe
 */
public interface Gpodder {

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

}
