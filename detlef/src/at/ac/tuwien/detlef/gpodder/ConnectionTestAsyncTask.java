/* *************************************************************************
 *  Copyright 2012-2013 The detlef developers                              *
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

package at.ac.tuwien.detlef.gpodder;

import android.app.Fragment;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 * Background task that verifies the user settings.
 */
public class ConnectionTestAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = ConnectionTestAsyncTask.class.getCanonicalName();

    private final ConnectionTestCallback<? extends Fragment> callback;

    private GpodderSettings settings;

    public ConnectionTestAsyncTask(ConnectionTestCallback<? extends Fragment> pCallback) {
        callback = pCallback;
    }

    @Override
    public void run() {
        
        try {
            if (getConnectionTester().testConnection(getSettings())) {
                callback.connectionIsValid(getSettings());
            } else {
                callback.connectionIsNotValid();
            }
        } catch (GpodderConnectionException e) {
            callback.connectionFailed();
        } catch (InterruptedException e) {
            Log.w(TAG, "ConnectionTestAsyncTask was interrupted.");
            return;
        }

    }

    /**
     * @return The {@link ConnectionTester} that can be used to determine if the
     *         user name and password are correct.
     */
    public ConnectionTester getConnectionTester() {
        return DependencyAssistant.getDependencyAssistant().getConnectionTester();
    }

    public GpodderSettings getSettings() {
        return settings;
    }

    public ConnectionTestAsyncTask setSettings(GpodderSettings settings) {
        this.settings = settings;
        return this;
    }
    
}
