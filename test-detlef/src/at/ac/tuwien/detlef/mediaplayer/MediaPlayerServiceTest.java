
package at.ac.tuwien.detlef.mediaplayer;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;

public class MediaPlayerServiceTest extends ServiceTestCase<MediaPlayerService> {

    /** Fake application for these test cases. */
    private static class FakeApplication extends MockApplication {
    }

    /** Constructs a new MediaPlayerServiceTest. */
    public MediaPlayerServiceTest() {
        super(MediaPlayerService.class);
        Log.d("PodderServiceTest@" + this.hashCode(), "c'tor");

        setApplication(new FakeApplication());
    }

    /**
     * Test whether the service can be started.
     */
    @SmallTest
    public final void testStart() {
        Log.d("MediaPlayerServiceTest@" + this.hashCode(), "testStart()");

        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), MediaPlayerService.class);
        startService(startIntent);
        assertNotNull(getService());
    }

    @MediumTest
    public final void testCheckFilename() {
        startService();
        Episode ep = null;
        assertFalse("Null episode should not have a correct file path",
                getService().episodeFileOK(ep));
        Podcast p = new Podcast();
        ep = new Episode(p);
        assertFalse("Empty episode should not have a correct file path", getService()
                .episodeFileOK(ep));
        ep.setFilePath("/myasdf/qwerty");
        assertFalse("Nonexistent path should not be correct", getService()
                .episodeFileOK(ep));
        ep.setFilePath("/data");
        assertFalse("Directory should not be correct", getService()
                .episodeFileOK(ep));
        ep.setFilePath("/init.rc");
        assertFalse("File should be correct", getService()
                .episodeFileOK(ep));
    }

    /**
     * Launches the service.
     */
    private void startService() {
        Log.d("MediaPlayerServiceTest@" + this.hashCode(), "performBind()");

        if (getService() == null) {
            Intent startIntent = new Intent();
            startIntent.setClass(getContext(), MediaPlayerService.class);
            startService(startIntent);
            assertNotNull(getService());
        }
    }
}
