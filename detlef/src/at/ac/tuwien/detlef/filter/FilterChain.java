package at.ac.tuwien.detlef.filter;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * Allows to chain up a set of {@link EpisodeFilter EpisodeFilters}.
 * This is especially useful if you need to combine multiple filters.
 *
 * @author moe
 *
 */
public class FilterChain implements EpisodeFilter {

    /** serialization id */
    private static final long serialVersionUID = -2419542734907859360L;
    /**
     * The {@link Map} that contains the filters. The key is the
     * canonical name of the {@link EpisodeFilter}'s class. This
     * means that each type can only be contained once.
     */
    private final Map<String, EpisodeFilter> filters = new HashMap<String, EpisodeFilter>();

    /**
     * Goes through the set of provided {@link EpisodeFilters} and checks
     * for each of them, if {@link EpisodeFilter#filter(Episode)} returns
     * true. If there is only a single EpisodeFilter for which filter(Episode)
     * returns true, this method will return true. If for all filters in
     * the chain filter(Episode) returns false, this method returns false.
     */
    @Override
    public boolean filter(Episode episode) {

        for (String key : filters.keySet()) {
            if (filters.get(key).filter(episode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds an {@link EpisodeFilter} to the chain. If there exists
     * already an EpsiodeFilter with the same name (determined by
     * {@link EpisodeFilter#getFilterName()}), then the existing filter
     * will be overwritten by the new one.
     * @param episodeFilter The {@link EpisodeFilter} to put into the
     * list.
     * @return Fluent Interface.
     */
    public FilterChain putEpisodeFilter(EpisodeFilter episodeFilter) {
        filters.put(episodeFilter.getFilterName(), episodeFilter);
        return this;
    }

    /**
     * Gets an {@link EpisodeFilter} by passing another instance of
     * EpisodeFilter.
     * @param episodeFilter
     * @return the EpisodeFilter or null, if no filter with the given type
     * exists in the chain. If the result is not null, the returned
     * EpisodeFilter must be of the same type as the one passed as
     * parameter.
     */
    public EpisodeFilter getEpisodeFilterByType(EpisodeFilter episodeFilter) {

        return filters.get(episodeFilter.getFilterName());

    }

    /**
     * Calls {@link #removeEpisodeFilter(String)} with the episode's
     * {@link EpisodeFilter#getFilterName() filter name} as parameter.
     * @see #removeEpisodeFilter(String)
     * @param episodeFilter
     * @return Fluent Interface
     */
    public FilterChain removeEpisodeFilter(EpisodeFilter episodeFilter) {
        return removeEpisodeFilter(episodeFilter.getFilterName());
    }

    /**
     * Removes an {@link EpisodeFilter} from the chain. You do not
     * need to provide the very same instance, but only an instance
     * that has the same {@link EpisodeFilter#getFilterName() filter name}.
     * E.g. a call with <code>filterChain.removeEpisodeFilter(new NullFilter())</code>
     * will remove any other instance of {@link NullFilter} in the chain, because
     * there only can exist one instance of any distinct filter name in the chain. If
     * no filter with the specified name exists, nothing will happen.
     * @param filterName The {@link EpisodeFilter} that should
     * be removed from the chain.
     * @return Fluent Interface.
     */

    public FilterChain removeEpisodeFilter(String filterName) {
        filters.remove(filterName);
        return this;
    }

    /**
     * Returns whether the list of filters contains an {@link EpisodeFilter}
     * of the same type as passed on via the parameter.
     * @param filter The EpisodeFilter to check for. This needs not to be
     * the very same instance, but only the same type.
     * @return true if a filter of the given type is contained, false else.
     */
    public boolean contains(EpisodeFilter filter) {
        return filters.containsKey(filter.getFilterName());
    }

    /**
     * @return The number of filters currently in the chain. If no
     * filters are contained, return 0.
     */
    public int countFilters() {
        return filters.size();
    }

    @Override
    public String getFilterName() {
        return getClass().getCanonicalName();
    }

}
