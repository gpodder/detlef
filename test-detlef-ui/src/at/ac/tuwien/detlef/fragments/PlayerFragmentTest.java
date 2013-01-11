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

import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
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
