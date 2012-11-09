package at.ac.tuwien.detlef.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class EpisodeListFragment extends ListFragment {

    private final ArrayList<Episode> listItems = new ArrayList<Episode>();
    private EpisodeListAdapter adapter;
    private int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EpisodeDAO episodeDAO = new EpisodeDAOImpl(getActivity());
        listItems.addAll(episodeDAO.getAllEpisodes());

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

    /**
     * Called whenever a podcast is clicked in the PodListFragment. Filters the
     * episode list to display only episodes belonging to the specified podcast.
     * If podcast is null, all episodes are shown.
     */
    public void setPodcast(Podcast podcast) {
        /* TODO: Quick and dirty implementation. */
        adapter.clear();

        EpisodeDAO episodeDAO = new EpisodeDAOImpl(getActivity());

        List<Episode> episodes;
        if (podcast == null) {
            episodes = episodeDAO.getAllEpisodes();
        } else {
            episodes = episodeDAO.getEpisodes(podcast);
        }

        adapter.addAll(episodes);
    }
}
