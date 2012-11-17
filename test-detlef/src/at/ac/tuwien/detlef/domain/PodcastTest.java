package at.ac.tuwien.detlef.domain;

import junit.framework.TestCase;

import com.dragontek.mygpoclient.simple.IPodcast;

public class PodcastTest extends TestCase {

    /**
     * Test the constructor which copies an IPodcast.
     */
    public void testCopyCtor() {
        final String realTitle = "PotCast";
        final String fakeTitle = "Pot-Cast";

        IPodcast ip = new IPodcast() {
            private String description = "All things Pot";
            private String logoUrl = "http://potcast.net/pot.png";
            private String title = realTitle;
            @Override
            public String getDescription() { return description; }
            @Override
            public String getLogoUrl() { return logoUrl; }
            @Override
            public String getTitle() { return title; }
            @Override
            public String getUrl() { return "https://potcast.net/potfeed.xml"; }
            @Override
            public void setDescription(String description) {
                this.description = description;
            }
            @Override
            public void setLogoUrl(String logoUrl) {
                this.logoUrl = logoUrl;
            }
            @Override
            public void setTitle(String title) {
                this.title = title;
            }
        };

        Podcast p = new Podcast(ip);

        assertEquals(ip.getDescription(), p.getDescription());
        assertEquals(ip.getLogoUrl(), p.getLogoUrl());
        assertEquals(ip.getTitle(), p.getTitle());
        assertEquals(ip.getUrl(), p.getUrl());

        /* Check whether the Podcast was really copied. */
        assertNotSame(ip, p);
        ip.setTitle(fakeTitle);
        assertEquals(realTitle, p.getTitle());
    }
}
