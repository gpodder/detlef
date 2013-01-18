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

import android.content.Context;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;

/**
 * A {@link ConnectionTester} that verifies {@link GpodderSettings} data against
 * the public API of gpodder.net.
 * 
 * <p>This implementation needs a {@link Context} to be injected via {@link #setContext(Context)}
 * in order to work properly.</p>
 * 
 * @author moe
 *
 */
public class ConnectionTesterGpodderNet implements ConnectionTester {

    /** Tag for logging. */
    private static final String TAG = ConnectionTesterGpodderNet.class.getCanonicalName();

    @Override
    public void testConnection(GPodderSync gpodderSync, GpodderSettings settings,
            NoDataResultHandler<?> callback) {

        gpodderSync.setHostname(settings.getApiHostname());

        gpodderSync.addAuthCheckJob(
                settings.getUsername(),
                settings.getPassword(),
                callback);
    }

}
