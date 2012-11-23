package at.ac.tuwien.detlef.activities;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity.SectionsPagerAdapter;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.adapters.SearchListAdapter;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
import at.ac.tuwien.detlef.fragments.PlayerFragment;
import at.ac.tuwien.detlef.search.Search;
import at.ac.tuwien.detlef.search.SearchCallback;
import at.ac.tuwien.detlef.search.SearchCriteriaKeyword;
import at.ac.tuwien.detlef.search.SearchKeywordDb;

public class SearchActivity
    extends FragmentActivity
    implements EpisodeListFragment.OnEpisodeSelectedListener {

    //private ArrayList<Episode> listItems = new ArrayList<Episode>();
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
                                getEpisodeListFragment().setListAdapter(
                                        new EpisodeListAdapter(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1,
                                            result
                                        )
                                    );
                                
                                refreshSearchResult();
                            }
                        }
                    );
                }
            }
        );
        
        refreshSearchResult();
   
        
        //registerForContextMenu(getListView());
    }
    
    /**
     * Retrieves the currently active episode list fragment.
     */
    private EpisodeListFragment getEpisodeListFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return (EpisodeListFragment) manager.findFragmentById(R.id.SearchEpsiodeListFragment);
    }
    
    private String getSearchTerm() {
        
        AutoCompleteTextView text = (AutoCompleteTextView) findViewById(R.id.searchBar);
        return text.getText().toString();
        
    }

    private void refreshSearchResult() {
        //adapter = new SearchListAdapter(this, R.layout.search_list_layout, listItems);
        //setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_context, menu);
    }

    private PlayerFragment getPlayerFragment() {
        FragmentManager manager = getSupportFragmentManager();
        String tag = String.format("android:switcher:%d:%d", R.id.pager,
                SectionsPagerAdapter.POSITION_PLAYER);
        return (PlayerFragment) manager.findFragmentByTag(tag);
    }
    
    @Override
    public void onEpisodeSelected(Episode episode) {
        Log.d(LOG_TAG, "onEpisodeSelected(" + episode + ")");
        getPlayerFragment().setActiveEpisode(episode);
        getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_PLAYER);
        getPlayerFragment().startPlaying();
        
    }
}
