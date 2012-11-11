package at.ac.tuwien.detlef;

import java.util.HashMap;

import android.content.Context;
import android.preference.PreferenceManager;
import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;

public class DependencyAssistant {

	/**
	 * Making this a final static variable does not make sense w.r.t. testability.
	 * It should be possible to inject modified versions of the dependency assistant.
	 * Therefore, use the static getter {@link #getDependencyAssistant()} instead.
	 * If you need to inject mock objects during testing, use {@link #setDependencyAssistant()}.
	 * @deprecated
	 */
    public final static DependencyAssistant DEPENDENCY_ASSISTANT = new DependencyAssistant();
    
    private static DependencyAssistant dependencyAssistant = new DependencyAssistant();
    
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
     * @param context The {@link Context}. This is needed in order to
     *     be able to access Android's system settings. Usually this will
     *     be the current {@link Activity}.
     * @return Gets the {@link GpodderSettings gpodder.net settings instance}
     *         that provides the user name, password and device name settings.
     */
    public GpodderSettings getGpodderSettings(Context context) {
    	
    	HashMap<String, Object> dependecies = new HashMap<String, Object>();
    	dependecies.put(
    		"sharedPreferences",
    		PreferenceManager.getDefaultSharedPreferences(context)
    	);
    	
    	GpodderSettingsDAO gpodderSettingsDAO = new GpodderSettingsDAOAndroid();
    	gpodderSettingsDAO.setDependecies(dependecies);
    	return gpodderSettingsDAO.getSettings();

    }
    
    /**
     * TODO Right now this is only a mock.
     * @return
     */
	public ConnectionTester getConnectionTester() {
		return new ConnectionTester() {
			public boolean testConnection(GpodderSettings pSettings)
					throws GpodderConnectionException, InterruptedException {

				Thread.sleep(10000);

				switch ((int) Math.floor(Math.random() * 3)) {
				case 0:
					return true;
				case 1:
					return false;
				default:
					throw new GpodderConnectionException();					
				}

				
			}
		};
	}

	public static DependencyAssistant getDependencyAssistant() {
		return dependencyAssistant;
	}

	/**
	 * Overwrites the default {@link DependencyAssistant} with a custom one. By using
	 * this method you can easily replace parts of the Application with Mocks which is
	 * useful for testing. 
	 * 
	 * @param pDependencyAssistant
	 */
	public static void setDependencyAssistant(DependencyAssistant pDependencyAssistant) {
		dependencyAssistant = pDependencyAssistant;
	}

}
