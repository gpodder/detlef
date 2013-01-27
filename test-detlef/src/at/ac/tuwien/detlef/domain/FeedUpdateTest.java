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

import com.dragontek.mygpoclient.feeds.IFeed;
import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

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
