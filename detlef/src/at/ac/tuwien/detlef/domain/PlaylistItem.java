
package at.ac.tuwien.detlef.domain;

public class PlaylistItem {

    private long id;
    private final Episode episode;

    public PlaylistItem(Episode episode) {
        this.episode = episode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Episode getEpisode() {
        return episode;
    }

}
