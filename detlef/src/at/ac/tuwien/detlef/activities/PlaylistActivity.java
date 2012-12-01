
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
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;

import com.mobeta.android.dslv.DragSortListView;

public class PlaylistActivity extends ListActivity {

    private final ArrayList<Episode> playlistItems = new ArrayList<Episode>();
    private PlaylistListAdapter adapter;
    private PlaylistDAO playlistDAO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity_layout);
        
        playlistDAO = PlaylistDAOImpl.i();

        DragSortListView lv = (DragSortListView) getListView();

        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        adapter = new PlaylistListAdapter(this, R.layout.playlist_list_layout,
                playlistItems);
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
