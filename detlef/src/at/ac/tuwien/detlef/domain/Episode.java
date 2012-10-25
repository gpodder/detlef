package at.ac.tuwien.detlef.domain;

public class Episode {

    public static enum State {
        NEW, DOWNLOADED, PLAYED
    }

    private String name;
    private Podcast podcast;
    private Episode.State state;

    public String getName() {
        return name;
    }

    public Episode.State getState() {
        return state;
    }

    public void setState(Episode.State state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
    }

}
