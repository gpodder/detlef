package at.ac.tuwien.detlef.settings;

import java.util.HashMap;

import android.content.SharedPreferences;

public class GpodderSettingsDAOAndroid implements GpodderSettingsDAO {

	private HashMap<String, Object> dependencies;

	public GpodderSettings getSettings() {
		return new GpodderSettings() {
			public String getUsername() {
				return getSharedPreferences().getString("username", "");
			}

			public String getPassword() {
				return getSharedPreferences().getString("password", "");
			}

			public String getDevicename() {
				String storedName = getSharedPreferences().getString("devicename", "");

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

            public long getLastUpdate() {
                return getSharedPreferences().getLong("lastUpdate", 0);
            }

            public void setLastUpdate(long timestamp) {
                getSharedPreferences().edit().putLong("lastUpdate", timestamp).commit();
            }
		};
	}

	public GpodderSettingsDAO writeSettings(GpodderSettings settings) {
		throw new UnsupportedOperationException(
			"The Android Settings DAO does not support write operations"
		);
	}

	/**
	 * This implementation requires an object of type
	 * {@link SharedPreferences} with the key sharedPreferences.
	 */
	public GpodderSettingsDAO setDependencies(HashMap<String, Object> pDependencies) {
		dependencies = pDependencies;
		return this;
	}

	private SharedPreferences getSharedPreferences() {
		return (SharedPreferences) dependencies.get("sharedPreferences");
	}

}
