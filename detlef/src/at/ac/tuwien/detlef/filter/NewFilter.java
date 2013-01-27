package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;

/**
 * An {@link EpisodeFilter} that filters out all
 * {@link Episode episodes} that are not {@link ActionState#NEW new}.
 * @author moe
 */
public class NewFilter implements EpisodeFilter {

    /** serialization id. */
    private static final long serialVersionUID = 4318458607820746610L;

    /**
     * @return false if the {@link Episode episode's} {@link ActionState}
     * is {@link ActionState#NEW}, true else.
     */
    @Override
    public boolean filter(Episode episode) {
        return !episode.getActionState().equals(Episode.ActionState.NEW);
    }

    @Override
    public String getFilterName() {
        return getClass().getCanonicalName();
    }

}
