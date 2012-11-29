package at.ac.tuwien.detlef.domain;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Dummy class to display initial test podcast content until the actual podcast
 * classes are available.
 */
public class Podcast implements IPodcast, Serializable, Parcelable {

    /**
     * default serialUID added.
     */
    private static final long serialVersionUID = 1L;

    private long id;

    private String title = "This is a podcast title";

    private String description = "This is a podcast description.";

    private String logoUrl = "www.dummyurl.com";

    private String logoFilePath;

    private String url = "www.dummyurl.com";

    private long lastUpdate;

    public Podcast() { }

    public Podcast(IPodcast p) {
        setUrl(p.getUrl());
        setTitle(p.getTitle());
        setDescription(p.getDescription());
        setLogoUrl(p.getLogoUrl());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLogoUrl() {
        return logoUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public Podcast setUrl(String urlIn) {
        this.url = urlIn;
        return this;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public long getId() {
        return id;
    }

    public Podcast setId(long idIn) {
        this.id = idIn;
        return this;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public Podcast setLastUpdate(long lastUpdateIn) {
        this.lastUpdate = lastUpdateIn;
        return this;
    }

    public String getLogoFilePath() {
        return logoFilePath;
    }

    public Podcast setLogoFilePath(String logoFilePathIn) {
        this.logoFilePath = logoFilePathIn;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(logoUrl);
        dest.writeString(logoFilePath);
        dest.writeString(url);
        dest.writeLong(lastUpdate);
    }

    public static final Parcelable.Creator<Podcast> CREATOR = new Creator<Podcast>() {
        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }

        @Override
        public Podcast createFromParcel(Parcel source) {
            Podcast ret = new Podcast();
            ret.setId(source.readLong());
            ret.setTitle(source.readString());
            ret.setDescription(source.readString());
            ret.setLogoUrl(source.readString());
            ret.setLogoFilePath(source.readString());
            ret.setUrl(source.readString());
            ret.setLastUpdate(source.readLong());
            return ret;
        }
    };
}
