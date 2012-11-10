package at.ac.tuwien.detlef.gpodder.plumbing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores information (username, password, target hostname, device name) about a client of a
 * gpodder.net-compatible service.
 * @author ondra
 */
public class GpoNetClientInfo implements Parcelable {

    /** The username of the user requesting something from the gpodder.net-compatible service. */
    private String username;

    /** The password of the user requesting something from the gpodder.net-compatible service. */
    private String password;

    /** The hostname of the gpodder.net-compatible service. */
    private String hostname;

    /**
     * The name of the device the user is wishing to contact the gpodder.net-compatible service
     * about.
     */
    private String deviceId;

    public GpoNetClientInfo() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(hostname);
        dest.writeString(deviceId);
    }

    public static final Parcelable.Creator<GpoNetClientInfo> CREATOR
    = new Parcelable.Creator<GpoNetClientInfo>() {

        public GpoNetClientInfo createFromParcel(Parcel source) {
            GpoNetClientInfo gnci = new GpoNetClientInfo();
            gnci.setUsername(source.readString());
            gnci.setPassword(source.readString());
            gnci.setHostname(source.readString());
            gnci.setDeviceId(source.readString());
            return gnci;
        }

        public GpoNetClientInfo[] newArray(int size) {
            return new GpoNetClientInfo[size];
        };
    };
}

