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



package at.ac.tuwien.detlef.fragments;

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.jayway.android.robotium.solo.Solo;

public class PlaylistTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private EpisodeDAO edao;
    private PodcastDAO pdao;

    private Podcast p1;
    private Episode e1;
    private Episode e2;

    public PlaylistTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        MainActivity activity = getActivity();

        solo = new Solo(getInstrumentation(), activity);
        edao = Singletons.i().getEpisodeDAO();
        pdao = Singletons.i().getPodcastDAO();

        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logo file path");
        p1.setLogoUrl("logo url");
        p1.setTitle("MYPODCAST101");
        p1.setUrl("die url halt");

        e1 = new Episode(p1);
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize(0);
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("MYEPISODE101");
        e1.setUrl("url");
        e1.setStorageState(StorageState.NOT_ON_DEVICE);
        e1.setFilePath("path");

        e2 = new Episode(p1);
        e2.setAuthor("author");
        e2.setDescription("description");
        e2.setFileSize(0);
        e2.setGuid("guid");
        e2.setLink("link");
        e2.setMimetype("mimetype");
        e2.setReleased(System.currentTimeMillis());
        e2.setTitle("MYEPISODE102");
        e2.setUrl("url");
        e2.setStorageState(StorageState.NOT_ON_DEVICE);
        e2.setFilePath("path");

        pdao.insertPodcast(p1);
        edao.insertEpisode(e1);
        edao.insertEpisode(e2);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
