package at.ac.tuwien.detlef.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import at.ac.tuwien.detlef.R;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }

    /**
     * This fragment contains the settings for the gpodder.net account.
     * @author moe
     */
    public static class GpodderNetPrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_gpoddernet);

	        Preference button = (Preference)findPreference("button");
	        button.setOnPreferenceClickListener(
			new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference arg0) {

					ProgressDialog check = ProgressDialog.show(
						getActivity(),
						getString(R.string.settings_fragment_gpodder_net_testconnection_progress_title),
						getString(R.string.settings_fragment_gpodder_net_testconnection_progress_message),
						true,
						true
					);
	                    return true;
				}
			}
	        );
        }

    }


}
