package at.ac.tuwien.detlef;

import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.settings.SharedPreferencesRetriever;

public class DependencyAssistant {

    public static final DependencyAssistant DEPENDENCY_ASSISTANT = new DependencyAssistant();

    /**
     * @return Gets the quasi-singleton GPodderSync instance for this program.
     */
    public GPodderSync getGPodderSync() {
        return null;
    }

    /**
     * @return Gets the quasi-singleton PodcastDBAssistant instance for this
     *         program.
     */
    public PodcastDBAssistant getPodcastDBAssistant() {
        return null;
    }

    /**
     * @return Gets the quasi-singleton EpisodeDBAssistant instance for this
     *         program.
     */
    public EpisodeDBAssistant getEpisodeDBAssistant() {
        return null;
    }

    /**
     * @return Gets the quasi-singleton SharedPreferencesRetriever instance for
     *         this program.
     */
    public SharedPreferencesRetriever getSharedPreferencesRetriever() {
        return null;
    }

}
