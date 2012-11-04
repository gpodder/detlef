package at.ac.tuwien.detlef;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.Gpodder;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;

/**
 * This fragment contains the settings for the gpodder.net account.
 * @author moe
 */
public class SettingsGpodderNetFragment extends PreferenceFragment {

	static Toast toast;

	/**
	 * @return The {@link ConnectionTester} that can be used to determine
	 *     the state of the
	 * TODO this is only a mock ... needs to be implemented correctly. This
	 *     returns randomly either one of the possible states.
	 */
	public ConnectionTester getConnectionTester() {
		return new ConnectionTester() {

			public boolean testConnection(Gpodder settings)
					throws GpodderConnectionException {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				switch ((int) Math.floor(Math.random() * 3)) {
				case 0:
					return true;
				case 1:
					return false;
				}

				throw new GpodderConnectionException();
			}
		};
	}

	/**
	 * TODO this is only a mock ... needs to be implemented correctly.
	 */
	public Gpodder getSettings() {
		return new Gpodder() {

			public String getUsername() {
				return PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("username", "");
			}

			public String getPassword() {
				return PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("password", "");
			}

			public String getDevicename() {
				String storedName = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("devicename", "");

				if (storedName.isEmpty()) {
					return getDefaultDevicename();
				}

				return storedName;
			}

			private String getDefaultDevicename() {
				return String.format("%s-android", getUsername());
			}

			public boolean isDefaultDevicename() {
				return getDevicename().equals(getDefaultDevicename());
			}

		};
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        toast = Toast.makeText(getActivity(), "", 0);
        addPreferencesFromResource(R.xml.preferences_gpoddernet);

        setUpTestConnectionButton();

        findPreference("username").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String) newValue);

				if(getSettings().isDefaultDevicename()) {
					updateDeviceName((String) newValue);
				}

				return true;
			}

			private void updateDeviceName(String username) {
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
				if (edit.putString("devicename", String.format("%s-android", username)).commit()) {
					findPreference("devicename").setSummary(getSettings().getDevicename());
				}
			}
		}
        );

        findPreference("password").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(maskPassword((String) newValue));
				return true;
			}
		}
        );

        findPreference("devicename").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String) newValue);
				return true;
			}
		}
        );

        loadSummaries();
    }

	private void loadSummaries() {
		Preference username = (Preference)findPreference("username");
		username.setSummary(getSettings().getUsername());
		Preference password = (Preference)findPreference("password");
		password.setSummary(maskPassword(getSettings().getPassword()));
		Preference devicename = (Preference)findPreference("devicename");
		devicename.setSummary(getSettings().getDevicename());
	}

	private Object getDefaultDeviceName() {
		return null;
	}

	private void setUpTestConnectionButton() {
        Preference button = (Preference)findPreference("button");
        button.setOnPreferenceClickListener(
		new Preference.OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference arg0) {

				final ProgressDialog check = ProgressDialog.show(
					getActivity(),
					getString(R.string.settings_fragment_gpodder_net_testconnection_progress_title),
					getString(R.string.settings_fragment_gpodder_net_testconnection_progress_message),
					true,
					true
				);

				toast.cancel();

				new Thread() {
					public void run() {

						int status;

						try {
								if(getConnectionTester().testConnection(getSettings())) {
									status = R.string.settings_fragment_gpodder_net_testconnection_successful;
								} else {
									status = R.string.settings_fragment_gpodder_net_testconnection_unsuccessful;
								}
							} catch (GpodderConnectionException e) {
								status = R.string.settings_fragment_gpodder_net_testconnection_connectionerror;
							}
						check.dismiss();
						final int toastStatus = status;

						getActivity().runOnUiThread(new Runnable() {
							  public void run() {
								  toast = Toast.makeText(
								      getActivity(),
                                          String.format(getString(toastStatus), getSettings().getUsername()), Toast.LENGTH_LONG);
								  toast.show();
							  }
						});


					}
				}.start();

                    return true;
			}
		}
        );
	}

	private String maskPassword(String password) {
		return new String(new char[password.length()]).replace(
				"\0",
				getText(R.string.settings_fragment_gpodder_net_password_mask_char)
			);
	}

}