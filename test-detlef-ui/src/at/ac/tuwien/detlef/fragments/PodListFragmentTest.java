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
import at.ac.tuwien.detlef.DependencyAssistant;
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
        DependencyAssistant.setDependencyAssistant(new MockDependencyAssistant());
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        dao = PodcastDAOImpl.i();
        uuid = java.util.UUID.randomUUID().toString();
    }

    private Podcast insertPodcast() {
        Podcast p = new Podcast();
        uuid = java.util.UUID.randomUUID().toString();
        p.setTitle(uuid);
        p.setLocalAdd(true);

        assertTrue(dao.insertPodcast(p) != null);

        return p;
    }

    /**
     * Upon adding a new podcast to the DAO, it should be displayed in the podcast list.
     */
    public void testAddPodcast() {
        Podcast p = insertPodcast();

        while (solo.scrollDown()) ;

        assertTrue(String.format("New podcast %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New podcast %s should be in DAO", uuid), dao.getPodcastById(p.getId()) != null);
    }

    public void testRotation() {
        Podcast p = insertPodcast();

        solo.setActivityOrientation(Solo.LANDSCAPE);
        while (solo.scrollDown()) ;

        assertTrue(String.format("New podcast %s should be displayed in list", uuid), solo.searchText(uuid));
        assertTrue(String.format("New podcast %s should be in DAO", uuid), dao.getPodcastById(p.getId()) != null);
    }

    /**
     * After deleting the newly added podcast, it should not be displayed in the podcast list,
     * and should not be contained in the podcast DAO.
     */
    public void testRemovePodcast() {
        Podcast p = insertPodcast();

        while (solo.scrollDown()) ;

        solo.clickLongOnText(uuid);
        solo.clickOnText(getActivity().getString(at.ac.tuwien.detlef.R.string.delete_feed));

        assertFalse(String.format("Deleted podcast %s should not be displayed in list", uuid), solo.searchText(uuid));
        assertFalse(String.format("Deleted podcast %s should not be in DAO", uuid), dao.getPodcastById(p.getId()) != null);

    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
