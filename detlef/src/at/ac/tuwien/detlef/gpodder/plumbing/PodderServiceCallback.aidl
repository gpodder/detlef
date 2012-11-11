package at.ac.tuwien.detlef.gpodder.plumbing;

import at.ac.tuwien.detlef.gpodder.plumbing.ParcelableByteArray;

/**
 * Interface for the {@link PodderService} to contact you.
 */
oneway interface PodderServiceCallback
{
	/**
	 * The HTTP download succeeded.
	 * @param reqId The request ID you passed.
	 * @param data The contents of the downloaded file.
	 */
	void httpDownloadSucceeded(int reqId, in ParcelableByteArray data);

	/**
	 * The HTTP download to a file succeeded.
	 * @param reqId The request ID you passed.
	 */
	void httpDownloadToFileSucceeded(int reqId);

	/**
	 * The HTTP download failed. (Also used for HTTP downloads-to-files.)
	 * @param reqId The request ID you passed.
	 * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
	 * error that occurred.
	 * @param errStr Error message describing the error that occurred.
	 */
	void httpDownloadFailed(int reqId, int errCode, in String errStr);

	/**
	 * HTTP download progress update. (Also used for HTTP downloads-to-files.)
	 * @param reqId The request ID you passed.
	 * @param haveBytes Number of bytes downloaded successfully.
	 * @param totalBytes Total file size in bytes, or -1 if unknown.
	 */
	void httpDownloadProgress(int reqId, int haveBytes, int totalBytes);

	/**
	 * The service is still alive.
	 * @param reqId The request ID you passed.
	 */
	void heartbeatSucceeded(int reqId);

	/**
	 * The authentication check succeeded.
	 * @param reqId The request ID you passed.
	 */
	void authCheckSucceeded(int reqId);

	/**
	 * The authentication check failed.
	 * @param reqId The request ID you passed.
	 * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
	 * error that occurred.
	 * @param errStr Error message describing the error that occurred.
	 */
	void authCheckFailed(int reqId, int errCode, in String errStr);

	/**
	 * The podcast list has been fetched successfully.
	 * @param reqId The request ID you passed.
	 * @param podcasts List of podcasts as fetched from server.
	 */
	void downloadPodcastListSucceeded(int reqId, in List<String> podcasts);

	/**
	 * The podcast list download failed.
	 * @param reqId The request ID you passed.
	 * @param errCode Error code (see {@link PodderService.ErrorCode}) describing the type of
	 * error that occurred.
	 * @param errStr Error message describing the error that occurred.
	 */
	void downloadPodcastListFailed(int reqId, int errCode, in String errStr);
}
