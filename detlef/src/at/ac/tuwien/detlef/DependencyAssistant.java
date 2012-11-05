package at.ac.tuwien.detlef;

import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.settings.GpodderSettings;

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
     * @return Gets the {@link GpodderSettings gpodder.net settings instance} that provides the
     *     user name, password and device name settings.
     * TODO Right now this is only a mock that returns some hard coded data. 
     */
    public GpodderSettings getGpodderSettings() {
        return new GpodderSettings() {
			
			public String getUsername() {
				return "";
			}
			
			public String getPassword() {
				return "";
			}
			
			public String getDevicename() {
				return String.format("%s-android", getUsername());
			}
			
			public boolean isDefaultDevicename() {
				return true;
			}
		};
    }

}
