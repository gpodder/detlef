package at.ac.tuwien.detlef.search;

import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.db.DatabaseHelper;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;

/**
 * Provides an interface for a search functionality which takes a
 * {@link SearchCriteriaKeyword keyword} as {@link SearchCriteria} and searches for
 * it in the database.
 * 
 * @author moe
 */
public class SearchKeywordDb 
    implements Search<SearchCriteriaKeyword, Episode> {

    private EpisodeDAO edao;
    
    public SearchKeywordDb() {
        edao = EpisodeDAOImpl.i();
    }
    
    @Override
    public void search(final SearchCriteriaKeyword criteria,
            final SearchCallback<Episode> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String selection = String.format(
                    "%s LIKE ? OR %s LIKE ?",
                    DatabaseHelper.COLUMN_EPISODE_TITLE,
                    DatabaseHelper.COLUMN_EPISODE_DESCRIPTION
                );
                String[] selectionArgs = {
                    String.format("%%%s%%", criteria.getKeyword()),
                    String.format("%%%s%%", criteria.getKeyword())
                };
                callback.getResult(edao.getEpisodesWhere(selection, selectionArgs));
            }
        }
        ).run();
    }
    
    


    
}
