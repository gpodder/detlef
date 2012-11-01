package at.ac.tuwien.detlef.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.ui.adapters.PodListAdapter;

public class PodListFragment extends ListFragment {

    private ArrayList<Podcast> listItems = new ArrayList<Podcast>();
    private PodListAdapter adapter;
    int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Podcast all = new Podcast();
        Podcast p1 = new Podcast();
        Podcast p2 = new Podcast();
        Podcast p3 = new Podcast();
        Podcast p4 = new Podcast();
        all.setName("All Podcasts");
        p1.setName("My Podcast 1");
        p2.setName("My Podcast 2");
        p3.setName("My Podcast 3");
        p4.setName("My Podcast 4");
        listItems.add(all);
        listItems.add(p1);
        listItems.add(p2);
        listItems.add(p3);
        listItems.add(p4);
        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout,
                listItems);
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.podcast_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return super.onContextItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.pod_fragment_layout, container, false);
    }
}
