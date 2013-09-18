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

package at.ac.tuwien.detlef.gpodder;

import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;

/**
 * This class facilitates background HTTP and gpodder.net transactions.
 *
 * @author ondra
 */
public class GPodderSync {
    /** Logging tag. */
    private static final String TAG = "GPodderSync";

    /** Information about this client of the gpodder.net-compatible service. */
    private final GpoNetClientInfo clientInfo;

    /**
     * Constructs a GPodderSync instance.
     *
     * @param sr The handler which will take care of any
     *            threading/synchronization concerns.
     */
    public GPodderSync() {
        Log.d(TAG, "GPodderSync");

        clientInfo = new GpoNetClientInfo();
        clientInfo.setHostname(Singletons.i().getGpodderSettings().getApiHostname());
    }

    /**
     * Sets the username used for access to gpodder.net-compatible services.
     *
     * @param newUsername The new username.
     */
    public void setUsername(String newUsername) {
        clientInfo.setUsername(newUsername);
    }

    /**
     * Sets the password used for access to gpodder.net-compatible services.
     *
     * @param newPassword The new password.
     */
    public void setPassword(String newPassword) {
        clientInfo.setPassword(newPassword);
    }

    /**
     * Sets the hostname of the gpodder.net-compatible service to use.
     *
     * @param newHostname The hostname of the gpodder.net-compatible service to
     *            use.
     */
    public void setHostname(String newHostname) {
        clientInfo.setHostname(newHostname);
    }

    /**
     * Sets the device name used for access to gpodder.net-compatible services.
     *
     * @param newDeviceName The new device name.
     */
    public void setDeviceName(String newDeviceName) {
        clientInfo.setDeviceId(newDeviceName);
    }


    public GpoNetClientInfo getClientInfo() {
        return clientInfo;
    }
}
