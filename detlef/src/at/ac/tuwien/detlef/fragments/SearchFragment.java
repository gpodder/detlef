package at.ac.tuwien.detlef.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.search.Search;
import at.ac.tuwien.detlef.search.SearchCallback;
import at.ac.tuwien.detlef.search.SearchCriteriaKeyword;
import at.ac.tuwien.detlef.search.SearchKeywordDb;

public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getCanonicalName();

    private Search<SearchCriteriaKeyword, Episode> searchKeyword = new SearchKeywordDb();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate(" + savedInstanceState + ")");
        
        //refreshSearchResult();
   
        
        //registerForContextMenu(getListView());
    }
    
    private String getSearchTerm() {
        AutoCompleteTextView text = 
                (AutoCompleteTextView) getActivity().findViewById(R.id.searchBar);
        return text.getText().toString();
        
    }
    
    private EpisodeListFragment getEpisodeListFragment() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        return (EpisodeListFragment) manager.findFragmentById(R.id.SearchEpsiodeListFragment);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        Log.d(TAG, "onCreateView(" + inflater + "," + container + ")");
        
        View result = inflater.inflate(R.layout.search_activity_layout, container,
                false);
        
        Button searchButton = (Button) result.findViewById(R.id.ButtonSearch);
        Log.d(TAG, "searchButton =" + searchButton);

        searchButton.setOnClickListener(
            new OnClickListener() {

                @Override
                public void onClick(View v) {
                    
                    Log.d(TAG, "searchButton.onClick(" + v + ")");
                    
                    searchKeyword.search(
                        new SearchCriteriaKeyword().setKeyword(getSearchTerm()),
                        new SearchCallback<Episode>() {
                            @Override
                            public void getResult(List<Episode> result) {
                                getEpisodeListFragment().setListAdapter(
                                        new EpisodeListAdapter(
                                            getActivity().getApplicationContext(),
                                            android.R.layout.simple_list_item_1,
                                            result
                                        )
                                    );
                                
                                //refreshSearchResult();
                            }
                        }
                    );
                }
            }
        );
        
        return result;
    }
    
}
