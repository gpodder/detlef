package at.ac.tuwien.detlef.domain;

import java.io.Serializable;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Dummy class to display initial test podcast content until the actual podcast
 * classes are available.
 */
public class Podcast implements IPodcast, Serializable {

    private long id;

    private String title = "This is a podcast title";

    private String description = "This is a podcast description.";

    private String logoUrl = "www.dummyurl.com";

    private String logoFilePath;

    private String url = "www.dummyurl.com";

    private long lastUpdate;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getUrl() {
        return url;
    }

    public Podcast setUrl(String url) {
        this.url = url;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;

    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public long getId() {
        return id;
    }

    public Podcast setId(long id) {
        this.id = id;
        return this;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public Podcast setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public String getLogoFilePath() {
        return logoFilePath;
    }

    public Podcast setLogoFilePath(String logoFilePath) {
        this.logoFilePath = logoFilePath;
        return this;
    }

}
