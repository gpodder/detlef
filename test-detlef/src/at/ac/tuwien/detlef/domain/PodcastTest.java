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
            public String getDescription() {
                return description;
            }
            @Override
            public String getLogoUrl() {
                return logoUrl;
            }
            @Override
            public String getTitle() {
                return title;
            }
            @Override
            public String getUrl() {
                return "https://potcast.net/potfeed.xml";
            }
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
