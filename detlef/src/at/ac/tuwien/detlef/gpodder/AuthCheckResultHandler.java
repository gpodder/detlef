package at.ac.tuwien.detlef.gpodder;

/**
 * This is the callback interface for {@link GPodderSync#addAuthCheckJob(String, String, String,
 * AuthCheckResultHandler))}.
 * @author ondra
 */
public interface AuthCheckResultHandler extends ResultHandler {
    /**
     * Called to handle a successful authentication.
     */
    void handleSuccess();
}
