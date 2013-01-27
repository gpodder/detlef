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
 * Tests the {@link PodcastFilter}.
 * @author moe
 *
 */
public class PodcastFilterTest extends TestCase {

    public void testFilter_positive() {

        Episode episode = new Episode(new Podcast().setId(1));
        PodcastFilter filter = new PodcastFilter().setPodcast(new Podcast().setId(1));

        Assert.assertFalse(
            "Episode should not be filterd, because the"
            + "filter's Podcast id matches the episode's podcast id",
            filter.filter(episode)
        );

    }

    public void testFilter_negative() {

        Episode episode = new Episode(new Podcast().setId(1));
        PodcastFilter filter = new PodcastFilter().setPodcast(new Podcast().setId(2));

        Assert.assertTrue(
            "Episode should be filterd, because the filter's Podcast id "
            + "does not match the episode's podcast id",
            filter.filter(episode)
        );

    }

    public void testFilter_doesNotFilterIfPodcastIdIsNull() {

        Episode episode = new Episode(new Podcast().setId(1));
        PodcastFilter filter = new PodcastFilter().setPodcast(null);

        Assert.assertFalse(
            "Episode should not be filterd, because the filter's Podcast id "
            + "is null",
            filter.filter(episode)
        );

    }

    public void testFilter_doesFilterIfEpisodePodcastIdIsNull() {

        Episode episode = new Episode(new Podcast());
        PodcastFilter filter = new PodcastFilter().setPodcast(new Podcast().setId(3));

        Assert.assertTrue(
            "Episode should be filterd, because the episode's Podcast id "
            + "is null, but the filter's podcast id is not",
            filter.filter(episode)
        );

    }

    public void testFilter_doesNotFilterIfAllPodcastIdsAreNull() {

        Episode episode = new Episode(new Podcast());
        PodcastFilter filter = new PodcastFilter().setPodcast(new Podcast());

        Assert.assertFalse(
            "Episode should not be filterd, because the both the "
            + "episode's podcast id  and the filter's podcast id are null.",
            filter.filter(episode)
        );

    }

    public void testFilter_doesNotFilterIfAllPodcastsAreNull() {

        Episode episode = new Episode(null);
        PodcastFilter filter = new PodcastFilter().setPodcast(null);

        Assert.assertFalse(
            "Episode should not be filterd, because the both the "
            + "episode's podcast and the filter's podcast are null.",
            filter.filter(episode)
        );

    }

    public void testFilter_doesFilterIfEpisodePodcastIsNull() {

        Episode episode = new Episode(null);
        PodcastFilter filter = new PodcastFilter().setPodcast(new Podcast().setId(2));

        Assert.assertTrue(
            "Episode should be filterd, because the the "
            + "episode's podcast id is null, but the filter's podcast is not.",
            filter.filter(episode)
        );

    }


}
