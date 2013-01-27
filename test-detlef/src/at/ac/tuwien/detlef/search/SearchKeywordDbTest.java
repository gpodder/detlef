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


package at.ac.tuwien.detlef.search;

import java.util.List;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

public class SearchKeywordDbTest extends AndroidTestCase {

    private List<Episode> result;

    public static final int TIMEOUT = 2000;

    @Override
    protected void setUp() throws Exception {

        result = null;

        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        EpisodeDAO episodeDao = EpisodeDAOImpl.i();

        // remove all episodes from db
        for (Episode episode : episodeDao.getAllEpisodes()) {
            episodeDao.deleteEpisode(episode);
        }


        Podcast p1;
        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logo file path");
        p1.setLogoUrl("logo url");
        p1.setTitle("title");
        p1.setUrl("die url halt");
        pdao.insertPodcast(p1);


        // text taken from
        // http://en.wikinews.org/w/index.php?title=Main_Page&oldid=1519685

        assertNotNull(episodeDao.insertEpisode(
            new Episode(p1)
                .setAuthor("author")
                .setDescription("Israel warned Hamas yesterday to cease rocket fire and threatened a widening offensive if Hamas continues. Hamas maintains Israel \"is the aggressor.\"")
                .setFileSize(0)
                .setGuid("guid")
                .setLink("link")
                .setMimetype("mimetype")
                .setReleased(System.currentTimeMillis())
                .setTitle("Israel sets 36 hour ultimatum for Hamas")
                .setUrl("url")
                .setStorageState(StorageState.NOT_ON_DEVICE)
                .setFilePath("path")
        ));

        assertNotNull(episodeDao.insertEpisode(
                new Episode(p1)
                    .setAuthor("author")
                    .setDescription("The Philippines announced today it will host a meeting on December 12 in Manila regarding territorial disputes in the South China Sea.")
                    .setFileSize(0)
                    .setGuid("guid")
                    .setLink("link")
                    .setMimetype("mimetype")
                    .setReleased(System.currentTimeMillis())
                    .setTitle("Philippines to host four-country meeting about South China Sea disputes")
                    .setUrl("url")
                    .setStorageState(StorageState.NOT_ON_DEVICE)
                    .setFilePath("path")
            ));

        assertNotNull(episodeDao.insertEpisode(
                new Episode(p1)
                    .setAuthor("author")
                    .setDescription("Three astronauts return to Earth today, after touching down safely in Kazakhstan aboard their Soyuz capsule in the pre-dawn hours of Monday morning.")
                    .setFileSize(0)
                    .setGuid("guid")
                    .setLink("link")
                    .setMimetype("mimetype")
                    .setReleased(System.currentTimeMillis())
                    .setTitle("Expedition 33 crew returns to Earth")
                    .setUrl("url")
                    .setStorageState(StorageState.NOT_ON_DEVICE)
                    .setFilePath("path")
            ));

    }

    /**
     * http://en.wikinews.org/w/index.php?title=Main_Page&oldid=1519685
Israel sets 36 hour ultimatum for Hamas
Philippines to host four-country meeting about South China Sea disputes
Israeli airstrikes damage more offices housing international journalists in Gaza City
Canberra United lose first game since January 2011
Expedition 33 crew returns to Earth
     * @throws Exception
     */

    public void testSearchTitle() throws Exception {

        SearchCallback<Episode> callback = new SearchCallback<Episode>() {

            @Override
            public void getResult(List<Episode> pResult) {
                result = pResult;

            }
        };

        SearchKeywordDb search = new SearchKeywordDb();
        search.search(new SearchCriteriaKeyword().setKeyword("Earth"), callback);

        Thread.sleep(TIMEOUT);

        if (result == null) {
            fail(String.format("Search did not finish within %d ms", TIMEOUT));
        }

        assertEquals("Expedition 33 crew returns to Earth", result.get(0).getTitle());
    }

    public void testSearchDescription() throws Exception {

        SearchCallback<Episode> callback = new SearchCallback<Episode>() {

            @Override
            public void getResult(List<Episode> pResult) {
                result = pResult;

            }
        };

        SearchKeywordDb search = new SearchKeywordDb();
        search.search(new SearchCriteriaKeyword().setKeyword("Kazakhstan"), callback);

        Thread.sleep(TIMEOUT);

        if (result == null) {
            fail(String.format("Search did not finish within %d ms", TIMEOUT));
        }

        assertEquals("Expedition 33 crew returns to Earth", result.get(0).getTitle());
    }

}
