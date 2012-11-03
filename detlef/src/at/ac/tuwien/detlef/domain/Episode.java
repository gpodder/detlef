
package at.ac.tuwien.detlef.domain;

import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

/**
 * Dummy class to display initial test episode content until the actual podcast
 * classes are available.
 */
public class Episode implements IEpisode {

    public static enum State {
        NEW, DOWNLOADED, PLAYED
    }

    private long id;

    private String title;

    private Podcast podcast;

    private Episode.State state;

    private String fileSize;

    private String author;

    private String description;

    private String mimetype; // enclosure

    private String guid;

    private String link;

    private long released;

    private String url;

    public String getTitle() {
        return title;
    }

    public Episode.State getState() {
        return state;
    }

    public void setState(Episode.State state) {
        this.state = state;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getReleased() {
        return released;
    }

    public void setReleased(long released) {
        this.released = released;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public IEnclosure getEnclosure() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
