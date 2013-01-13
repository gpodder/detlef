package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

public class NullFilter implements EpisodeFilter {

    /** serialization id. */
    private static final long serialVersionUID = 5494335824388147984L;

    @Override
    public boolean filter(Episode episode) {
        return false;
    }
    
    @Override
    public String getFilterName() {
        return getClass().getCanonicalName();
    }
}
