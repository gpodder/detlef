package at.ac.tuwien.detlef.gpodder;

import java.util.List;

import at.ac.tuwien.detlef.domain.Podcast;

/**
 * This is the callback interface for jobs which return a list of podcasts on success.
 */
public interface PodcastListResultHandler extends ResultHandler {
    /**
     * Called to handle a successful fetching of a podcast list.
     * @param result List of strings that has been successfully fetched.
     */
    void handleSuccess(List<Podcast> result);
}
