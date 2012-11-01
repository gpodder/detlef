package at.ac.tuwien.detlef.settings;
/**
 * The connection tester is responsible for the verification of user credentials.
 * @author moe
 */
public interface ConnectionTester {
	/**
	 * Establishes a connection to the gpodder service and verifies if the
	 * provided user credentials are correct.
	 * @param settings The settings that are used to connect to the service.
	 * @return true if the credentials provided in the {@link Gpodder settings} are valid,
	 *     false otherwise.
	 * @throws GpodderConnectionException If no connection to the server could be
	 *     established.
	 */
	boolean testConnection(Gpodder settings) throws GpodderConnectionException;
}
