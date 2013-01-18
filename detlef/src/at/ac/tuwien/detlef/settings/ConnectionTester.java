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

import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;

/**
 * The connection tester is responsible for the verification of user credentials.
 * It is used by the "Test Connection" button by the {@link SettingsGpodderNet} fragment.
 * @author moe
 */
public interface ConnectionTester {
    /**
     * Verifies if the provided user credentials are correct.
     * @param gpodderSync The GPodderSync to use.
     * @param settings The settings that are used to connect to the service.
     * @param callback The callback gpodderSync has to report to.
     */
    public void testConnection(GPodderSync gpodderSync, GpodderSettings settings,
            NoDataResultHandler<?> callback);
}
