
package at.ac.tuwien.detlef.db;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * tests the episodeDAOImpl
 *
 * @author Lacky
 */
public class EpisodeDAOImplTest extends AndroidTestCase {

    Episode e1, e2;

    Podcast p1;

    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logo file path");
        p1.setLogoUrl("logo url");
        p1.setTitle("title");
        p1.setUrl("die url halt");

        e1 = new Episode(p1);
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize(0);
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("title");
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
        e2.setTitle("title");
        e2.setUrl("url");
        e2.setStorageState(StorageState.NOT_ON_DEVICE);
        e2.setFilePath("path");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * tests the getAllEpisodes functionality
     */
    public void testGetAllEpisodes() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        assertNotNull("List of episodes shouldn't be null", edao.getAllEpisodes());
    }

    /**
     * tests the insertEpisode functionality
     */
    public void testInsertEpisode() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        int countBeforeInsert = edao.getAllEpisodes().size();
        e1 = edao.insertEpisode(e1);
        int countAfterInsert = edao.getAllEpisodes().size();
        assertEquals(countBeforeInsert + 1, countAfterInsert);
        assertTrue(e1.getId() > 0);
    }

    /**
     * tests the deleteEpisode functionality
     */
    public void testDeleteEpisode() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        int countBeforeDelete = edao.getAllEpisodes().size();
        int ret = edao.deleteEpisode(e1);
        int countAfterDelete = edao.getAllEpisodes().size();
        assertEquals(1, ret);
        assertEquals(countBeforeDelete - 1, countAfterDelete);
    }

    /**
     * tests the getEpisodes functionality
     */
    public void testGetEpisodes() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        ArrayList<Episode> episodes = (ArrayList<Episode>)edao.getEpisodes(p1);
        assertEquals(1, episodes.size());
        Episode ep = episodes.get(0);
        assertEquals(e1.getFilePath(), ep.getFilePath());
        assertEquals(e1.getMimetype(), ep.getMimetype());
        // TODO i'm not sure about his one ...
        assertEquals(ep.getId(), e1.getId());
    }

    /**
     * tests the updateState functionality
     */
    public void testUpdateState() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        StorageState newState = StorageState.DOWNLOADED;
        e1.setStorageState(newState);
        assertEquals(1, edao.updateState(e1));
        ArrayList<Episode> eps = (ArrayList<Episode>)edao.getEpisodes(p1);
        Episode ep = eps.get(0);
        assertEquals(newState, ep.getStorageState());
    }

    /**
     * tests the updateFilePath functionality
     */
    public void testUpdateFilePath() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        String newPath = "a wholy shit new path";
        e1.setFilePath(newPath);
        assertEquals(1, edao.updateFilePath(e1));
        ArrayList<Episode> eps = (ArrayList<Episode>)edao.getEpisodes(p1);
        Episode ep = eps.get(0);
        assertEquals(newPath, ep.getFilePath());
    }
    
    /**
     * tests the updateActionState functionality
     */
    public void testUpdateActionState() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        e1.setActionState(ActionState.DELETE);
        assertEquals(1, edao.updateActionState(e1));
        ArrayList<Episode> eps = (ArrayList<Episode>)edao.getEpisodes(p1);
        Episode ep = eps.get(0);
        assertEquals(ActionState.DELETE, ep.getActionState());
    }
    
    /**
     * tests the updatePlayPosition functionality
     */
    public void testUpdatePlayPosition() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        e1.setPlayPosition(33);
        assertEquals(1, edao.updatePlayPosition(e1));
        ArrayList<Episode> eps = (ArrayList<Episode>)edao.getEpisodes(p1);
        Episode ep = eps.get(0);
        assertEquals(33, ep.getPlayPosition());
    }

    /**
     * tests the deletePodcast functionality which forces a on delete cascade
     * for the episodes
     */
    public void testDeletePodcastCascade() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1 = edao.insertEpisode(e1);
        e2 = edao.insertEpisode(e2);
        assertEquals(1, pdao.deletePodcast(p1));
        ArrayList<Episode> eps = (ArrayList<Episode>)edao.getEpisodes(p1);
        assertEquals(0, eps.size());
    }

    /**
     * tests insert episode functionality with trying to insert
     * null on a non nullable column
     */
    public void testInsertNotNullableColumnShouldFail() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1.setUrl(null);
        e1 = edao.insertEpisode(e1);
        assertNull(e1);
    }

    /**
     * tests insert episode functionality with inserting null
     * on a nullable column
     */
    public void testInsertNullOnNullableColumn() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        e1.setStorageState(null);
        e1 = edao.insertEpisode(e1);
        assertNotNull(e1);
    }

    public void testGetEpisodeByUrlOrGuid() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        String newGuid = java.util.UUID.randomUUID().toString();
        e1.setGuid(newGuid);
        e1 = edao.insertEpisode(e1);
        Episode newEp = edao.getEpisodeByUrlOrGuid("", newGuid);
        assertEquals(e1.getId(), newEp.getId());
        assertEquals(newGuid, newEp.getGuid());
    }

    public void testGetEpisodeByUrlOrGuidWhichNotExists() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        assertNull(edao.getEpisodeByUrlOrGuid("thisurldoesntexist", "nosuchguidavailable"));
    }
}
