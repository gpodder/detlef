package at.ac.tuwien.detlef.ui;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.ui.adapters.PlaylistListAdapter;

import com.mobeta.android.dslv.DragSortListView;

public class PlaylistActivity extends ListActivity {

    private ArrayList<Episode> listItems = new ArrayList<Episode>();
    private PlaylistListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity_layout);

        DragSortListView lv = (DragSortListView) getListView();

        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        Podcast p1 = new Podcast();
        Podcast p2 = new Podcast();
        p1.setName("My Podcast 1");
        p2.setName("My Podcast 2");
        Episode e1 = new Episode();
        Episode e2 = new Episode();
        Episode e3 = new Episode();
        Episode e4 = new Episode();
        Episode e5 = new Episode();
        Episode e6 = new Episode();
        Episode e7 = new Episode();
        Episode e8 = new Episode();
        e1.setPodcast(p1);
        e2.setPodcast(p1);
        e3.setPodcast(p2);
        e4.setPodcast(p1);
        e5.setPodcast(p2);
        e6.setPodcast(p1);
        e7.setPodcast(p2);
        e8.setPodcast(p1);
        e1.setName("Episode 1");
        e2.setName("Episode 2");
        e3.setName("Episode 24");
        e4.setName("Episode 25");
        e5.setName("Episode 569326");
        e6.setName("Episode 26");
        e7.setName("Episode 259");
        e8.setName("Episode 26234");
        listItems.add(e1);
        listItems.add(e2);
        listItems.add(e3);
        listItems.add(e4);
        listItems.add(e5);
        listItems.add(e6);
        listItems.add(e7);
        listItems.add(e8);
        adapter = new PlaylistListAdapter(this, R.layout.playlist_list_layout,
                listItems);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        public void drop(int from, int to) {
            Episode item = adapter.getItem(from);

            adapter.remove(item);
            adapter.insert(item, to);
        }
    };

    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        public void remove(int which) {
            adapter.remove(adapter.getItem(which));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_context, menu);
    }
}
