package at.ac.tuwien.detlef.gpodder.events;

import java.util.List;

import at.ac.tuwien.detlef.domain.Podcast;

public class ToplistResultEvent {
    public int code;
    public List<Podcast> podcasts;

    public ToplistResultEvent(int code, List<Podcast> podcasts) {
        this.code = code;
        this.podcasts = podcasts;
    }
}
