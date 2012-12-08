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

    /** The key that is used to store the {@link DeviceId}. */
    private static final String KEY_DEVICE_ID = "device-id";

    private static final String TAG = GpodderSettingsDAOAndroid.class.getCanonicalName();

    private HashMap<String, Object> dependencies = new HashMap<String, Object>();

    public GpodderSettings getSettings() {
        GpodderSettings result = new GpodderSettings();

        result.setUsername(getSharedPreferences().getString("username", ""));
        result.setPassword(getSharedPreferences().getString("password", ""));
        result.setDevicename(getSharedPreferences().getString("devicename", ""));
        result.setLastUpdate(getSharedPreferences().getLong("lastUpdate", 0));

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
            .putString(KEY_DEVICE_ID, deviceId)
            .putLong("lastUpdate", settings.getLastUpdate())
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
