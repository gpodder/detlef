package at.ac.tuwien.detlef.fragments;

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
import at.ac.tuwien.detlef.adapters.PodListAdapter;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl.OnPodcastChangeListener;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.models.PodListModel;

public class PodListFragment extends ListFragment implements OnPodcastChangeListener {

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

        PodcastDAOImpl dao = PodcastDAOImpl.i(this.getActivity());
        dao.addPodcastChangedListener(this);

        List<Podcast> podlist = dao.getAllPodcasts();

        model = new PodListModel<Podcast>(podlist);

        /* And set up the adapter. */

        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout, podlist);
        setListAdapter(adapter);

        /* Then create the 'All Podcasts' header. */

        allPodcasts = createHeader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.pod_fragment_layout, container, false);

        /* Add the 'All Podcasts' header. */

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.addHeaderView(allPodcasts);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onDestroy() {
        PodcastDAOImpl dao = PodcastDAOImpl.i(this.getActivity());
        dao.removePodListChangeListener(this);

        super.onDestroy();
    }

    private View createHeader() {
        View v = getLayoutInflater(getArguments()).inflate(R.layout.pod_list_layout, null);

        TextView tv = (TextView)v.findViewById(R.id.podListPodcastName);
        tv.setText(R.string.all_podcasts);

        tv = (TextView)v.findViewById(R.id.podListLastUpdate);
        tv.setText("22.06.2012");

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
            /* Apparently, the header is counted as a position, so we need to subtract one. */
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
        PodcastDAO dao = PodcastDAOImpl.i(getActivity());
        dao.deletePodcast(podcast);
    }

    @Override
    public void onPodcastChanged(Podcast podcast) {
        Log.v(TAG, String.format("onPodcastChanged: %s", podcast.getTitle()));
        updatePodcastList();
    }

    @Override
    public void onPodcastAdded(Podcast podcast) {
        Log.v(TAG, String.format("onPodcastAdded: %s", podcast.getTitle()));
        model.addPodcast(podcast);
        updatePodcastList();
    }

    @Override
    public void onPodcastDeleted(Podcast podcast) {
        Log.v(TAG, String.format("onPodcastDeleted: %s", podcast.getTitle()));
        model.removePodcast(podcast);
        updatePodcastList();
    }

    /**
     * Updates the displayed list based on the current model contents.
     * Ensures that UI methods are called on the UI thread.
     */
    private void updatePodcastList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
