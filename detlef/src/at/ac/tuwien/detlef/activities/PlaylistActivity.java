/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */

package at.ac.tuwien.detlef.activities;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.adapters.PlaylistListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.EpisodePersistence;
import at.ac.tuwien.detlef.download.DetlefDownloadManager;
import at.ac.tuwien.detlef.gpodder.events.PlaylistChangedEvent;
import at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;
import at.ac.tuwien.detlef.util.GUIUtils;

import com.mobeta.android.dslv.DragSortListView;

import de.greenrobot.event.EventBus;

public class PlaylistActivity extends ListActivity implements EpisodeDAO.OnEpisodeChangeListener {

    private ArrayList<Episode> playlistItems;
    private PlaylistListAdapter adapter;
    private PlaylistDAO playlistDAO;
    private DetlefDownloadManager downloadManager;

    private boolean bound;
    private IMediaPlayerService service;

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            bound = true;
            Log.d(getClass().getName(), "Service connected to playlistactivity");
            MediaPlayerService.MediaPlayerBinder binder =
                (MediaPlayerService.MediaPlayerBinder) iBinder;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity_layout);

        playlistDAO = Singletons.i().getPlaylistDAO();
        EventBus.getDefault().register(this, PlaylistChangedEvent.class);
        playlistItems = playlistDAO.getNonCachedEpisodes();
        Singletons.i().getEpisodeDAO().addEpisodeChangedListener(this);

        downloadManager = Singletons.i().getDownloadManager(
                              Detlef.getAppContext());

        initListView();
        registerForContextMenu(getListView());
        connectToMediaService();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void initListView() {
        DragSortListView lv = (DragSortListView) getListView();
        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);
        adapter = new PlaylistListAdapter(this, R.layout.playlist_list_layout,
                                          playlistItems);
        setListAdapter(adapter);
    }

    private void connectToMediaService() {
        if (!MediaPlayerService.isRunning()) {
            Intent serviceIntent =
                new Intent(Detlef.getAppContext(), MediaPlayerService.class);
            Detlef.getAppContext().startService(serviceIntent);
        }
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private final DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            playlistDAO.moveEpisode(from, to);
            EventBus.getDefault().post(new PlaylistChangedEvent());
        }
    };

    private final DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            playlistDAO.removeEpisode(which);
            EventBus.getDefault().post(new PlaylistChangedEvent());
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

    public void onEventMainThread(PlaylistChangedEvent event) {
        playlistItems.clear();
        playlistItems.addAll(playlistDAO.getNonCachedEpisodes());
        adapter.notifyDataSetChanged();
    }

    public void removeFromPlaylist(View v) {
        int position = (Integer) v.getTag();
        playlistDAO.removeEpisode(position);
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.playlistClear:
            playlistClear();
            break;
        case R.id.playlistDownloadAll:
            playlistDownloadAll();
            break;
        case R.id.playlistStopDownload:
            playlistStopPlaylistDownloads();
            break;
        case R.id.playlistStopAllDownloads:
            stopAllDownloads();
            break;
        case R.id.settings:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            break;
        default:
            break;
        }
        return true;
    }

    private void stopAllDownloads() {
        downloadManager.cancelAll();
    }

    /**
     * Stops all the current downloads in the playlist.
     */
    private void playlistStopPlaylistDownloads() {
        for (int i = 0; i < playlistItems.size(); i++) {
            try {
                Episode ep = playlistItems.get(i);
                if (ep.getStorageState() == StorageState.DOWNLOADING) {
                    downloadManager.cancel(ep);
                }
            } catch (Exception e) {
                Log.d(getClass().getName(), "Could not cancel episode download " + i
                      + " on playlist");
            }
        }
    }

    /**
     * Adds all items on the playlist to the downloader
     */
    private void playlistDownloadAll() {
        for (int i = 0; i < playlistItems.size(); i++) {
            try {
                Episode ep = playlistItems.get(i);
                if (ep.getStorageState() == StorageState.NOT_ON_DEVICE) {
                    EpisodePersistence.download(playlistItems.get(i));
                }
            } catch (Exception e) {
                // TODO @Joshi show that episodes are being downloaded somehow?
                Log.d(getClass().getName(), "Could not add episode " + i
                      + " on playlist to download manager");
            }
        }
    }

    /**
     * Clears the playlist.
     */
    private void playlistClear() {
        Log.d(getClass().getName(), "Clearing playlist");
        playlistDAO.clearPlaylist();
        EventBus.getDefault().post(new PlaylistChangedEvent());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(getClass().getName(), "List item " + position + " clicked");
        if (service == null) {
            return;
        }
        service.skipToPosition(position);
        service.startPlaying();
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
    }

    @Override
    public void onEpisodeDeleted(Episode episode) {
        // we get this automatically from the playlistDAO
    }

    public void downloadEpisode(View v) {
        Episode episode = ((Episode) v.getTag());
        String tag = getClass().getName();
        switch (episode.getStorageState()) {
        case NOT_ON_DEVICE:
            try {
                GUIUtils.showToast(String.format("Downloading %s", episode.getTitle()),
                                   this, tag);
                EpisodePersistence.download(episode);
            } catch (IOException e) {
                GUIUtils.showToast(getString(R.string.cannot_download_episode),
                                   this, tag);
            }
            break;
        case DOWNLOADING:
            GUIUtils.showToast("Download aborted", this, tag);
            EpisodePersistence.cancelDownload(episode);
            break;
        default:
            Log.e(tag, "Invalid storage state encountered");
        }
    }
}
