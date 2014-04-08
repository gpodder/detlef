package at.ac.tuwien.detlef.gpodder.events;

import at.ac.tuwien.detlef.domain.Podcast;

public class PodcastInfoResultEvent {
    public final int code;
    public final Podcast podcast;
    public PodcastInfoResultEvent(int code, Podcast podcast) {
        this.code = code;
        this.podcast = podcast;
    }
}
