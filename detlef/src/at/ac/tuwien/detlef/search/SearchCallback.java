package at.ac.tuwien.detlef.search;

import java.util.List;

/**
 * 
 * @author moe
 *
 * @param <T> The type of object that is searched.
 */
public interface SearchCallback<T> {
    
    /**
     * This method is called when a search called via {@link Search#search(String, SearchCallback)}
     * has been finished. 
     * @param result The final results of the search.
     */
    void getResult(List<T> result);
    
}
