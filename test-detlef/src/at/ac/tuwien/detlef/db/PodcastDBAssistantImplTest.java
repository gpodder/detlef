package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Testcases for PodcastDBAssistantImpl
 * @author Lacky
 *
 */
public class PodcastDBAssistantImplTest extends AndroidTestCase{


    Podcast p1;
    Podcast p2;
    Podcast p3;
    Podcast podToAdd1;
    Podcast podToAdd2;
    Podcast podToAdd3;
    Podcast toDelete1;

    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("description1");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logoFilePath1");
        p1.setLogoUrl("logoUrl1");
        p1.setTitle("title1");
        p1.setUrl("url1");

        p2 = new Podcast();
        p2.setDescription("description2");
        p2.setLastUpdate(222);
        p2.setLogoFilePath("logoFilePath2");
        p2.setLogoUrl("logoUrl2");
        p2.setTitle("title2");
        p2.setUrl("url2");

        toDelete1 = new Podcast();
        toDelete1.setDescription("description2");
        toDelete1.setLastUpdate(222);
        toDelete1.setLogoFilePath("logoFilePath2");
        toDelete1.setLogoUrl("logoUrl2");
        toDelete1.setTitle("title2");
        toDelete1.setUrl("url2");

        p3 = new Podcast();
        p3.setDescription("description3");
        p3.setLastUpdate(333);
        p3.setLogoFilePath("logoFilePath3");
        p3.setLogoUrl("logoUrl3");
        p3.setTitle("title3");
        p3.setUrl("url3");

        podToAdd1 = new Podcast();
        podToAdd1.setDescription("description new1");
        podToAdd1.setLogoUrl("logoUrl new1");
        podToAdd1.setTitle("title new1");
        podToAdd1.setLastUpdate(444);
        podToAdd1.setUrl("url new1");

        podToAdd2 = new Podcast();
        podToAdd2.setDescription("description new2");
        podToAdd2.setLogoUrl("logoUrl new2");
        podToAdd2.setTitle("title new2");
        podToAdd2.setLastUpdate(555);
        podToAdd2.setUrl("url new2");

        podToAdd3 = new Podcast();
        podToAdd3.setDescription("description new3");
        podToAdd3.setLogoUrl("logoUrl new3");
        podToAdd3.setTitle("title new3");
        podToAdd3.setLastUpdate(666);
        podToAdd3.setUrl("url new3");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * tests the applySubscriptionChanges functionality
     * 3 new Podcasts will be added and 1 deleted
     */
    public void testApplySubscriptionChanges() {
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);
        p2 = pdao.insertPodcast(p2); // will be deleted
        p3 = pdao.insertPodcast(p3);

        List<IPodcast> add = new ArrayList<IPodcast>();
        add.add(podToAdd1);
        add.add(podToAdd2);
        add.add(podToAdd3);

        List<IPodcast> remove = new ArrayList<IPodcast>();
        remove.add(toDelete1);

        EnhancedSubscriptionChanges changes = new EnhancedSubscriptionChanges(add,remove,9999);
        PodcastDBAssistantImpl impl = new PodcastDBAssistantImpl();
        impl.applySubscriptionChanges(this.mContext, changes);

        assertNull(pdao.getPodcastById(p2.getId()));
        assertEquals(9999,pdao.getPodcastByUrl("url new1").getLastUpdate());
        assertEquals(9999,pdao.getPodcastByUrl("url new2").getLastUpdate());
        assertEquals(9999,pdao.getPodcastByUrl("url new3").getLastUpdate());
        assertEquals(p1.getId(),pdao.getPodcastById(p1.getId()).getId());
        assertEquals(p3.getId(), pdao.getPodcastById(p3.getId()).getId());
    }
}
