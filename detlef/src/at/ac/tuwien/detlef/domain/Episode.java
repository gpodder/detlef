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

    private String filePath;

    public String getTitle() {
        return title;
    }

    public Episode.State getState() {
        return state;
    }

    public Episode setState(Episode.State state) {
        this.state = state;
        return this;
    }

    public Episode setTitle(String title) {
        this.title = title;
        return this;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public Episode setPodcast(Podcast podcast) {
        this.podcast = podcast;
        return this;
    }

    public String getFileSize() {
        return fileSize;
    }

    public Episode setFileSize(String fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Episode setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Episode setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getGuid() {
        return guid;
    }

    public Episode setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Episode setLink(String link) {
        this.link = link;
        return this;
    }

    public long getReleased() {
        return released;
    }

    public Episode setReleased(long released) {
        this.released = released;
        return this;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Episode setMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public IEnclosure getEnclosure() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUrl() {
        return url;
    }

    public Episode setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getId() {
        return id;
    }

    public Episode setId(long id) {
        this.id = id;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public Episode setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }
}
