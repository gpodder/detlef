package at.ac.tuwien.detlef.domain;

import java.util.LinkedList;

import junit.framework.TestCase;

import com.dragontek.mygpoclient.simple.IPodcast;

public class EnhancedSubscriptionChangesTest extends TestCase {

    /**
     * Test the constructor.
     */
    public void testCtor() {
        Podcast add1 = new Podcast();
        add1.setTitle("add1");
        Podcast add2 = new Podcast();
        add2.setTitle("add2");

        Podcast rem1 = new Podcast();
        rem1.setTitle("rem1");
        Podcast rem2 = new Podcast();
        rem2.setTitle("rem2");

        LinkedList<IPodcast> add = new LinkedList<IPodcast>();
        add.add(add1);
        add.add(add2);
        LinkedList<IPodcast> rem = new LinkedList<IPodcast>();
        rem.add(rem1);
        rem.add(rem2);

        EnhancedSubscriptionChanges esc = new EnhancedSubscriptionChanges(add, rem, 666);

        Podcast pAdd1 = esc.getAdd().get(0);
        Podcast pAdd2 = esc.getAdd().get(1);
        Podcast pRem1 = esc.getRemove().get(0);
        Podcast pRem2 = esc.getRemove().get(1);

        assertEquals("add1", pAdd1.getTitle());
        assertEquals(666, pAdd1.getLastUpdate());
        assertEquals("add2", pAdd2.getTitle());
        assertEquals(666, pAdd2.getLastUpdate());
        assertEquals("rem1", pRem1.getTitle());
        assertEquals(666, pRem1.getLastUpdate());
        assertEquals("rem2", pRem2.getTitle());
        assertEquals(666, pRem2.getLastUpdate());
    }
}
