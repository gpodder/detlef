package at.ac.tuwien.detlef.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;

/**
 * Sets the storage information about the episodes in the settings screen.
 *
 * @author Lacky
 */
public class SettingsStorage extends PreferenceFragment {

    private static final String TAG = SettingsStorage.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_storage);
        setUpStorageLocation();
    }

    /**
     * Sets the storage location textbox in the settings screen,
     * if the external files dir isn't available we will display an empty string.
     */
    private void setUpStorageLocation() {
        Preference storageLocation = findPreference("storage_location");
        try {
            storageLocation.setSummary(Detlef.getAppContext()
                .getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
        } catch (Exception ex) {
            Log.w(TAG, "cannot get music directory: " + ex.toString());
            storageLocation.setSummary("");
        }
    }
}
