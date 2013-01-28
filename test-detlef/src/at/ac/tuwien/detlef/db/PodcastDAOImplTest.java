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
import at.ac.tuwien.detlef.DependencyAssistant;
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
        PodcastDAO dao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        assertNotNull("List of podcasts shouldn't be null", dao.getAllPodcasts());
    }

    /**
     * tests the insertPodcast functionality
     */
    public void testInsertPodcast() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
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
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
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
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setTitle("expected title");
        p1 = pdao.insertPodcast(p1);
        Podcast pod = pdao.getPodcastById(p1.getId());
        assertEquals("expected title", pod.getTitle());
        assertEquals(p1.getLastUpdate(), pod.getLastUpdate());
        assertEquals(p1.hashCode(), pod.hashCode());
    }

    /**
     * tests the updateUrl functionality
     */
    public void testUpdate() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();

        long currentMilis = System.currentTimeMillis();
        String newFilePath = "new path haha";

        p1 = pdao.insertPodcast(p1);
        p1.setUrl("newUrl");
        p1.setLastUpdate(currentMilis);
        p1.setLogoFilePath(newFilePath);

        assertEquals(1, pdao.update(p1));

        Podcast pod = pdao.getPodcastById(p1.getId());
        assertEquals("newUrl", pod.getUrl());
        assertEquals(newFilePath, pod.getLogoFilePath());
        assertEquals(currentMilis, pod.getLastUpdate());
    }

    /**
     * tests insert podcast functionality with trying to insert
     * null on a non nullable column
     */
    public void testInsertNotNullableColumnShouldFail() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setUrl(null);
        Podcast pod = pdao.insertPodcast(p1);
        assertNull(pod);
    }

    /**
     * tests insert podcast functionality with inserting null
     * on a nullable column
     */
    public void testInsertNullOnNullableColumn() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setLogoFilePath(null);
        Podcast pod = pdao.insertPodcast(p1);
        assertNotNull(pod);
    }

    /**
     * tests the getpodcastbyurl functionality
     */
    public void testGetPodcastByUrl() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        String url = "supergeileurl";
        p1.setUrl(url);
        pdao.insertPodcast(p1);
        Podcast nw = pdao.getPodcastByUrl(url);
        assertEquals(url, nw.getUrl());
    }

    /**
     * Tests adding a podcast locally.
     */
    public void testLocalAdd() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setLocalAdd(true);
        pdao.insertPodcast(p1);
        assertTrue(pdao.getNonDeletedPodcasts().contains(p1));
        assertTrue(pdao.getLocallyAddedPodcasts().contains(p1));
        p1.setLocalAdd(false);
    }

    /**
     * Tests adding a podcast locally and then deleting it locally.
     */
    public void testLocalDeletePodcastLocalAdd() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setLocalAdd(true);
        pdao.insertPodcast(p1);
        assertTrue(pdao.localDeletePodcast(p1));
        assertTrue(!pdao.getNonDeletedPodcasts().contains(p1));
        assertTrue(!pdao.getLocallyDeletedPodcasts().contains(p1));
        assertTrue(!pdao.getLocallyAddedPodcasts().contains(p1));
        p1.setLocalAdd(false);
    }

    /**
     * Tests adding a podcast non-locally, then deleting it locally
     * and then deleting it completely.
     */
    public void testLocalDeletePodcastNonLocalAdd() {
        PodcastDAO pdao = DependencyAssistant.getDependencyAssistant().getPodcastDAO();
        p1.setLocalAdd(false);
        pdao.insertPodcast(p1);
        assertTrue(pdao.localDeletePodcast(p1));
        assertTrue(!pdao.getNonDeletedPodcasts().contains(p1));
        assertTrue(pdao.getLocallyDeletedPodcasts().contains(p1));
        assertTrue(pdao.setRemotePodcast(p1));
        assertTrue(!pdao.getLocallyDeletedPodcasts().contains(p1));
    }

}
