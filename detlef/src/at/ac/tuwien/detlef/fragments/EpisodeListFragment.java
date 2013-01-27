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
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
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
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.EpisodePersistence;
import at.ac.tuwien.detlef.domain.EpisodeSortChoice;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.filter.EpisodeFilter;
import at.ac.tuwien.detlef.filter.FilterChain;
import at.ac.tuwien.detlef.filter.KeywordFilter;
import at.ac.tuwien.detlef.filter.NewFilter;
import at.ac.tuwien.detlef.filter.PodcastFilter;
import at.ac.tuwien.detlef.models.EpisodeListModel;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;

/**
 * The {@link Fragment} that displays a list of {@link Episode Episodes}.
 */
public class EpisodeListFragment extends ListFragment
    implements EpisodeDAO.OnEpisodeChangeListener, PlaylistDAO.OnPlaylistChangeListener {

    private static final String TAG = EpisodeListFragment.class.getName();
    private static final String BUNDLE_SELECTED_PODCAST = "BUNDLE_SELECTED_PODCAST";
    private static final String BUNDLE_FILTERS = "BUNDLE_FILTERS";

    private static final long ID_NONE = -1;

    private EpisodeListModel model;
    private EpisodeListAdapter adapter;
    private FilterChain filter = new FilterChain();
    private Podcast filteredByPodcast = null;
    private OnEpisodeSelectedListener listener;
    private PlaylistDAO playlistDAO;

    private GUIUtils guiUtils;

    private GpodderSettings settings;

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

        Log.d(TAG, "onCreate(" + savedInstanceState + ")");

        EpisodeDAOImpl dao = EpisodeDAOImpl.i();
        dao.addEpisodeChangedListener(this);
        playlistDAO = PlaylistDAOImpl.i();
        playlistDAO.addPlaylistChangedListener(this);

        List<Episode> eplist = dao.getAllEpisodes();
        model = new EpisodeListModel(eplist);

        adapter = new EpisodeListAdapter(getActivity(),
                                         android.R.layout.simple_list_item_1,
                                         new ArrayList<Episode>(eplist));
        setListAdapter(adapter);

        guiUtils = DependencyAssistant.getDependencyAssistant().getGuiUtils();
        restoreSortOrder();
        restoreFilter(savedInstanceState);
    }

    /**
     * Restores the {@link EpisodeFilter episode filters}, e.g. after the screen
     * has been rotated.
     *
     * @param savedInstanceState
     */
    private void restoreFilter(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }

        try {
            FilterChain pFilter = (FilterChain) savedInstanceState.getSerializable(BUNDLE_FILTERS);
            setFilter(pFilter);
            refresh();
        } catch (Exception e) {
            Log.e(TAG, "Exception restoring filter chain", e);
        }

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
        /* restore sort order */
        restoreSortOrder();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /*
         * Save the currently selected podcast's ID in order to be able to
         * restore filter settings later on.
         */
        outState.putSerializable(BUNDLE_FILTERS, getFilter());
        long id = (filteredByPodcast == null ? ID_NONE : filteredByPodcast.getId());
        outState.putLong(BUNDLE_SELECTED_PODCAST, id);
    }

    @Override
    public void onDestroy() {
        EpisodeDAOImpl dao = EpisodeDAOImpl.i();
        dao.removeEpisodeChangedListener(this);
        playlistDAO.removePlaylistChangeListener(this);

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

        /* restore sort order */
        restoreSortOrder();
    }

    private void restoreSortOrder() {
        settings = DependencyAssistant.getDependencyAssistant().getGpodderSettings(getActivity());
        this.sortEpisodeList(settings.getSortChoice(), settings.isAscending());
    }

    private void filterByPodcast() {
        adapter.clear();
        if (filteredByPodcast == null) {
            getFilter().removeEpisodeFilter(new PodcastFilter());
        } else {
            getFilter().putEpisodeFilter(new PodcastFilter().setPodcast(filteredByPodcast));
        }
        refresh();
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

    /**
     * Handles clicks on the mark read/unread button
     *
     * @param v The view of the button
     */
    public void onMarkReadUnreadClick(View v) {
        Episode episode = ((Episode) v.getTag());
        DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
        .toggleEpisodeReadState(episode);
        adapter.notifyDataSetChanged();
    }

    /**
     * Sorts the episode list by a specific choice.
     *
     * @param choice The sort choice
     * @param ascending Whether to sort ascending/descending
     */
    public void sortEpisodeList(EpisodeSortChoice choice, final boolean ascending) {
        switch (choice) {
        case Podcast:
            adapter.sort(new Comparator<Episode>() {
                @Override
                public int compare(Episode e1, Episode e2) {
                    if (ascending) {
                        return Long.valueOf(e1.getPodcast().getId()).compareTo(
                                   Long.valueOf(e2.getPodcast().getId()));
                    }
                    return Long.valueOf(e2.getPodcast().getId()).compareTo(
                               Long.valueOf(e1.getPodcast().getId()));
                }
            });
            break;
        case ReleaseDate:
            adapter.sort(new Comparator<Episode>() {
                @Override
                public int compare(Episode e1, Episode e2) {
                    if (ascending) {
                        return Long.valueOf(e1.getReleased()).compareTo(
                                   Long.valueOf(e2.getReleased()));
                    }
                    return Long.valueOf(e2.getReleased()).compareTo(
                               Long.valueOf(e1.getReleased()));
                }
            });
            break;
        default:
        }
        updateEpisodeList();
    }

    /**
     * Sets a keyword for the episode search.
     *
     * @param newText The keyword to search for.
     */
    public void setKeyword(String newText) {
        filter.putEpisodeFilter(new KeywordFilter().setKeyword(newText));
        refresh();
    }

    /**
     * Refreshes the episode list view.
     */
    public void refresh() {
        adapter.clear();

        for (Episode e : model.getAll()) {
            if (filter.filter(e)) {
                continue;
            }
            adapter.add(e);
        }

    }

    /**
     * @return The FilterChain currently associated with this
     *         EpisodeListFragment. This method must not return null.
     */
    public FilterChain getFilter() {
        return filter;
    }

    /**
     * Sets a filter for filtering the episodes.
     *
     * @param pFilter The filter to set.
     * @return this
     */
    public EpisodeListFragment setFilter(FilterChain pFilter) {

        if (pFilter == null) {
            throw new IllegalArgumentException("pFilter must not be null");
        }

        filter = pFilter;
        return this;
    }

    public void setReadFilter(boolean checked) {
        if (checked) {
            getFilter().putEpisodeFilter(new NewFilter());
        } else {
            getFilter().removeEpisodeFilter(new NewFilter());
        }
        refresh();
    }

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition) {
        // not of interest here
    }

    @Override
    public void onPlaylistEpisodeRemoved(int position) {
        adapter.notifyDataSetChanged();
    }
}
