package at.ac.tuwien.detlef.settings;

/**
 * The connection tester is responsible for the verification of user credentials.
 * It is used by the "Test Connection" button by the {@link SettingsGpodderNet} fragment. 
 * @author moe
 */
public interface ConnectionTester {
	/**
	 * Verifies if the provided user credentials are correct.
	 * @param settings The settings that are used to connect to the service.
	 * @return true if the credentials provided in {@link GpodderSettings} 
	 *     are valid, false otherwise.
	 * @throws GpodderConnectionException If no connection to the server could 
	 *     be established.
	 * @throws InterruptedException If the worker thread gets interrupted while
	 *     executing the operation.
	 */
	boolean testConnection(GpodderSettings settings)
		throws InterruptedException, GpodderConnectionException;
}
