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

/**
 * The GpodderSettingsDAO takes care that {@link GpodderSettings} are retrieved
 * from some storage.
 *
 * @author moe
 */
public interface GpodderSettingsDAO {

    GpodderSettings getSettings();

    /**
     * Writes the settings to the storage engine.
     * @param settings
     * @return Fluent interface.
     * @throws UnsupportedOperationException If the Implementation does not support writing
     * settings. This might be the case if the settings are stored via some internal mechanism,
     * as it is the case in {@link GpodderSettingsDAOAndroid}.
     */
    GpodderSettingsDAO writeSettings(GpodderSettings settings);

    /**
     * This method can be used to pass dependencies that are specific to a
     * certain implementation, e.g. A database handler for a DB based settings
     * DAO or the PreferenceManager for an android system based storage.
     * 
     * Regardless which dependency you need: Do not forget to document
     * which ones your implementation expects.
     * 
     * @param dependencies
     * @return Fluent interface.
     */
    GpodderSettingsDAO setDependencies(HashMap<String, Object> dependencies);


}
