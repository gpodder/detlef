package at.ac.tuwien.detlef.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;

/**
 * Advanced settings that are only needed by experienced users.
 *
 * @author moe
 */
public class SettingsAdvanced extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        writeDefaultSettings();
        addPreferencesFromResource(R.xml.preferences_advanced);
        loadSummaries();
        setUpSummaryUpdateListener(findPreference(GpodderSettingsDAO.KEY_API_ENDPOINT));
        setUpSummaryUpdateListener(findPreference(GpodderSettingsDAO.KEY_FEED_ENDPOINT));

    }

    private void loadSummaries() {
        Preference apiEndpoint = findPreference(GpodderSettingsDAO.KEY_API_ENDPOINT);
        apiEndpoint.setSummary(getSettings().getApiHostname());
        Preference feedEndpoint = findPreference(GpodderSettingsDAO.KEY_FEED_ENDPOINT);
        feedEndpoint.setSummary(getSettings().getFeedHostname());
    }

    private GpodderSettings getSettings() {
        return DependencyAssistant.getDependencyAssistant().getGpodderSettings(getActivity());
    }

    /**
     * Fetches the current settings and writes them. This makes sure that the default
     * settings are written to the storage and are visible in the settings screen.
     */
    private void writeDefaultSettings() {
        DependencyAssistant.getDependencyAssistant()
            .getGpodderSettingsDAO(getActivity())
            .writeSettings(getSettings());
    }

    private void setUpSummaryUpdateListener(Preference preference) {
        preference.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((String) newValue);
                    return true;
                }
            }
        );
    }

}
