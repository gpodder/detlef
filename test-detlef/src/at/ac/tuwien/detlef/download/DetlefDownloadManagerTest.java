package at.ac.tuwien.detlef.download;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class DetlefDownloadManagerTest extends AndroidTestCase {

    private static final String url = "http://ondrahosek.dyndns.org/detlef.txt";
    private static final String podcastTitle = "TestPodcast";
    private static final String episodeTitle = "Detlef Episode 3";
    private static final String path = String.format("%s/%s", podcastTitle, new File(url).getName());

    private DetlefDownloadManager mgr;
    private final Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DependencyAssistant.setDependencyAssistant(new MockDependencyAssistant());
        mgr = DependencyAssistant.getDependencyAssistant().getDownloadManager(mContext);

        getFile().delete();
    }

    private File getFile() {
        return new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), path);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEnqueue() throws IOException, InterruptedException {
        Podcast p = new Podcast();
        p.setTitle(podcastTitle);

        Episode e = new Episode(p);
        e.setTitle(episodeTitle);
        e.setUrl(url);

        mgr.enqueue(e);
        semaphore.acquire();

        assertTrue(getFile().exists());
    }

    private class MockDependencyAssistant extends DependencyAssistant {

        DetlefDownloadManager ddm = null;

        @Override
        public DetlefDownloadManager getDownloadManager(Context context) {
            if (ddm == null) {
                ddm = new MockDetlefDownloadManager(context);
            }
            return ddm;
        }
    }

    private class MockDetlefDownloadManager extends DetlefDownloadManager {

        public MockDetlefDownloadManager(Context context) {
            super(context);
        }

        @Override
        public void downloadComplete(long id) {
            super.downloadComplete(id);
            semaphore.release();
        }
    }
}
