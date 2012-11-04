package at.ac.tuwien.detlef.activities;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import at.ac.tuwien.detlef.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }

}
