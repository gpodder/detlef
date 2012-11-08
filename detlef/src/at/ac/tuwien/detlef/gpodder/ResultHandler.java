package at.ac.tuwien.detlef.gpodder;

/**
 * General callback interface.
 * @author ondra
 */
public interface ResultHandler {

    /**
     * Called to handle a failed HTTP download.
     * @param errCode The error code; see {@link PodderService.MessageErrorCode}.
     * @param errStr A string describing the error.
     */
    void handleFailure(int errCode, String errStr);
}
