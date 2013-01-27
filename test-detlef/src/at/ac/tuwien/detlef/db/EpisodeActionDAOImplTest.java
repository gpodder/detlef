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



package at.ac.tuwien.detlef.db;

import java.util.LinkedList;
import java.util.List;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.LocalEpisodeAction;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.domain.RemoteEpisodeAction;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 * Testcases for EpisodeActionDAOImpl.
 *
 */
public class EpisodeActionDAOImplTest extends AndroidTestCase {

    private LocalEpisodeAction lea1;
    private LocalEpisodeAction lea2;
    private Podcast p1;

    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logoFilePath");
        p1.setLogoUrl("logoUrl");
        p1.setTitle("title");
        p1.setUrl("url");
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);

        lea1 = new LocalEpisodeAction(p1, "ep1", Episode.ActionState.DOWNLOAD, null, null, null);
        lea2 = new LocalEpisodeAction(p1, "ep1", Episode.ActionState.PLAY, 0, 7, 42);

        // write deviceId (needed for EpisodeActionsDAO - getAllEpisodeActions)
        GpodderSettings settings = DependencyAssistant.getDependencyAssistant()
                                   .getGpodderSettings();
        DeviceId devId = new DeviceId("mydevice");
        settings.setDeviceId(devId);
        DependencyAssistant.getDependencyAssistant()
        .getGpodderSettingsDAO(mContext).writeSettings(settings);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetAllEpisodeActions() {
        EpisodeActionDAOImpl dao = EpisodeActionDAOImpl.i();
        assertNotNull("List of episode actions shouldn't be null", dao.getAllEpisodeActions());
    }

    /**
     * Tests whether inserting a non-play episode action works.
     */
    public void testInsertEpisodeActionNonPlay() {
        EpisodeActionDAOImpl dao = EpisodeActionDAOImpl.i();
        int countBeforeInsert = dao.getAllEpisodeActions().size();
        assertTrue(dao.insertEpisodeAction(lea1));
        int countAfterInsert = dao.getAllEpisodeActions().size();
        assertEquals(countBeforeInsert + 1, countAfterInsert);
    }

    /**
     * Tests whether inserting a play episode action works.
     */
    public void testInsertEpisodeActionPlay() {
        EpisodeActionDAOImpl dao = EpisodeActionDAOImpl.i();
        int countBeforeInsert = dao.getAllEpisodeActions().size();
        assertTrue(dao.insertEpisodeAction(lea2));
        int countAfterInsert = dao.getAllEpisodeActions().size();
        assertEquals(countBeforeInsert + 1, countAfterInsert);
    }

    /**
     * Tests flushEpisodeActions with an empty list.
     */
    public void testFlushEpisodeActionsEmptyList() {
        EpisodeActionDAOImpl dao = EpisodeActionDAOImpl.i();
        int countBeforeFlush = dao.getAllEpisodeActions().size();
        dao.flushEpisodeActions(new LinkedList<RemoteEpisodeAction>());
        int countAfterFlush = dao.getAllEpisodeActions().size();
        assertEquals(countBeforeFlush, countAfterFlush);
    }

    /**
     * Tests flushEpisodeActions with the list returned by getAllEpisodeActions..
     */
    public void testFlushEpisodeActionsAll() {
        EpisodeActionDAOImpl dao = EpisodeActionDAOImpl.i();
        List<RemoteEpisodeAction> all = dao.getAllEpisodeActions();
        dao.flushEpisodeActions(all);
        assertEquals(0, dao.getAllEpisodeActions().size());
    }

}
