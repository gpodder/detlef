package at.ac.tuwien.detlef.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.SearchListAdapter;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
import at.ac.tuwien.detlef.search.Search;
import at.ac.tuwien.detlef.search.SearchCallback;
import at.ac.tuwien.detlef.search.SearchCriteriaKeyword;
import at.ac.tuwien.detlef.search.SearchKeywordDb;

public class SearchActivity
    extends ListActivity
    implements EpisodeListFragment.OnEpisodeSelectedListener {

    private ArrayList<Episode> listItems = new ArrayList<Episode>();
    private SearchListAdapter adapter;
    
    private Search<SearchCriteriaKeyword, Episode> searchKeyword = new SearchKeywordDb();
    
    /**
     * A tag for the LogCat so everything this class produces can be filtered.
     */
    private static final String LOG_TAG = "search";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);
        
        Button searchButton = (Button) findViewById(R.id.ButtonSearch);
        
        searchButton.setOnClickListener(
            new OnClickListener() {

                @Override
                public void onClick(View v) {
                    
                    Log.d(LOG_TAG, "searchButton.onClick(" + v + ")");
                    
                    searchKeyword.search(
                        new SearchCriteriaKeyword().setKeyword(getSearchTerm()),
                        new SearchCallback<Episode>() {
                            @Override
                            public void getResult(List<Episode> result) {
                                listItems = new ArrayList<Episode>(result);
                                refreshSearchResult();
                            }
                        }
                    );
                }
            }
        );
        
        refreshSearchResult();
        
   
        
        registerForContextMenu(getListView());
    }
    
    private String getSearchTerm() {
        
        AutoCompleteTextView text = (AutoCompleteTextView) findViewById(R.id.searchBar);
        return text.getText().toString();
        
    }

    private void refreshSearchResult() {
        adapter = new SearchListAdapter(this, R.layout.search_list_layout, listItems);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_context, menu);
    }

    @Override
    public void onEpisodeSelected(Episode episode) {
        Log.d(LOG_TAG, "onEpisodeSelected(" + episode + ")");
        // TODO Auto-generated method stub
        
    }
}
