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


package at.ac.tuwien.detlef.domain;

import net.x4a42.volksempfaenger.feedparser.Enclosure;
import net.x4a42.volksempfaenger.feedparser.FeedItem;

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

    public enum ActionState {
        DOWNLOAD,
        PLAY,
        DELETE,
        NEW
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
    private int playPosition = 0;
    private Episode.ActionState actionState;

    public Episode(Podcast podcast) {
        this.podcast = podcast;
        this.storageState = StorageState.NOT_ON_DEVICE;
        this.actionState = ActionState.NEW;
        this.playPosition = 0;
    }

    public Episode(Enclosure enclosure, Podcast podcast) {
        this(podcast);

        FeedItem item = enclosure.feedItem;

        setAuthor("Deprecated");
        setDescription(item.description);
        setFileSize(enclosure.size);
        setGuid(item.itemId);
        setLink(item.url);
        setMimetype(enclosure.mime);
        setReleased(item.date.getTime());
        setTitle(item.title);
        setUrl(enclosure.url);
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

    /**
     * Get the playback position in milliseconds.
     * @return
     */
    public int getPlayPosition() {
        return playPosition;
    }

    /**
     * Set the playback position in milliseconds.
     * @param playPosition
     * @return
     */
    public Episode setPlayPosition(int playPosition) {
        this.playPosition = playPosition;
        return this;
    }

    public Episode.ActionState getActionState() {
        return actionState;
    }

    public Episode setActionState(Episode.ActionState actionState) {
        this.actionState = actionState;
        return this;
    }
}
