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



package at.ac.tuwien.detlef.db;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Testcases for PodcastDAOImpl
 *
 * @author Lacky
 */
public class PodcastDAOImplTest extends AndroidTestCase {

    Podcast p1;

    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logoFilePath");
        p1.setLogoUrl("logoUrl");
        p1.setTitle("title");
        p1.setUrl("url");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * test the getAllPodcast functionality
     */
    public void testGetAllPodcasts() {
        PodcastDAOImpl dao = PodcastDAOImpl.i();
        assertNotNull("List of podcasts shouldn't be null", dao.getAllPodcasts());
    }

    /**
     * tests the insertPodcast functionality
     */
    public void testInsertPodcast() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        int countBeforeInsert = pdao.getAllPodcasts().size();
        p1 = pdao.insertPodcast(p1);
        int countAfterInsert = pdao.getAllPodcasts().size();
        assertEquals(countBeforeInsert + 1, countAfterInsert);
        assertTrue(p1.getId() > 0);
    }

    /**
     * tests the deletePodcast functionality
     */
    public void testDeletePodcast() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        int countBeforeDelete = pdao.getAllPodcasts().size();
        int ret = pdao.deletePodcast(p1);
        int countAfterDelete = pdao.getAllPodcasts().size();
        assertEquals(1, ret);
        assertNotSame(countBeforeDelete, countAfterDelete);
    }

    /**
     * tests the getPodcastById functionality
     */
    public void testGetPodcastById() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1.setTitle("expected title");
        p1 = pdao.insertPodcast(p1);
        Podcast pod = pdao.getPodcastById(p1.getId());
        assertEquals("expected title", pod.getTitle());
        assertEquals(p1.getLastUpdate(), pod.getLastUpdate());
        assertEquals(p1.hashCode(), pod.hashCode());
    }

    /**
     * tests the updateLastUpdate functionality
     */
    public void testUpdateLastUpdate() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        long currentMilis = System.currentTimeMillis();
        p1.setLastUpdate(currentMilis);
        assertEquals(1, pdao.updateLastUpdate(p1));
        Podcast pod = pdao.getPodcastById(p1.getId());
        assertEquals(currentMilis, pod.getLastUpdate());
    }

    /**
     * tests the updateLogoFilePath functionality
     */
    public void testUpdateLogoFilePath() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        String newFilePath = "new path haha";
        p1.setLogoFilePath(newFilePath);
        assertEquals(1, pdao.updateLogoFilePath(p1));
        Podcast pod = pdao.getPodcastById(p1.getId());
        assertEquals(newFilePath, pod.getLogoFilePath());
    }

    /**
     * tests insert podcast functionality with trying to insert
     * null on a non nullable column
     */
    public void testInsertNotNullableColumnShouldFail() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1.setUrl(null);
        Podcast pod = pdao.insertPodcast(p1);
        assertNull(pod);
    }

    /**
     * tests insert podcast functionality with inserting null
     * on a nullable column
     */
    public void testInsertNullOnNullableColumn() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1.setLogoFilePath(null);
        Podcast pod = pdao.insertPodcast(p1);
        assertNotNull(pod);
    }

    /**
     * tests the getpodcastbyurl functionality
     */
    public void testGetPodcastByUrl() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        String url = "supergeileurl";
        p1.setUrl(url);
        pdao.insertPodcast(p1);
        Podcast nw = pdao.getPodcastByUrl(url);
        assertEquals(url, nw.getUrl());
    }

}
