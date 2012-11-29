package at.ac.tuwien.detlef.db;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;

import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.api.EpisodeAction;
import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.feeds.Feed;
import com.dragontek.mygpoclient.feeds.Feed.Episode;
import com.dragontek.mygpoclient.feeds.Feed.Episode.Enclosure;
import com.dragontek.mygpoclient.feeds.IFeed.IEpisode;

public class EpisodeDBAssistantImplTest extends AndroidTestCase{

    Podcast p1;


    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("EpisodeDBAssistantImpldescription1");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("EpisodeDBAssistantImpllogoFilePath1");
        p1.setLogoUrl("EpisodeDBAssistantImpllogoUrl1");
        p1.setTitle("EpisodeDBAssistantImpltitle1");
        p1.setUrl("EpisodeDBAssistantImplurl1");
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testApplyActionChanges() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1.setUrl("podcastUrl343");
        p1 = pdao.insertPodcast(p1);
        String url = java.util.UUID.randomUUID().toString();
        
        at.ac.tuwien.detlef.domain.Episode ep = new at.ac.tuwien.detlef.domain.Episode(p1);
        ep.setActionState(ActionState.NEW);
        ep.setAuthor("authro");
        ep.setDescription("description");
        ep.setFilePath("filePath");
        ep.setFileSize(456);
        ep.setGuid("guid");
        ep.setLink("linke");
        ep.setMimetype("mimetype");
        ep.setPlayPosition(0);
        ep.setReleased(343443);
        ep.setStorageState(StorageState.NOT_ON_DEVICE);
        ep.setTitle("titel");
        
        ep.setUrl(url);
        ep = edao.insertEpisode(ep);
        
        EpisodeAction action = new EpisodeAction("podcastUrl343",url, "play",
                "device", "timestamp", null, 34, null);
        
        List<EpisodeAction> aList = new ArrayList<EpisodeAction>();
        aList.add(action);
        
        EpisodeActionChanges changes = new EpisodeActionChanges(aList, (long)44444);
        
        EpisodeDBAssistantImpl epassist = new EpisodeDBAssistantImpl();
        epassist.applyActionChanges(this.mContext, p1, changes);
        
        List<at.ac.tuwien.detlef.domain.Episode> eps = edao.getEpisodes(p1);
        assertEquals(1,eps.size());
        at.ac.tuwien.detlef.domain.Episode freshlyEpisode = eps.get(0);
        assertEquals(ActionState.PLAY,freshlyEpisode.getActionState());
        assertEquals(34,freshlyEpisode.getPlayPosition());
    }

    public void testUpsertAndDeleteEpisodes() {
        EpisodeDAOImpl edao = EpisodeDAOImpl.i(this.mContext);
        PodcastDAOImpl pdao = PodcastDAOImpl.i(this.mContext);
        p1 = pdao.insertPodcast(p1);

        Feed feed = Mockito.mock(Feed.class);
        Mockito.when(feed.getDescription()).thenReturn("feedDescription");
        Mockito.when(feed.getLink()).thenReturn("feedLink");
        Mockito.when(feed.getLogoUrl()).thenReturn("feedLogoUrl");
        Mockito.when(feed.getTitle()).thenReturn("feedTitle");
        Mockito.when(feed.getUrl()).thenReturn("feedUrl");

        Enclosure encl = Mockito.mock(Enclosure.class);
        Mockito.when(encl.getFilesize()).thenReturn(Long.valueOf(1234));
        Mockito.when(encl.getMimetype()).thenReturn("enclMimetype");
        Mockito.when(encl.getUrl()).thenReturn("enclUrl");

        Episode ep = Mockito.mock(Episode.class);
        Mockito.when(ep.getAuthor()).thenReturn("epAuthor");
        Mockito.when(ep.getDescription()).thenReturn("epDescription");
        Mockito.when(ep.getDuration()).thenReturn(Long.valueOf(222));
        Mockito.when(ep.getGuid()).thenReturn("epguid");
        Mockito.when(ep.getLink()).thenReturn("eplink");
        Mockito.when(ep.getReleased()).thenReturn(Long.valueOf(333));
        Mockito.when(ep.getTitle()).thenReturn("eptitle");
        Mockito.when(ep.getEnclosure()).thenReturn(encl);

        IEpisode[] episodeArray = new Episode[1];
        episodeArray[0] = ep;
        Mockito.when(feed.getEpisodes()).thenReturn(episodeArray);

        EpisodeDBAssistantImpl epassist = new EpisodeDBAssistantImpl();
        epassist.upsertAndDeleteEpisodes(this.mContext, p1, feed);

        List<at.ac.tuwien.detlef.domain.Episode> eps = edao.getEpisodes(p1);
        assertEquals(1,eps.size());
        at.ac.tuwien.detlef.domain.Episode freshlyEpisode = eps.get(0);
        assertEquals("epguid",freshlyEpisode.getGuid());
    }
}
