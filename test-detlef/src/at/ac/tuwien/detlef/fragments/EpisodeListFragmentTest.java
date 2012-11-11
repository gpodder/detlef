package at.ac.tuwien.detlef.fragments;

import java.util.UUID;

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.State;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class EpisodeListFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private EpisodeDAOImpl dao;
    private String uuid;
    private Episode e;

    public EpisodeListFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        dao = EpisodeDAOImpl.i(getActivity());

        uuid = UUID.randomUUID().toString();
    }

    /**
     * Upon adding a new episode to the DAO, it should be displayed in the episode list.
     */
    public void testAddEpisode() {
        e = new Episode();
        e.setAuthor("author");
        e.setDescription("description");
        e.setFileSize("filesize");
        e.setGuid("guid");
        e.setLink("link");
        e.setMimetype("mimetype");
        e.setPodcast(new Podcast());
        e.setReleased(System.currentTimeMillis());
        e.setTitle(uuid);
        e.setUrl("url");
        e.setState(State.NEW);
        e.setId(dao.insertEpisode(e));
        assertTrue(String.format("New episode %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New episode %s should be in DAO", uuid), dao.getAllEpisodes().contains(e));
    }

    /**
     * After deleting the newly added episode, it should not be displayed in the episode list,
     * and should not be contained in the episode DAO.
     */
    //    public void testRemoveEpisode() {
    //        /* TODO How can I click on a button within a list element? */
    //        assertFalse(String.format("Deleted episode %s should not be displayed in list", uuid), solo.searchText(uuid));
    //        assertFalse(String.format("Deleted episode %s should not be in DAO", uuid), dao.getAllEpisodes().contains(e));
    //    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
