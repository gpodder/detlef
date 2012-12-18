package at.ac.tuwien.detlef.filter;

import at.ac.tuwien.detlef.domain.Episode;

public class KeywordFilter implements EpisodeFilter {

    private String keyword = null;

    public KeywordFilter setKeyword(String pKeyword) {
        
        if (pKeyword.isEmpty()) {
            pKeyword = null;
        } else {
            pKeyword = pKeyword.toLowerCase();
        }
        
        keyword = pKeyword;
        return this;
    }
    
    @Override
    public boolean filter(Episode episode) {
        
        if (keyword == null) {
            return false;
        }
            
        return !(episode.getTitle().toLowerCase().contains(keyword) || episode.getDescription().toLowerCase().contains(keyword));
    }

}
