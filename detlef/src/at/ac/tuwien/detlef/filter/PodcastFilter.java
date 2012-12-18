package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodcastFilter implements EpisodeFilter {

    private Podcast podcast = null;

    public PodcastFilter setPodcast(Podcast pPodcast) {
        
        podcast = pPodcast;
        return this;
    }
    
    @Override
    public boolean filter(Episode episode) {
        
        if (podcast == null) {
            return false;
        }
        
        return episode.getPodcast().getId() != podcast.getId();
        
    }

}
