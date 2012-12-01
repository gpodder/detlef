
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

public class PlaylistActivity extends ListActivity implements PlaylistDAO.OnPlaylistChangeListener {

    private ArrayList<Episode> playlistItems;
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
        playlistItems = playlistDAO.getNonCachedEpisodes();
        adapter = new PlaylistListAdapter(this, R.layout.playlist_list_layout,
                playlistItems);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        playlistDAO.addPlaylistChangedListener(this);
    }

    private final DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            playlistDAO.moveEpisode(from, to);
        }
    };

    private final DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            playlistDAO.removeEpisode(which);
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

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        playlistItems.add(position, episode);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition) {
        Episode ep = playlistItems.remove(firstPosition);
        playlistItems.add(secondPosition, ep);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistEpisodeRemoved(int position) {
        playlistItems.remove(position);
        adapter.notifyDataSetChanged();
    }

    public void removeFromPlaylist(View v) {
        int position = (Integer) v.getTag();
        playlistDAO.removeEpisode(position);
    }
}
