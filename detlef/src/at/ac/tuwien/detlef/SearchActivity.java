package at.ac.tuwien.detlef;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import at.ac.tuwien.detlef.adapters.SearchListAdapter;
import at.ac.tuwien.detlef.domain.Podcast;

public class SearchActivity extends ListActivity {

    private ArrayList<Podcast> listItems = new ArrayList<Podcast>();
    private SearchListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);

        Podcast p1 = new Podcast();
        Podcast p2 = new Podcast();
        Podcast p3 = new Podcast();
        Podcast p4 = new Podcast();
        p1.setName("Search Result 1");
        p2.setName("Search Result 2");
        p3.setName("Search Result 3");
        p4.setName("Search Result 4");
        listItems.add(p1);
        listItems.add(p2);
        listItems.add(p3);
        listItems.add(p4);
        adapter = new SearchListAdapter(this, R.layout.search_list_layout,
                listItems);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_context, menu);
    }
}
