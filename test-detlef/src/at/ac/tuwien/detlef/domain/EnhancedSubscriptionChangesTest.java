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
        assertEquals(0, pAdd1.getLastUpdate());
        assertEquals("add2", pAdd2.getTitle());
        assertEquals(0, pAdd2.getLastUpdate());
        assertEquals("rem1", pRem1.getTitle());
        assertEquals(666, pRem1.getLastUpdate());
        assertEquals("rem2", pRem2.getTitle());
        assertEquals(666, pRem2.getLastUpdate());
        assertEquals(666, esc.getTimestamp());
    }
}
