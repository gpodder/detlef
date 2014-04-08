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

import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.adapters.PodListAdapter;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.SubscriptionsChangedEvent;
import at.ac.tuwien.detlef.models.PodListModel;
import de.greenrobot.event.EventBus;

public class PodListFragment extends ListFragment {

    private static final String TAG = PodListFragment.class.getName();

    private PodListAdapter adapter;
    private PodListModel<Podcast> model;
    private OnPodcastSelectedListener listener;

    private View allPodcasts;

    /**
     * The parent activity must implement this interface in order to interact
     * with this fragment. The listener is called whenever a podcast is clicked.
     */
    public interface OnPodcastSelectedListener {
        void onPodcastSelected(Podcast podcast);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnPodcastSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s",
                                         activity.toString(),
                                         OnPodcastSelectedListener.class.getName()));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize our podcast model. */

        PodcastDAO dao = Singletons.i().getPodcastDAO();
        EventBus.getDefault().register(this, SubscriptionsChangedEvent.class);

        List<Podcast> podlist = dao.getNonDeletedPodcasts();

        model = new PodListModel<Podcast>(podlist);

        /* And set up the adapter. */

        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout, podlist);
        sortByTitle();
        setListAdapter(adapter);

        /* Then create the 'All Podcasts' header. */

        allPodcasts = createHeader();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.pod_fragment_layout, container, false);

        /* Add the 'All Podcasts' header and empty views. */

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.addHeaderView(allPodcasts);
        listView.setEmptyView(getLayoutInflater(getArguments()).inflate(R.layout.pod_list_empty,
                              null));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private View createHeader() {
        View v = getLayoutInflater(getArguments()).inflate(R.layout.pod_list_header, null);

        TextView tv = (TextView) v.findViewById(R.id.podListPodcastName);
        tv.setText(R.string.all_episodes);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPodcastSelected(null);
            }
        });

        return v;
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.delete_feed:
            /*
             * Apparently, the header is counted as a position, so we need
             * to subtract one.
             */
            onDeleteFeedClicked(info.position - 1);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Podcast podcast = (Podcast) v.getTag();
        listener.onPodcastSelected(podcast);
    }

    private void onDeleteFeedClicked(int pos) {
        Log.v(TAG, String.format("onDeleteFeedClicked %d", pos));
        Podcast podcast = model.get(pos);
        PodcastDAO dao = Singletons.i().getPodcastDAO();
        dao.localDeletePodcast(podcast);
        updatePodcastList();
        //EventBus.getDefault().post(new SubscriptionsChangedEvent());
    }

    public void onEventMainThread(SubscriptionsChangedEvent event) {
        updatePodcastList();
    }

    private void sortByTitle() {
        adapter.sort(new Comparator<Podcast>() {
            @Override
            public int compare(Podcast p1, Podcast p2) {
                return p1.getTitle().compareToIgnoreCase(p2.getTitle());
            }
        });
    }

    /**
     * Updates the displayed list based on the current model contents. Ensures
     * that UI methods are called on the UI thread.
     */
    private void updatePodcastList() {
        Log.v("PodListFragment", "updatePodcastList");

        PodcastDAO dao = Singletons.i().getPodcastDAO();

        List<Podcast> podlist = dao.getNonDeletedPodcasts();

        model.update(podlist);
        adapter.notifyDataSetChanged();
    }
}
