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

import org.mockito.Mockito;

import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Tests the {@link FilterChain}.
 * @author moe
 *
 */
public class FilterChainTest extends TestCase {

	public void testFilter_returnsFalseIfNoFilterInChain() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();
		Assert.assertFalse(
			"An empty filterChain must always return false",
			filterChain.filter(episode)
		);

	}

	public void testFilter_returnsTrueIfTrueFilter() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filter = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filter.filter(episode)).thenReturn(true);

        filterChain.putEpisodeFilter(filter);

        Assert.assertTrue(
        	"Filterchain must return true, because it contains a"
        	+ " filter that returns true",
        	filterChain.filter(episode)
        );

	}

	public void testFilter_returnsFalseIfFalseFilter() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filter = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filter.filter(episode)).thenReturn(false);

        filterChain.putEpisodeFilter(filter);

        Assert.assertFalse(
        	"Filterchain must return true, because it contains a"
        	+ " filter that returns true",
        	filterChain.filter(episode)
        );

	}

	public void testFilter_returnsTrueIfSingleFilterisTrue() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filterFalse = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterFalse.filter(episode)).thenReturn(false);

		EpisodeFilter filterTrue = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrue.filter(episode)).thenReturn(true);


        filterChain.putEpisodeFilter(filterFalse);
        filterChain.putEpisodeFilter(filterTrue);


        Assert.assertTrue(
        	"Filterchain must return true, because it contains a"
        	+ " filter that returns true",
        	filterChain.filter(episode)
        );

	}

	public void testFilter_putFilterReplacesExistingType() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filterFalse = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterFalse.filter(episode)).thenReturn(false);
        Mockito.when(filterFalse.getFilterName()).thenReturn("filterFalse");

		EpisodeFilter filterTrue1 = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrue1.filter(episode)).thenReturn(true);
        Mockito.when(filterTrue1.getFilterName()).thenReturn("filterTrue");

		EpisodeFilter filterTrue2 = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrue2.filter(episode)).thenReturn(true);
        Mockito.when(filterTrue2.getFilterName()).thenReturn("filterTrue");

        filterChain.putEpisodeFilter(filterFalse);
        filterChain.putEpisodeFilter(filterTrue1);
        filterChain.putEpisodeFilter(filterTrue2);


        Assert.assertEquals(
        	"Filterchain must contain 2 elements, because"
        	+ " a filter with the same name was added twice",
        	2,
        	filterChain.countFilters()
        );

	}

	public void testFilter_removeFilter() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filterFalse = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterFalse.filter(episode)).thenReturn(false);
        Mockito.when(filterFalse.getFilterName()).thenReturn("filterFalse");

		EpisodeFilter filterTrue = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrue.filter(episode)).thenReturn(true);
        Mockito.when(filterTrue.getFilterName()).thenReturn("filterTrue");

		EpisodeFilter filterTrueDel = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrueDel.filter(episode)).thenReturn(true);
        Mockito.when(filterTrueDel.getFilterName()).thenReturn("filterTrue");


        filterChain.putEpisodeFilter(filterFalse);
        filterChain.putEpisodeFilter(filterTrue);
        filterChain.removeEpisodeFilter(filterTrueDel);

        Assert.assertEquals(
        	"Filterchain must contain 1 element, because"
        	+ " a filter was removed",
        	1,
        	filterChain.countFilters()
        );

	}

	public void testFilter_removeFilterByName() {

		Episode episode = new Episode(new Podcast());

		FilterChain filterChain = new FilterChain();

		EpisodeFilter filterFalse = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterFalse.filter(episode)).thenReturn(false);
        Mockito.when(filterFalse.getFilterName()).thenReturn("filterFalse");

		EpisodeFilter filterTrue = Mockito.mock(EpisodeFilter.class);
        Mockito.when(filterTrue.filter(episode)).thenReturn(true);
        Mockito.when(filterTrue.getFilterName()).thenReturn("filterTrue");

        filterChain.putEpisodeFilter(filterFalse);
        filterChain.putEpisodeFilter(filterTrue);
        filterChain.removeEpisodeFilter("filterFalse");

        Assert.assertEquals(
        	"Filterchain must contain 1 element, because"
        	+ " a filter was removed",
        	1,
        	filterChain.countFilters()
        );

	}

	public void testFilter_removeInexistantFilterDontCare() {

		FilterChain filterChain = new FilterChain();
        filterChain.removeEpisodeFilter("lolMeNoExists");

        Assert.assertEquals(
        	"Filterchain must contain 0 elements",
        	0,
        	filterChain.countFilters()
        );

	}


}
