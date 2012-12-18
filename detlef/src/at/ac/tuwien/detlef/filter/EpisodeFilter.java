package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

public interface EpisodeFilter {
    boolean filter(Episode episode);
}
