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


package at.ac.tuwien.detlef.filter;

import junit.framework.Assert;
import junit.framework.TestCase;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Tests the {@link NewFilter}.
 * @author moe
 *
 */
public class NewFilterTest extends TestCase {

    public void testFilter_filtersNewEpisode() {

        Episode episode = new Episode(new Podcast());
        episode.setActionState(ActionState.NEW);
        NewFilter filter = new NewFilter();

        Assert.assertFalse(
            "Episode should not be filterd, because its "
            + "ActionState is NEW",
            filter.filter(episode)
        );

    }

    public void testFilter_filtersNonNewEpisode() {

        Episode episode = new Episode(new Podcast());
        episode.setActionState(ActionState.PLAY);
        NewFilter filter = new NewFilter();

        Assert.assertTrue(
            "Episode should be filterd, because its "
            + "ActionState is not NEW",
            filter.filter(episode)
        );

    }

}
