package at.ac.tuwien.detlef.domain;

import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

/**
 * Dummy class to display initial test episode content until the actual podcast
 * classes are available.
 */
public class Episode implements IEpisode {

    /**
     * The storage state of an episode. It can be either
     * not stored on the device, currently downloading, or
     * completely downloaded.
     */
    public enum StorageState {
        NOT_ON_DEVICE,
        DOWNLOADING,
        DOWNLOADED
    }

    private long id;
    private String title;
    private final Podcast podcast;
    private Episode.StorageState storageState;
    private long fileSize;
    private String author;
    private String description;
    private String mimetype; // enclosure
    private String guid;
    private String link;
    private long released;
    private String url;
    private String filePath;

    public Episode(Podcast podcast) {
        this.podcast = podcast;
        this.storageState = StorageState.NOT_ON_DEVICE;
    }

    public Episode(IEpisode e, Podcast podcast) {
        this(podcast);
        setAuthor(e.getAuthor());
        setDescription(e.getDescription());
        setFileSize(e.getEnclosure().getFilesize());
        setGuid(e.getGuid());
        setLink(e.getLink());
        setMimetype(e.getEnclosure().getMimetype());
        setReleased(e.getReleased());
        setTitle(e.getTitle());
        setUrl(e.getEnclosure().getUrl());
    }

    @Override
    public String getTitle() {
        return title;
    }

    public Episode.StorageState getStorageState() {
        return storageState;
    }

    public Episode setStorageState(Episode.StorageState storageState) {
        this.storageState = storageState;
        return this;
    }

    public Episode setTitle(String titleIn) {
        this.title = titleIn;
        return this;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Episode setFileSize(long fileSizeIn) {
        this.fileSize = fileSizeIn;
        return this;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    public Episode setAuthor(String authorIn) {
        this.author = authorIn;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Episode setDescription(String descriptionIn) {
        this.description = descriptionIn;
        return this;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public Episode setGuid(String guidIn) {
        this.guid = guidIn;
        return this;
    }

    @Override
    public String getLink() {
        return link;
    }

    public Episode setLink(String linkIn) {
        this.link = linkIn;
        return this;
    }

    @Override
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

    @Override
    public IEnclosure getEnclosure() {
        return new IEnclosure() {
            
            @Override
            public long getFilesize() {
                return Episode.this.getFileSize();
            }

            @Override
            public String getMimetype() {
                return Episode.this.getMimetype();
            }

            @Override
            public String getUrl() {
                return Episode.this.getUrl();
            }
            
        };
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
