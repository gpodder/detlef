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

package at.ac.tuwien.detlef.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.EpisodeDBAssistantImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.EpisodePersistence;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.models.EpisodeListModel;
import at.ac.tuwien.detlef.util.GUIUtils;

public class EpisodeListFragment extends ListFragment
        implements EpisodeDAO.OnEpisodeChangeListener {

    private static final String TAG = EpisodeListFragment.class.getName();
    private static final String BUNDLE_SELECTED_PODCAST = "BUNDLE_SELECTED_PODCAST";
    private static final long ID_NONE = -1;

    private EpisodeListModel model;
    private EpisodeListAdapter adapter;
    private Podcast filteredByPodcast = null;
    private OnEpisodeSelectedListener listener;
    private EpisodeDAO episodeDAO;

    private GUIUtils guiUtils;

    /**
     * The parent activity must implement this interface in order to interact
     * with this fragment. The listener is called whenever an episode is
     * clicked.
     */
    public interface OnEpisodeSelectedListener {
        void onEpisodeSelected(Episode episode);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnEpisodeSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s",
                    activity.toString(),
                    OnEpisodeSelectedListener.class.getName()));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EpisodeDAOImpl dao = EpisodeDAOImpl.i();
        dao.addEpisodeChangedListener(this);

        List<Episode> eplist = dao.getAllEpisodes();
        model = new EpisodeListModel(eplist);

        adapter = new EpisodeListAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                new ArrayList<Episode>(eplist));
        setListAdapter(adapter);

        guiUtils = DependencyAssistant.getDependencyAssistant().getGuiUtils();

        episodeDAO = EpisodeDAOImpl.i();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.episode_fragment_layout, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());

        /* Restore selected podcast. */

        if (savedState != null) {
            long id = savedState.getLong(BUNDLE_SELECTED_PODCAST, ID_NONE);
            if (id != ID_NONE) {
                PodcastDAOImpl dao = PodcastDAOImpl.i();
                filteredByPodcast = dao.getPodcastById(id);
                filterByPodcast();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /*
         * Save the currently selected podcast's ID in order to be able to
         * restore filter settings later on.
         */

        long id = (filteredByPodcast == null ? ID_NONE : filteredByPodcast.getId());
        outState.putLong(BUNDLE_SELECTED_PODCAST, id);
    }

    @Override
    public void onDestroy() {
        EpisodeDAOImpl dao = EpisodeDAOImpl.i();
        dao.removeEpisodeChangedListener(this);

        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Episode episode = (Episode) v.getTag();
        listener.onEpisodeSelected(episode);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.episode_context, menu);
    }

    /**
     * Called whenever a podcast is clicked in the PodListFragment. Filters the
     * episode list to display only episodes belonging to the specified podcast.
     * If podcast is null, all episodes are shown.
     */
    public void setPodcast(Podcast podcast) {
        boolean selectionChanged = false;
        if (filteredByPodcast != podcast) {
            selectionChanged = true;
        }

        filteredByPodcast = podcast;
        filterByPodcast();

        /* Scroll to the top of the list. */
        if (selectionChanged) {
            setSelection(0);
        }
    }

    private void filterByPodcast() {
        adapter.clear();
        if (filteredByPodcast == null) {
            adapter.addAll(model.getAll());
        } else {
            adapter.addAll(model.getByPodcast(filteredByPodcast));
        }
    }

    private void filterByPodcastOnUiThread() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filterByPodcast();
            }
        });
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        updateEpisodeList();
    }

    @Override
    public void onEpisodeAdded(final Episode episode) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                model.addEpisode(episode);
            }
        });
        filterByPodcastOnUiThread();
    }

    @Override
    public void onEpisodeDeleted(final Episode episode) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                model.removeEpisode(episode);
            }
        });
        filterByPodcastOnUiThread();
    }

    /**
     * Updates the displayed list based on the current model contents. Ensures
     * that UI methods are called on the UI thread.
     */
    private void updateEpisodeList() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Handles a click on the combined start download / abort download / delete
     * episode button. The action taken depends on the episode's storage state.
     */
    public void onDownloadTrashClick(View v) {
        Episode episode = ((Episode) v.getTag());
        switch (episode.getStorageState()) {
            case NOT_ON_DEVICE:
                try {
                    guiUtils.showToast(String.format("Downloading %s", episode.getTitle()),
                            getActivity(), TAG);
                    EpisodePersistence.download(episode);
                } catch (IOException e) {
                    guiUtils.showToast(getActivity().getString(R.string.cannot_download_episode),
                            getActivity(), TAG);
                }
                break;
            case DOWNLOADING:
                guiUtils.showToast("Download aborted", getActivity(), TAG);
                EpisodePersistence.cancelDownload(episode);
                break;
            case DOWNLOADED:
                guiUtils.showToast(String.format("Deleted %s", episode.getTitle()),
                        getActivity(), TAG);
                EpisodePersistence.delete(episode);
                break;
            default:
                Log.e(TAG, "Unknown storage state encountered");
        }
    }

    public void onMarkReadUnreadClick(View v) {
        Episode episode = ((Episode) v.getTag());
        DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
                .toggleEpisodeReadState(episode);
        adapter.notifyDataSetChanged();
    }
}
