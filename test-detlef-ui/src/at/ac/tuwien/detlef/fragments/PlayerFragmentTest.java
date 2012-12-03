
package at.ac.tuwien.detlef.fragments;

import android.content.Context;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class PlayerFragmentTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private MainActivity activity;
    private EpisodeDAOImpl dao;

    public PlayerFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
        dao = EpisodeDAOImpl.i();
    }

    private void delay() {
        solo.sleep(2000);
    }

    /*
     * Upon adding a new episode to the DAO, it should be displayed in the
     * episode list. This does not work anymore because I removed the raw test
     * song. But testing audio playback via robotium is silly indeed, anyway.
     * public void testPlayPause() { Podcast p = new Podcast();
     * p.setTitle("My Podcast Asdf");
     * PodcastDAOImpl.i(getActivity()).insertPodcast(p); Episode e = new
     * Episode(p); e.setAuthor("my author qwer");
     * e.setDescription("my description asdf"); e.setFileSize(0);
     * e.setGuid("guid"); e.setLink("link"); e.setMimetype("mimetype");
     * e.setReleased(System.currentTimeMillis()); e.setTitle("MyTitleYxcv");
     * e.setUrl("url"); e.setStorageState(StorageState.NOT_ON_DEVICE);
     * e.setFilePath(Uri.parse( "android.resource://at.ac.tuwien.detlef/" +
     * R.raw.testsong_20_sec) .getPath()); dao.insertEpisode(e);
     * solo.clickOnText("EPISODES"); while (solo.scrollDown()) { ; }
     * solo.clickOnText("MyTitleYxcv"); ImageButton imageButton = (ImageButton)
     * solo.getView(R.id.ButtonPlayStop); solo.clickOnView(imageButton); SeekBar
     * slider = (SeekBar) solo.getView(R.id.SeekBar01); delay();
     * assertTrue("SeekBar should be at a position > 0", slider.getProgress() >
     * 0); solo.clickOnView(imageButton); }
     */

    /**
     * When switching fragments, the slider should not get confused and update
     * itself when the player comes to the foreground again.
     */
    public void testSwitchViews() {
        solo.clickOnText("PLAYER");
        ImageButton imageButton =
                (ImageButton) solo.getView(R.id.ButtonPlayStop);
        solo.clickOnView(imageButton);
        SeekBar slider = (SeekBar) solo.getView(R.id.SeekBar01);

        delay();

        assertTrue("SeekBar should have correct max", slider.getMax() != -1);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // This does not work because it only works in a completely fresh app state
    // - is not really "reproducible"
    // @SuppressWarnings("deprecation")
    // public void testShowEmptyInfo() {
    // solo.clickOnText("EPISODES");
    //
    // Display d =
    // ((WindowManager) activity.getApplication().getSystemService(
    // Context.WINDOW_SERVICE)).getDefaultDisplay();
    // int screenWidth = d.getWidth();
    // int screenHeight = d.getHeight();
    // int fromX = (screenWidth / 2) + (screenWidth / 3);
    // int toX = (screenWidth / 2) - (screenWidth / 3);
    // int fromY = screenHeight / 2;
    // int toY = screenHeight / 2;
    // solo.drag(fromX, toX, fromY, toY, 1);
    // solo.sleep(500);
    // solo.drag(fromX, toX, fromY, toY, 1);
    // solo.sleep(500);
    //
    // // WebView description = (WebView)
    // // solo.getView(R.id.playerEpisodeDescription);
    // // can't load the webview contents, so cannot assert it
    // TextView episodeName = (TextView) solo.getView(R.id.playerEpisode);
    // TextView podcastName = (TextView) solo.getView(R.id.playerPodcast);
    //
    // String noSelected = solo.getString(R.string.no_episode_selected);
    // assertTrue("Episode title should be 'No episode selected'",
    // episodeName.getText().equals(noSelected));
    // assertTrue("Episode name should be empty",
    // episodeName.getText().equals(""));
    // assertTrue("Podcast name should be empty",
    // podcastName.getText().equals(""));
    // }

    /**
     * After clicking on an episode in the episode list, the episode info should
     * be displayed in the player.
     */
    public void testShowEpisodeInfo() {
        Podcast p = new Podcast();
        p.setTitle("My Podcast Asdf");
        PodcastDAOImpl.i().insertPodcast(p);

        Episode e = new Episode(p);
        e.setAuthor("my author qwer");
        e.setDescription("my description asdf");
        e.setFileSize(0);
        e.setGuid("guid");
        e.setLink("link");
        e.setMimetype("mimetype");
        e.setReleased(System.currentTimeMillis());
        e.setTitle("MyTitleYxcv");
        e.setUrl("url");
        e.setStorageState(StorageState.NOT_ON_DEVICE);
        e.setFilePath(Uri.parse(
                "android.resource://com.androidbook.samplevideo/" + R.raw.testsong_20_sec)
                .getPath());
        dao.insertEpisode(e);

        solo.clickOnText("EPISODES");
        while (solo.scrollDown()) {
            ;
        }

        solo.clickOnText("MyTitleYxcv");
        TextView episodeName = (TextView) solo.getView(R.id.playerEpisode);
        TextView podcastName = (TextView) solo.getView(R.id.playerPodcast);

        assertTrue("Episode title should be correct",
                episodeName.getText().equals(e.getTitle()));
        assertTrue("Episode name should be correct", episodeName.getText().equals(e.getTitle()));
        assertTrue("Podcast name should be correct", podcastName.getText().equals(p.getTitle()));
    }
}
