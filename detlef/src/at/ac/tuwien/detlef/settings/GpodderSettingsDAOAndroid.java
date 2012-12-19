/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */


package at.ac.tuwien.detlef.settings;

import java.util.HashMap;

import android.content.SharedPreferences;
import android.util.Log;
import at.ac.tuwien.detlef.domain.DeviceId;

/**
 * Implementation of {@link GpodderSettingsDAO} that uses the internal Preference Storage
 * Registry of Android to store and retrieve Settings.
 * @author moe
 */
public class GpodderSettingsDAOAndroid implements GpodderSettingsDAO {

    private static final String TAG = GpodderSettingsDAOAndroid.class.getCanonicalName();

    private HashMap<String, Object> dependencies = new HashMap<String, Object>();

    public GpodderSettings getSettings() {
        GpodderSettings result = new GpodderSettings();

        result.setUsername(getSharedPreferences().getString(KEY_USERNAME, ""));
        result.setPassword(getSharedPreferences().getString(KEY_PASSWORD, ""));
        result.setDevicename(getSharedPreferences().getString("devicename", ""));
        result.setLastUpdate(getSharedPreferences().getLong("lastUpdate", 0));
        result.setLastEpisodeActionUpdate(getSharedPreferences()
                .getLong("lastEpisodeActionUpdate", 0));
        result.setApiHostname(
            getSharedPreferences().getString(KEY_API_ENDPOINT, DEFAULT_API_ENDPOINT)
        );
        result.setFeedHostname(
            getSharedPreferences().getString(KEY_FEED_ENDPOINT, DEFAULT_FEED_ENDPOINT)
        );
        
        result.setAccountVerified(getSharedPreferences().getBoolean(KEY_ACCOUNT_VERIFIED, false));
        
        try {
            result.setDeviceId(new DeviceId(getSharedPreferences().getString(KEY_DEVICE_ID, null)));
        } catch (IllegalArgumentException e) {
            result.setDeviceId(null);
        }

        return result;
    }

    public GpodderSettingsDAO writeSettings(GpodderSettings settings) {

        String deviceId = null;
        
        if (settings.getDeviceId() != null) {
            deviceId = settings.getDeviceId().toString();
        }
        
        getSharedPreferences().edit()
            .putString(KEY_USERNAME, settings.getUsername())
            .putString(KEY_PASSWORD, settings.getPassword())
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_API_ENDPOINT, settings.getApiHostname())
            .putString(KEY_FEED_ENDPOINT, settings.getFeedHostname())
            .putBoolean(KEY_ACCOUNT_VERIFIED, settings.isAccountVerified())
            .putLong("lastUpdate", settings.getLastUpdate())
            .putLong("lastEpisodeActionUpdate", settings.getLastEpisodeActionUpdate())
            .commit();
        return this;

    }

    /**
     * This implementation requires an object of type
     * {@link SharedPreferences} with the key sharedPreferences.
     */
    public GpodderSettingsDAO setDependencies(HashMap<String, Object> pDependencies) {
        Log.d(TAG, this + "setDependencies: " + pDependencies);
        dependencies = pDependencies;
        return this;
    }

    private SharedPreferences getSharedPreferences() {
        Log.d(TAG, this + "getSharedPreferences: " + dependencies);
        return (SharedPreferences) dependencies.get("sharedPreferences");
    }

}
