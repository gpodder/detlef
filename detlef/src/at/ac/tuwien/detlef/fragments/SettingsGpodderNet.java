package at.ac.tuwien.detlef.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.Gpodder;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;

/**
 * This fragment contains the UI logic for the gpodder.net account settings 
 * screen.
 * 
 * The user can enter these settings:
 * 	- User name
 *  - Password
 *  - Device name
 * 
 * Additionally, there exists a button which checks the validity of the entered
 * data.
 * 
 * @author moe
 */
public class SettingsGpodderNet extends PreferenceFragment {

	private static final String STATEVAR_PROGRESSBAR = "ProgressbarShowing";
	
	/**
	 * Holds a static reference to all {@link Toast} messages emitted by this
	 * fragment so that the can be canceled at any time.
	 */
	private static Toast toast;
	
	private Gpodder settings;
	private ConnectionTester connectionTester;
	
	/**
	 * The Thread that executes the user credential check. It is static so
	 * it can be accessed even if the Fragment gets recreated in the mean time.
	 */
	private static Thread connectionTestThread;
	
	private static ProgressDialog check;
	
	/**
	 * This holds a static reference to the activity that currently is 
	 * associated with this fragment. The reason for this is that if a 
	 * {@link Thread} is started and the screen is rotated while the thread is 
	 * running, the method {@link #getActivity()} will return null. But as the
	 * {@link Toast} needs to know the current {@link Context}, the 
	 * activity needs to be stored somewhere.
	 */
	private static Activity activity;
	
	private static final String logTag = "settings";
	
	public class TestConnectionButtonPreferenceListener implements
			OnPreferenceClickListener {

		

		public boolean onPreferenceClick(Preference arg0) {

			showProgressBar();
			toast.cancel();
			
			connectionTestThread = new Thread(new Runnable() {
				
				public void run() {
					
					try {
						if (getConnectionTester().testConnection(getSettings())) {
							showToast(activity.getText(R.string.connectiontest_successful));
						} else {
							showToast(activity.getText(R.string.connectiontest_unsuccessful));
						}
					} catch (GpodderConnectionException e) {
						showToast(activity.getText(R.string.connectiontest_error));
					} catch (InterruptedException e) {
						return;
					}
					
					if (check != null && check.isShowing()) {
		        		check.dismiss();
		        	}
					
					
					
				}
			});
			
			connectionTestThread.start();
			
			return true;
		}

	}

