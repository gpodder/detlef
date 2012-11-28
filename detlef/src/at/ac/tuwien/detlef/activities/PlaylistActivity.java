
package at.ac.tuwien.detlef.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.PlaylistListAdapter;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

import com.mobeta.android.dslv.DragSortListView;

public class PlaylistActivity extends ListActivity {

    private final ArrayList<Episode> listItems = new ArrayList<Episode>();
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
        p1.setTitle("My Podcast 1");
        p2.setTitle("My Podcast 2");
        Episode e1 = new Episode(p1);
        Episode e2 = new Episode(p1);
        Episode e3 = new Episode(p2);
        Episode e4 = new Episode(p1);
        Episode e5 = new Episode(p2);
        Episode e6 = new Episode(p1);
        Episode e7 = new Episode(p2);
        Episode e8 = new Episode(p1);
        e1.setTitle("Episode 1");
        e2.setTitle("Episode 2");
        e3.setTitle("Episode 24");
        e4.setTitle("Episode 25");
        e5.setTitle("Episode 569326");
        e6.setTitle("Episode 26");
        e7.setTitle("Episode 259");
        e8.setTitle("Episode 26234");
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

    private final DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            Episode item = adapter.getItem(from);

            adapter.remove(item);
            adapter.insert(item, to);
        }
    };

    private final DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
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
