package at.ac.tuwien.detlef.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dragontek.mygpoclient.feeds.IFeed;

public class FeedUpdate implements IFeed {
    private final String title;
    private final String link;
    private final String description;
    private final String url;
    private final List<Episode> episodes;
    private final long lastRelease;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IEpisode[] getEpisodes() {
        IEpisode[] ret = new IEpisode[episodes.size()];

        int i = 0;
        for (IEpisode e : episodes) {
            ret[i++] = e;
        }

        return ret;
    }
    
    public List<Episode> getEpisodeList() {
        return Collections.unmodifiableList(episodes);
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }
    
    /**
     * Constructs a FeedUpdate containing all Episodes which were added to the podcast after its
     * last update time.
     * @param iFeed The original feed
     * @param podcast The podcast it belongs to.
     */
    public FeedUpdate(IFeed iFeed, Podcast podcast) {
        IEpisode[] iepisodes = iFeed.getEpisodes();
        episodes = new ArrayList<Episode>(iepisodes.length);
        long lastRelease = podcast.getLastUpdate();
        for (IEpisode ie : iepisodes) {
            // TODO: skip Episodes without enclosure, find a way to correctly handle this case.
            if (ie.getEnclosure() == null) {
                continue;
            }
            if (ie.getReleased() <= podcast.getLastUpdate()) {
                continue;
            }
            if (ie.getReleased() > lastRelease) {
                lastRelease = ie.getReleased();
            }
            episodes.add(new Episode(ie, podcast));
        }

        this.lastRelease = lastRelease;
        title = iFeed.getTitle();
        description = iFeed.getDescription();
        url = iFeed.getUrl();
        link = iFeed.getLink();
    }
    
    public long getLastReleaseTime() {
        return lastRelease;
    }
}
