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

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class EpisodeListFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private EpisodeDAOImpl dao;
    private String uuid;

    public EpisodeListFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        dao = EpisodeDAOImpl.i();
        uuid = java.util.UUID.randomUUID().toString();
    }

    /**
     * Upon adding a new episode to the DAO, it should be displayed in the episode list.
     */
    public void testAddEpisode() {
        Podcast p = new Podcast();
        p.setTitle(uuid);
        PodcastDAOImpl.i().insertPodcast(p);
        Episode e = new Episode(p);
        e.setAuthor("author");
        e.setDescription("description");
        e.setFileSize(0);
        e.setGuid("guid");
        e.setLink("link");
        e.setMimetype("mimetype");
        e.setReleased(System.currentTimeMillis());
        e.setTitle(uuid);
        e.setUrl("url");
        e.setStorageState(StorageState.NOT_ON_DEVICE);
        dao.insertEpisode(e);

        solo.clickOnText("EPISODES");
        while (solo.scrollDown()) ;

        assertTrue(String.format("New episode %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New episode %s should be in DAO", uuid), dao.getAllEpisodes().contains(e));
    }

    public void testRotation() {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        while (solo.scrollDown()) ;

        assertTrue(String.format("New episode %s should be displayed in list", uuid), solo.searchText(uuid));
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
