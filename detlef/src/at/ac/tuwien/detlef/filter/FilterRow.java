package at.ac.tuwien.detlef.filter;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;

/**
 * An {@link EpisodeFilter} that implements the composite
 * pattern, i.e. that can contain multiple instances of
 * {@link EpisodeFilter} implementations but with one
 * little specialty: Each Type can only be contained 
 * once and will be replaced if added again.
 * @author moe
 *
 */
public class FilterRow implements EpisodeFilter {
    
    private Map<String, EpisodeFilter> filters = new HashMap<String, EpisodeFilter>();
    
    private static final String TAG = FilterRow.class.getCanonicalName();
    
    @Override
    public boolean filter(Episode episode) {
        
        Log.d(TAG, "filter(" + episode + ")");
        Log.d(TAG, "there are " + filters.size() + " filters");
        
        for (String key : filters.keySet()) {
            if (filters.get(key).filter(episode)) {
                Log.d(TAG, filters.get(key).getClass().getCanonicalName() + " returns true");
                return true;
            }
            Log.d(TAG, filters.get(key).getClass().getCanonicalName() + " returns false");
        }
        
        Log.d(TAG, "FilterRow returns false");
        return false;
    }
    
    public FilterRow putEpisodeFilter(EpisodeFilter episodeFilter) {
        filters.put(episodeFilter.getClass().getCanonicalName(), episodeFilter);
        return this;
    }
    
    public FilterRow removeEpisodeFilter(EpisodeFilter episodeFilter) {
        filters.remove(episodeFilter.getClass().getCanonicalName());
        return this;
    }

}
