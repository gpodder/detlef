package at.ac.tuwien.detlef.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.R.xml;

public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        addPreferencesFromResource(R.xml.preferences);
    }

}
