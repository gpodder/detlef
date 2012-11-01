package at.ac.tuwien.detlef;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class EpisodeListFragment extends ListFragment {

    private ArrayList<Episode> listItems = new ArrayList<Episode>();
    private EpisodeListAdapter adapter;
    int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.episode_list_layout);
        Podcast p1 = new Podcast();
        Podcast p2 = new Podcast();
        p1.setName("My Podcast 1");
        p2.setName("My Podcast 2");
        Episode e1 = new Episode();
        Episode e2 = new Episode();
        Episode e3 = new Episode();
        Episode e4 = new Episode();
        Episode e5 = new Episode();
        e1.setPodcast(p1);
        e2.setPodcast(p1);
        e3.setPodcast(p2);
        e4.setPodcast(p1);
        e5.setPodcast(p2);
        e1.setName("Episode 1");
        e2.setName("Episode 2");
        e3.setName("Episode 24");
        e4.setName("Episode 25");
        e5.setName("Episode 26");
        listItems.add(e1);
        listItems.add(e2);
        listItems.add(e3);
        listItems.add(e4);
        listItems.add(e5);
        adapter = new EpisodeListAdapter(getActivity(),
                android.R.layout.simple_list_item_1, listItems);
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
        inflater.inflate(R.menu.episode_context, menu);
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
        return inflater.inflate(R.layout.episode_fragment_layout, container,
                false);
    }
}
