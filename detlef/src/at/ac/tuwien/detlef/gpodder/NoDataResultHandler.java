package at.ac.tuwien.detlef.gpodder;

/**
 * This is the callback interface for any job which doesn't return data on success. Examples of
 * methods starting such jobs are {@link GPodderSync#addAuthCheckJob(String, String, String,
 * NoDataResultHandler)))}.
 * @author ondra
 */
public interface NoDataResultHandler extends ResultHandler {
    /**
     * Called to handle a successful authentication.
     */
    void handleSuccess();
}
