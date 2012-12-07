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

/**
 * POJO that holds the settings for Gpodder.net.
 *
 * @author moe
 */
public interface GpodderSettings {

    /**
     * @return The user name
     */
    String getUsername();

    /**
     * @return The password
     */
    String getPassword();

    /**
     * @return The name that is used to identify this device in gpodder.net
     */
    String getDevicename();
    
    /**
     * @return The timestamp of the last synchronization with gpodder.net
     */
    long getLastUpdate();

    /**
     * @return Set the timestamp of the last synchronization with gpodder.net
     */
    void setLastUpdate(long timestamp);

    /**
     * This is used to indicate whether the value of
     * {@link GpodderSettings#getDevicename()} has been determined automatically 
     * from the user name. If so, the device name gets updated if the user name 
     * is updated.
     *
     * @return true, if the value from {@link GpodderSettings#getDevicename()} 
     *         is the default value, false otherwise.
     */
    boolean isDefaultDevicename();

}
