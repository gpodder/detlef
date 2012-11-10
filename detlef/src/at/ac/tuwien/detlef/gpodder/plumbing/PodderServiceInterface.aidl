package at.ac.tuwien.detlef.gpodder.plumbing;

import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.gpodder.plumbing.PodderServiceCallback;

/**
 * Interface to connect to the {@link PodderService}.
 */
oneway interface PodderServiceInterface
{
	/**
	 * Perform an HTTP download.
	 * @param cb Callback object to send responses to.
	 * @param reqId The ID of the request; will be passed to the callback unchanged.
	 * @param url URL of the file to download.
	 */
	void httpDownload(in PodderServiceCallback cb, int reqId, in String url);

	/**
	 * Perform an HTTP download and store the result in a file.
	 * @param cb Callback object to send responses to.
	 * @param reqId The ID of the request; will be passed to the callback unchanged.
	 * @param url URL of the file to download.
	 * @param localfn Local path at which to store the downloaded file.
	 */
	void httpDownloadToFile(in PodderServiceCallback cb, int reqId, in String url, in String localfn);

	/**
	 * Prove that the service is still alive.
	 * @param cb Callback object to send responses to.
	 * @param reqId The ID of the request; will be passed to the callback unchanged.
	 */
	void heartbeat(in PodderServiceCallback cb, int reqId);

	/**
	 * Check the given authentication credentials with a gpodder.net-compatible service.
	 * @param cb Callback object to send responses to.
	 * @param reqId The ID of the request; will be passed to the callback unchanged.
	 * @param cinfo The information about the client. Must contain username, password and hostname.
	 */
	void authCheck(in PodderServiceCallback cb, int reqId, in GpoNetClientInfo cinfo);
}
