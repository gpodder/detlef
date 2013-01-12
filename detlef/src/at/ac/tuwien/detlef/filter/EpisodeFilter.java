package at.ac.tuwien.detlef.filter;

import java.io.Serializable;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * Determines if an {@link Episode} should be filtered or not.
 * @author moe
 *
 */
public interface EpisodeFilter extends Serializable {
    
    /**
     * Runs a check and returns a status that determines, if an
     * {@link Episode} should be filtered or not. 
     * @param episode The episode that should be examined.
     * @return true, if the {@link Episode} should be filtered (i.e.
     *     it does <strong>not</strong> match the filter's criteria),
     *     false else.
     */
    boolean filter(Episode episode);
    
    /**
     * The filter's name.
     * @return returns the name of the filter. Usually it will
     * absolutely be sufficient to return the {@link #getClass() class'}.
     * {@link Class#getCanonicalName() canonical name}, but under
     * special circumstance it might be useful to change this
     * to something else (think of testing).
     */
    String getFilterName();
}
