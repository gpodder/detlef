package at.ac.tuwien.detlef.domain;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Dummy class to display initial test podcast content
 * until the actual podcast classes are available.
 */
public class Podcast implements IPodcast {

    private String title = "This is a podcast title";
    private String description = "This is a podcast description.";
    private String logoUrl = "www.dummyurl.com";
    private final String url = "www.dummyurl.com";

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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;

    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

}