	/**
	 * @return The {@link ConnectionTester} that can be used to determine
	 *     the state of the
	 * TODO this is only a mock ... needs to be implemented correctly. This
	 *     returns randomly either one of the possible states.
	 */
	public ConnectionTester getConnectionTester() {
		if (connectionTester != null) {
			return connectionTester;
		}

		return new ConnectionTester() {
			public boolean testConnection(Gpodder pSettings)
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

	public SettingsGpodderNet setConnectionTester(ConnectionTester pConnectionTester) {
		connectionTester = pConnectionTester;
		return this;
	}

	/**
	 * TODO this is only a mock ... needs to be implemented correctly.
	 */
	public Gpodder getSettings() {
		if (settings != null) {
			return settings;
		}

		return new Gpodder() {

			public String getUsername() {
				return PreferenceManager
					.getDefaultSharedPreferences(getActivity())
					.getString("username", "");
			}

			public String getPassword() {
				return PreferenceManager
					.getDefaultSharedPreferences(getActivity())
					.getString("password", "");
			}

			public String getDevicename() {
				String storedName = PreferenceManager
					.getDefaultSharedPreferences(getActivity())
					.getString("devicename", "");

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

	public SettingsGpodderNet setSettings(Gpodder settings) {
		this.settings = settings;
		return this;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        Log.d(logTag, "onCreate(" + savedInstanceState + ")");
        Log.d(logTag, "Associated Activity is: " + getActivity());
        
        activity = getActivity();
        
        restoreState(savedInstanceState);
        
        
        toast = Toast.makeText(getActivity(), "", 0);
        
        Log.d(logTag, "Toast: " + toast);
        
        addPreferencesFromResource(R.xml.preferences_gpoddernet);

        setUpTestConnectionButton();
        setUpUsernameButton();
        setUpPasswordButton();
        setUpDeviceNameButton();

        loadSummaries();
    }

	private void restoreState(Bundle savedInstanceState) {
		
		if (savedInstanceState == null) {
			return;
		}
		
		if (savedInstanceState.getBoolean(STATEVAR_PROGRESSBAR)) {
			showProgressBar();
		}
		
	}

	private void setUpDeviceNameButton() {
        findPreference("devicename").setOnPreferenceChangeListener(
        	new OnPreferenceChangeListener() {
        		public boolean onPreferenceChange(Preference preference, Object newValue) {
        			preference.setSummary((String) newValue);
        			return true;
        		}
        	}
        );
	}

	private void setUpPasswordButton() {
        findPreference("password").setOnPreferenceChangeListener(
        	new OnPreferenceChangeListener() {
        		public boolean onPreferenceChange(Preference preference, Object newValue) {
        			preference.setSummary(maskPassword((String) newValue));
        			return true;
        		}
        	}
        );
	}

	private void setUpUsernameButton() {
        findPreference("username").setOnPreferenceChangeListener(
        	new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					preference.setSummary((String) newValue);
	
					if (getSettings().isDefaultDevicename()) {
						updateDeviceName((String) newValue);
					}
	
					return true;
				}
	
				private void updateDeviceName(String username) {
					Editor edit = PreferenceManager
							.getDefaultSharedPreferences(getActivity())
							.edit();
					
					boolean writeDevicenameStatus = edit.putString(
							"devicename",
							String.format("%s-android", username)
					).commit(); 
					
					if (writeDevicenameStatus) {
						findPreference("devicename").setSummary(getSettings().getDevicename());
					}
				}
        	}
        );
	}

	private void loadSummaries() {
		Preference username = (Preference) findPreference("username");
		username.setSummary(getSettings().getUsername());
		Preference password = (Preference) findPreference("password");
		password.setSummary(maskPassword(getSettings().getPassword()));
		Preference devicename = (Preference) findPreference("devicename");
		devicename.setSummary(getSettings().getDevicename());
	}

	private void setUpTestConnectionButton() {
        Preference button = (Preference) findPreference("button");
        button.setOnPreferenceClickListener(new TestConnectionButtonPreferenceListener());
	}

	private String maskPassword(String password) {
		return new String(new char[password.length()]).replace(
				"\0",
				getText(R.string.settings_fragment_gpodder_net_password_mask_char)
			);
	}
	
	/**
	 * Takes its best effort to display a toast message within the current
	 * {@link Activity#getApplicationContext() application context}.
	 * 
	 * This method also works if it is called from within a thread whose 
	 * belonging activity already has been destroyed (e.g. if the screen has 
	 * been rotated in the mean time). This is particularly useful for the
	 * connection test. 
	 * 
	 * Note that this method does not guarantee that the message is eventually 
	 * shown to the user. If the {@link #activity} is not accessible then
	 * no message is shown at all.   
	 * 
	 * @param message
	 */
	private void showToast(final CharSequence message) {
		
		Log.d(logTag, String.format("%s.showToast(%s)", toast, message));
		Log.d(logTag, String.format("Activity(%s)", activity));
		try {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.getApplicationContext();
					Toast.makeText(
						activity.getApplicationContext(),
						message,
						Toast.LENGTH_LONG
					).show();
				}
			});
		} catch (Exception e) {
			// don't care - after all it's not that critical that the user
			// receives the result under any circumstances. so better
			// catch and log any exception instead of breaking the complete
			// application at some other point.
			Log.e(logTag, e.getMessage(), e);
		}

	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  
	  Log.d(logTag, "onSaveInstanceState()");
	  savedInstanceState.putBoolean(STATEVAR_PROGRESSBAR, check != null && check.isShowing());
	}
	
	private void showProgressBar() {
		check = ProgressDialog.show(
			getActivity(),
			getString(R.string.settings_fragment_gpodder_net_testconnection_progress_title),
			getString(R.string.settings_fragment_gpodder_net_testconnection_progress_message),
			true,
			true,
			new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					Log.d(logTag, String.format("%s.onCancel(%s)", check, dialog));
					if (connectionTestThread != null && connectionTestThread.isAlive()) {
						Log.d(logTag, "interrupting thread.");
						connectionTestThread.interrupt();
					}
				}
			}
		);
		
		
		
		Log.d(logTag, "Open Progressbar: " + check);
	}

}