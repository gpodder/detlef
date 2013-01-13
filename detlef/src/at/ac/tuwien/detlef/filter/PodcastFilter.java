package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * An {@link EpisodeFilter} that filters out all
 * {@link Episode episodes} that do not belong to a given
 * {@link Podcast}.
 * @author moe
 */
public class PodcastFilter implements EpisodeFilter {

    /** serialization id. */
    private static final long serialVersionUID = 6156719201776953724L;
    /**
     * The {@link Podcast} to be filtered.
     */
    private Podcast podcast = null;

    /**
     * Sets the {@link Podcast} to be filtered. 
     * @param pPodcast The {@link Podcast} or null, if nothing
     *     should be filtered.
     * @return Fluent Interface
     */
    public PodcastFilter setPodcast(Podcast pPodcast) {
        
        podcast = pPodcast;
        return this;
    }
    
    /**
     * @return false, if the
     * {@link #setPodcast(Podcast) given Podcast's}
     * {@link Podcast#getId() id} is the same as the
     * {@link Episode#getPodcast() Episode's Podcast}
     * id, or the set Podcast is null; true else.
     */
    @Override
    public boolean filter(Episode episode) {
        
        if (podcast == null) {
            return false;
        }
        
        if (episode.getPodcast() == null) {
            return true;
        }
        
        return episode.getPodcast().getId() != podcast.getId();
        
    }
    
    @Override
    public String getFilterName() {
        return getClass().getCanonicalName();
    }

}
