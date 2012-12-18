package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

public class NewFilter implements EpisodeFilter {

    @Override
    public boolean filter(Episode episode) {        
        return !episode.getActionState().equals(Episode.ActionState.NEW);
    }

}
