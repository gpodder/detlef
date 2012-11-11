package at.ac.tuwien.detlef.fragments;

import java.util.UUID;

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class PodListFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private PodcastDAO dao;
    private String uuid;
    private long podcastId;

    public PodListFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        dao = PodcastDAOImpl.i(getActivity());

        uuid = UUID.randomUUID().toString();
    }

    /**
     * Upon adding a new podcast to the DAO, it should be displayed in the podcast list.
     */
    public void testAddPodcast() {
        Podcast p = new Podcast();
        p.setTitle(uuid);
        podcastId = dao.insertPodcast(p);
        p.setId(podcastId);
        assertTrue(String.format("New podcast %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New podcast %s should be in DAO", uuid), dao.getPodcastById(podcastId) != null);
    }

    /**
     * After deleting the newly added podcast, it should not be displayed in the podcast list,
     * and should not be contained in the podcast DAO.
     */
    public void testRemovePodcast() {
        solo.clickLongOnText(uuid);
        solo.clickOnMenuItem(getActivity().getString(at.ac.tuwien.detlef.R.string.delete_feed));
        assertFalse(String.format("Deleted podcast %s should not be displayed in list", uuid), solo.searchText(uuid));
        assertFalse(String.format("Deleted podcast %s should not be in DAO", uuid), dao.getPodcastById(podcastId) != null);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
