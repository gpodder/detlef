package at.ac.tuwien.detlef.gpodder;

/**
 * This is the callback interface for {@link GPodderSync#addHttpDownloadJob(String,
 * HttpDownloadResultHandler))}.
 * @author ondra
 */
public interface HttpDownloadResultHandler extends ResultHandler {
    /**
     * Called to handle a successful HTTP download.
     * @param bytes The bytes downloaded from the server.
     */
    public void handleSuccess(byte[] bytes);

    /**
     * Called to handle an HTTP download progress update.
     * @param have Number of bytes that have been downloaded.
     * @param total Total size of file being downloaded, or -1 if unknown.
     */
    public void handleProgress(int have, int total);
}
