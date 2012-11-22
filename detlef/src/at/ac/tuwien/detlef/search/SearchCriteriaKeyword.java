package at.ac.tuwien.detlef.search;

/**
 * A simple keyword which is used as search criteria.
 * 
 * @author moe
 */
public class SearchCriteriaKeyword implements SearchCriteria {
    
    private String keyword;

    public SearchCriteriaKeyword setKeyword(String pKeyword) {
        keyword = pKeyword;
        return this;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
}
