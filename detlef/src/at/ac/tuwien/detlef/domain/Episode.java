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

    public Episode setState(Episode.State stateIn) {
        this.state = stateIn;
        return this;
    }

    public Episode setTitle(String titleIn) {
        this.title = titleIn;
        return this;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public Episode setPodcast(Podcast podcastIn) {
        this.podcast = podcastIn;
        return this;
    }

    public String getFileSize() {
        return fileSize;
    }

    public Episode setFileSize(String fileSizeIn) {
        this.fileSize = fileSizeIn;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Episode setAuthor(String authorIn) {
        this.author = authorIn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Episode setDescription(String descriptionIn) {
        this.description = descriptionIn;
        return this;
    }

    public String getGuid() {
        return guid;
    }

    public Episode setGuid(String guidIn) {
        this.guid = guidIn;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Episode setLink(String linkIn) {
        this.link = linkIn;
        return this;
    }

    public long getReleased() {
        return released;
    }

    public Episode setReleased(long releasedIn) {
        this.released = releasedIn;
        return this;
    }

    public String getMimetype() {
        return mimetype;
    }

    public Episode setMimetype(String mimetypeIn) {
        this.mimetype = mimetypeIn;
        return this;
    }

    public IEnclosure getEnclosure() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUrl() {
        return url;
    }

    public Episode setUrl(String urlIn) {
        this.url = urlIn;
        return this;
    }

    public long getId() {
        return id;
    }

    public Episode setId(long idIn) {
        this.id = idIn;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public Episode setFilePath(String filePathIn) {
        this.filePath = filePathIn;
        return this;
    }
}
