package at.ac.tuwien.detlef.settings;

import java.util.HashMap;

/**
 * The GpodderSettingsDAO takes care that {@link GpodderSettings} are retrieved
 * from some storage.
 *
 * @author moe
 */
public interface GpodderSettingsDAO {

    GpodderSettings getSettings();
    
    GpodderSettingsDAO writeSettings(GpodderSettings settings);
    
    /**
     * This method can be used to pass dependencies that are specific to a
     * certain implementation, e.g. A database handler for a DB based settings
     * DAO or the PreferenceManager for an android system based storage.
     * 
     * Regardless which dependency you need: Do not forget to document 
     * which ones your implementation expects. 
     * 
     * @param dependecies 
     * @return Fluent interface.
     */
    GpodderSettingsDAO setDependecies(HashMap<String, Object> dependecies);


}
