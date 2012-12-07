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

import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

public class EpisodeTest extends TestCase {

    /**
     * Despite not being very useful, this is the first test of our project. So
     * be nice to it :)
     */
    public void testNameSetterAndgetter() {
        String name = "Episode 1";
        Episode episode = new Episode(new Podcast());
        episode.setTitle(name);
        assertEquals(name, episode.getTitle());
    }

    /**
     * Test the constructor which copies an IEpisode.
     */
    public void testCopyCtor() {
        final String realAuthor = "Rodney";

        IEpisode ie = new IEpisode() {
            private String author = realAuthor;
            @Override
            public String getAuthor() {
                /* Change the IEpisode and test again later to make sure we don't just use it as a
                 * backing store. */
                String ret = author;
                author = "Wizard";
                return ret; }
            @Override
            public String getDescription() { return "Needed for invocation ritual."; }
            @Override
            public IEnclosure getEnclosure() {
                return new IEnclosure() {
                    @Override
                    public long getFilesize() { return 666; }
                    @Override
                    public String getMimetype() { return "text/invocation"; }
                    @Override
                    public String getUrl() {
                        return "!= getLink()";
                    }
                };
            }
            @Override
            public String getGuid() { return "DafuqIsAGuid?"; }
            @Override
            public String getLink() { return "http://nethack.wikia.com/wiki/Book_of_the_Dead"; }
            @Override
            public long getReleased() { return -1; }
            @Override
            public String getTitle() { return "Book of the Dead"; }
        };

        Episode e = new Episode(ie, new Podcast());

        assertEquals(realAuthor, e.getAuthor());
        assertEquals(ie.getDescription(), e.getDescription());
        assertEquals(ie.getEnclosure().getFilesize(), e.getFileSize());
        assertEquals(ie.getGuid(), e.getGuid());
        assertEquals(ie.getLink(), e.getLink());
        assertEquals(ie.getEnclosure().getMimetype(), e.getMimetype());
        assertEquals(ie.getReleased(), e.getReleased());
        assertEquals(ie.getTitle(), e.getTitle());
        assertEquals(ie.getEnclosure().getUrl(), e.getUrl());
        assertEquals(Episode.StorageState.NOT_ON_DEVICE, e.getStorageState());

        /* Check whether the Episode was really copied. */
        assertNotSame(ie, e);
    }
    
    public void testGetEnclosure() {
        Episode e = new Episode(new Podcast());
        e.setFileSize(1000);
        e.setMimetype("XXX");
        e.setUrl("blabla)");

        assertEquals(e.getFileSize(), e.getEnclosure().getFilesize());
        assertEquals(e.getMimetype(), e.getEnclosure().getMimetype());
        assertEquals(e.getUrl(), e.getEnclosure().getUrl());
    }

}
