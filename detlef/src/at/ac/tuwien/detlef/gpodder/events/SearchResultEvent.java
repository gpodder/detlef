package at.ac.tuwien.detlef.gpodder.events;

import java.util.List;

import at.ac.tuwien.detlef.domain.Podcast;

public class SearchResultEvent {
    public int code;
    public List<Podcast> podcasts;

    public SearchResultEvent(int code, List<Podcast> podcasts) {
        this.code = code;
        this.podcasts = podcasts;
    }
}
