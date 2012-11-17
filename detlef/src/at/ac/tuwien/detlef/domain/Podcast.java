package at.ac.tuwien.detlef.domain;

import java.io.Serializable;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Dummy class to display initial test podcast content until the actual podcast
 * classes are available.
 */
public class Podcast implements IPodcast, Serializable {

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

}
