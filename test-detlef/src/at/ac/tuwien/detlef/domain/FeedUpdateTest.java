package at.ac.tuwien.detlef.domain;

import com.dragontek.mygpoclient.feeds.IFeed;
import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

import junit.framework.TestCase;

public class FeedUpdateTest extends TestCase {

    public void testCtor() {
        Podcast p = new Podcast();
        p.setLastUpdate(2000);
        
        final Episode e1 = new Episode(p);
        e1.setTitle("e1");
        e1.setReleased(1000);
        final Episode e2 = new Episode(p);
        e2.setTitle("e2");
        e2.setReleased(2000);
        final Episode e3 = new Episode(p);
        e3.setTitle("e3");
        e3.setReleased(3000);
        final Episode e4 = new Episode(p);
        e4.setTitle("e4");
        e4.setReleased(4000);
        
        IFeed f = new IFeed() {
            @Override
            public String getDescription() {
                return "description";
            }
            @Override
            public IEpisode[] getEpisodes() {
                return new IEpisode[] {e1, e2, e3, e4};
            }
            @Override
            public String getLink() {
                return "link";
            }
            @Override
            public String getTitle() {
                return "title";
            }
            @Override
            public String getUrl() {
                return "url";
            }
        };
        
        FeedUpdate fu = new FeedUpdate(f, p);
        
        assertEquals(f.getDescription(), fu.getDescription());
        assertEquals(f.getLink(), fu.getLink());
        assertEquals(f.getTitle(), fu.getTitle());
        assertEquals(f.getUrl(), fu.getUrl());
        assertEquals(4000, fu.getLastReleaseTime());
        
        IEpisode[] ies = fu.getEpisodes();
        assertEquals(2, ies.length);
        assertEquals(e3.getTitle(), ies[0].getTitle());
        assertEquals(e3.getReleased(), ies[0].getReleased());
        assertEquals(e4.getTitle(), ies[1].getTitle());
        assertEquals(e4.getReleased(), ies[1].getReleased());
    }
}
