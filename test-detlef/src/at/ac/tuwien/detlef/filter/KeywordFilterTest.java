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
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Tests the {@link KeywordFilter}.
 * @author moe
 *
 */
public class KeywordFilterTest extends TestCase {

	public void testFilter_FiltersTitlePositive() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setTitle("Deus ex machina.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("Deus");

		Assert.assertFalse(
			"Epsiode should not be filtered, because its "
			+ "title contains the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_FiltersTitleNegative() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setTitle("Deus ex machina.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("maschine");

		Assert.assertTrue(
			"Epsiode should be filtered, because its title "
			+ "does not contain the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_FiltersDescriptionPositive() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setDescription("And this is the description.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("description");

		Assert.assertFalse(
			"Epsiode should not be filtered, because its "
			+ "description does contain the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_FiltersDescriptionNegative() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setDescription("And this is the description.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("maschine");

		Assert.assertTrue(
			"Epsiode should be filtered, because its description "
			+ "does not contain the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_TitleIsCaseInsensitive() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setTitle("Deux ex machina.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("MaChINa");

		Assert.assertFalse(
			"Epsiode should not be filtered, because its "
			+ "title contains the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_DescriptionIsCaseInsensitive() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setDescription("And this is the description.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("DeScriptIon");

		Assert.assertFalse(
			"Epsiode should not be filtered, because its "
			+ "description contains the keyword",
			filter.filter(episode)
		);
	}

	public void testFilter_TitleAndDescriptionMatch() {

		Episode episode = new Episode(new Podcast().setId(1));

		episode.setTitle("Deux ex machina.");
		episode.setDescription("And this is the machina description.");

		KeywordFilter filter = new KeywordFilter();
		filter.setKeyword("machina");

		Assert.assertFalse(
			"Epsiode should not be filtered, because its description "
			+ "and title contains the keyword",
			filter.filter(episode)
		);
	}

}
