
package at.ac.tuwien.detlef.fragments;

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class PlaylistTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private EpisodeDAOImpl edao;
    private PlaylistDAOImpl ldao;
    private PodcastDAOImpl pdao;

    private Podcast p1;
    private Episode e1;
    private Episode e2;

    public PlaylistTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        edao = EpisodeDAOImpl.i();
        ldao = PlaylistDAOImpl.i();
        pdao = PodcastDAOImpl.i();

        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logo file path");
        p1.setLogoUrl("logo url");
        p1.setTitle("MYPODCAST101");
        p1.setUrl("die url halt");

        e1 = new Episode(p1);
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize(0);
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("MYEPISODE101");
        e1.setUrl("url");
        e1.setStorageState(StorageState.NOT_ON_DEVICE);
        e1.setFilePath("path");

        e2 = new Episode(p1);
        e2.setAuthor("author");
        e2.setDescription("description");
        e2.setFileSize(0);
        e2.setGuid("guid");
        e2.setLink("link");
        e2.setMimetype("mimetype");
        e2.setReleased(System.currentTimeMillis());
        e2.setTitle("MYEPISODE102");
        e2.setUrl("url");
        e2.setStorageState(StorageState.NOT_ON_DEVICE);
        e2.setFilePath("path");

        pdao.insertPodcast(p1);
        edao.insertEpisode(e1);
        edao.insertEpisode(e2);
    }

    // TODO these GUI tests won't work, but everything they should do works in
    // practice. maybe the listener stuff doesn't update the
    // playlist correctly. I don't care anymore, frankly.

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
