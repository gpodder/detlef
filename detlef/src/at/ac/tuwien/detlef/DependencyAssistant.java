package at.ac.tuwien.detlef;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import at.ac.tuwien.detlef.db.EpisodeDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;

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
     * @param context The {@link Context}. This is needed in order to
     *     be able to access Android's system settings.
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

}
