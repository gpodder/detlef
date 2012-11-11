package at.ac.tuwien.detlef.gpodder;

import java.util.List;

/**
 * This is the callback interface for jobs which return a list of strings on success. Examples of
 * methods starting such jobs are {@link
 * GPodderSync#addDownloadPodcastListJob(StringListResultHandler)}.
 * @author ondra
 */
public interface StringListResultHandler extends ResultHandler {
    /**
     * Called to handle a successful fetching of a string list.
     * @param strs List of strings that has been successfully fetched.
     */
    void handleSuccess(List<String> strs);
}
