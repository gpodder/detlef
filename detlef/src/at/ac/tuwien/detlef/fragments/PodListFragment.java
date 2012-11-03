
package at.ac.tuwien.detlef.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.PodListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodListFragment extends ListFragment {

    private ArrayList<Podcast> listItems = new ArrayList<Podcast>();

    private PodListAdapter adapter;

    int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Podcast all = new Podcast();
        Podcast p1 = new Podcast();
        Podcast p2 = new Podcast();
        Podcast p3 = new Podcast();
        Podcast p4 = new Podcast();
        PodcastDAO dao = new PodcastDAOImpl(this.getActivity().getApplicationContext());
        EpisodeDAO edao = new EpisodeDAOImpl(this.getActivity().getApplicationContext());
        all.setTitle("All Podcasts");
        all.setDescription("description all");
        all.setLastUpdate(System.currentTimeMillis());
        all.setLogoUrl("logoUrl");
        all.setUrl("url");
        all.setId(dao.insertPodcast(all));
        p1.setTitle("My Podcast 1");
        p1.setDescription("description 1");
        p1.setLastUpdate(System.currentTimeMillis());
        p1.setLogoUrl("logoUrl");
        p1.setUrl("url");
        p1.setId(dao.insertPodcast(p1));
        p2.setTitle("My Podcast 2");
        p2.setDescription("description 2");
        p2.setLastUpdate(System.currentTimeMillis());
        p2.setLogoUrl("logoUrl");
        p2.setUrl("url");
        p2.setId(dao.insertPodcast(p2));
        p3.setTitle("My Podcast 3");
        p3.setDescription("description 3");
        p3.setLastUpdate(System.currentTimeMillis());
        p3.setLogoUrl("logoUrl");
        p3.setUrl("url");
        p3.setId(dao.insertPodcast(p3));
        p4.setTitle("My Podcast 4");
        p4.setDescription("description 4");
        p4.setLastUpdate(System.currentTimeMillis());
        p4.setLogoUrl("logoUrl");
        p4.setUrl("url");
        p4.setId(dao.insertPodcast(p4));
        Episode e1 = new Episode();
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize("filesize");
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setPodcast(all);
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("title");
        e1.setUrl("url");
        e1.setId(edao.insertEpisode(e1));
        edao.insertEpisode(e1);
        Episode e2 = new Episode();
        e2.setAuthor("author");
        e2.setDescription("description");
        e2.setFileSize("filesize");
        e2.setGuid("guid");
        e2.setLink("link");
        e2.setMimetype("mimetype");
        e2.setPodcast(p1);
        e2.setReleased(System.currentTimeMillis());
        e2.setTitle("title");
        e2.setUrl("url");
        e2.setId(edao.insertEpisode(e2));

        dao.deletePodcast(((ArrayList<Podcast>)dao.getAllPodcasts()).get(0));
        listItems = (ArrayList<Podcast>)dao.getAllPodcasts();
        // listItems.add(all);
        // listItems.add(p1);
        // listItems.add(p2);
        // listItems.add(p3);
        // listItems.add(p4);
        adapter = new PodListAdapter(getActivity(), R.layout.pod_list_layout, listItems);
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.podcast_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return super.onContextItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.pod_fragment_layout, container, false);
    }
}
