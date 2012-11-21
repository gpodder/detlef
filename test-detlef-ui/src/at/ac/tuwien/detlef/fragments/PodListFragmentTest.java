package at.ac.tuwien.detlef.fragments;

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

    public PodListFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        dao = PodcastDAOImpl.i(getActivity());
        uuid = java.util.UUID.randomUUID().toString();
    }

    /**
     * Upon adding a new podcast to the DAO, it should be displayed in the podcast list.
     */
    public void testAddPodcast() {
        Podcast p = new Podcast();
        uuid = java.util.UUID.randomUUID().toString();
        p.setTitle(uuid);

        assertTrue(dao.insertPodcast(p) != null);

        while (solo.scrollDown()) ;

        assertTrue(String.format("New podcast %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New podcast %s should be in DAO", uuid), dao.getPodcastById(p.getId()) != null);
    }

    public void testRotation() {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        while (solo.scrollDown()) ;
        Podcast p = new Podcast();
        uuid = java.util.UUID.randomUUID().toString();
        p.setTitle(uuid);
        dao.insertPodcast(p);

        assertTrue(String.format("New podcast %s should be displayed in list", uuid), solo.searchText(uuid));
    }

    /**
     * After deleting the newly added podcast, it should not be displayed in the podcast list,
     * and should not be contained in the podcast DAO.
     */
    public void testRemovePodcast() {
        while (solo.scrollDown()) ;
        Podcast p = new Podcast();
        uuid = java.util.UUID.randomUUID().toString();
        p.setTitle(uuid);
        p = dao.insertPodcast(p);

        solo.clickLongOnText(uuid);
        solo.clickOnText(getActivity().getString(at.ac.tuwien.detlef.R.string.delete_feed));

        assertFalse(String.format("Deleted podcast %s should not be displayed in list", uuid), solo.searchText(uuid));
        // Member variables don't keep their values between tests?
        assertFalse(String.format("Deleted podcast %s should not be in DAO", uuid), dao.getPodcastById(p.getId()) != null);
        
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
