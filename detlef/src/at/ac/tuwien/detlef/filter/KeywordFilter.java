package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

/**
 * An {@link EpisodeFilter} that filters by a keyword.
 * @author moe
 */
public class KeywordFilter implements EpisodeFilter {

    /** serialization id. */
    private static final long serialVersionUID = -7024572083865033288L;
    /**
     * The keyword to filter for in lower case.
     */
    private String keyword = null;

    /**
     * Sets the keyword.
     * @param pKeyword The keyword to set. If this parameter
     * {@link String#isEmpty() is empty} or null, then
     * no {@link Episode} will be filtered.
     * @return Fluent Interface
     */
    public KeywordFilter setKeyword(String pKeyword) {
        
        if (pKeyword.isEmpty()) {
            pKeyword = null;
        } else {
            pKeyword = pKeyword.toLowerCase();
        }
        
        keyword = pKeyword;
        return this;
    }
    
    /**
     * @return false, if the Episode's {@link Episode#getTitle() title}
     *     or the {@link Episode#getDescription() description} contains
     *     the {@link #setKeyword(String) keyword}, true else. This method
     *     is case insensitive, i.e. it does not matter if the keyword
     *     and the actual title and description have the same cases. 
     */
    @Override
    public boolean filter(Episode episode) {
        
        if (keyword == null) {
            return false;
        }
            
        String index = String.format(
            "%s %s",
            episode.getTitle() != null ? episode.getTitle() : "",
            episode.getDescription() != null ? episode.getDescription() : ""
        );
        
        return !(index.toLowerCase().contains(keyword));
    }

    @Override
    public String getFilterName() {
        return getClass().getCanonicalName();
    }

    /**
     * @return The current keyword or null if no keyword is set.
     */
    public String getKeyword() {
        return keyword;
    }

}
