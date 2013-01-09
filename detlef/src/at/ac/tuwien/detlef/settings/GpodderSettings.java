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

import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.EpisodeSortChoice;

/**
 * POJO that holds the settings for Gpodder.net.
 *
 * @author moe
 */
public class GpodderSettings {

    private String username;

    private String password;

    private String devicename;

    private DeviceId deviceId;

    private String apiHostname = "gpodder.net";

    private String feedHostname = "feeds.gpodder.net";

    private long lastUpdate;

    private long lastEpisodeActionUpdate;

    private boolean accountVerified = false;

    private EpisodeSortChoice sortChoice = EpisodeSortChoice.ReleaseDate;

    private boolean ascending = false;

    /**
     * @return The user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return The user name
     */
    public GpodderSettings setUsername(String pUsername) {
        username = pUsername;
        return this;
    }

    /**
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    public GpodderSettings setPassword(String pPassword) {
        password = pPassword;
        return this;
    }

    /**
     * @return A human readable label for the device to identify it at gpodder.net.
     * The device name must not be confused with the {@link #getDeviceId() device id} that
     * uniquely identifies a device.
     *
     */
    public String getDevicename() {
        return devicename;
    }

    public GpodderSettings setDevicename(String pDevicename) {
        devicename = pDevicename;
        return this;
    }

    /**
     * @return The {@link DeviceId} that belongs to this account. If the device id is not set
     *     or invalid this method <strong>must</strong> return <code>null</code>.
     */
    public DeviceId getDeviceId() {
        return deviceId;
    }

    public GpodderSettings setDeviceId(DeviceId pDevideId) {
        deviceId = pDevideId;
        return this;
    }

    /**
     * @return The timestamp of the last synchronization with gpodder.net
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @return Set the timestamp of the last synchronization with gpodder.net
     */
    public GpodderSettings setLastUpdate(long timestamp) {
        lastUpdate = timestamp;
        return this;
    }

    public GpodderSettings setApiHostname(String pHostname) {
        apiHostname = pHostname;
        return this;
    }

    /**
     * @return The timestamp of the last episode action sync.
     */
    public long getLastEpisodeActionUpdate() {
        return lastEpisodeActionUpdate;
    }

    public GpodderSettings setLastEpisodeActionUpdate(long lastEpisodeActionUpdate) {
        this.lastEpisodeActionUpdate = lastEpisodeActionUpdate;
        return this;
    }

    public String getApiHostname() {
        return apiHostname;
    }


    /**
     * This is used to indicate whether the value of
     * {@link GpodderSettings#getDevicename()} has been determined automatically
     * from the user name. If so, the device name gets updated if the user name
     * is updated.
     *
     * @return true, if the value from {@link GpodderSettings#getDevicename()}
     *         is the default value, false otherwise.
     */
    public boolean isDefaultDevicename() {
        return getDevicename().equals(getDefaultDevicename());
    }

    private String getDefaultDevicename() {
        return String.format("%s-android", getUsername());
    }

    public String getFeedHostname() {
        return feedHostname;
    }

    public GpodderSettings setFeedHostname(String pFeedHostname) {
        feedHostname = pFeedHostname;
        return this;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    public GpodderSettings setAccountVerified(boolean pAccountVerified) {
        accountVerified = pAccountVerified;
        return this;
    }

    public boolean isAscending() {
        return ascending;
    }

    public GpodderSettings setAscending(boolean iAscending) {
        this.ascending = iAscending;
        return this;
    }

    public EpisodeSortChoice getSortChoice() {
        return sortChoice;
    }

    public GpodderSettings setSortChoice(EpisodeSortChoice iSortChoice) {
        this.sortChoice = iSortChoice;
        return this;
    }

}
