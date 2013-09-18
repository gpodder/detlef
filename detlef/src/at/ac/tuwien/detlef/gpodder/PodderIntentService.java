package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.ToplistResultEvent;

import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

import de.greenrobot.event.EventBus;

public class PodderIntentService extends IntentService {

    private static final String TAG = PodderIntentService.class.getName();

    /** The requested action. One of the REQUEST_* constants in this class. */
    public static final String EXTRA_REQUEST      = "EXTRA_REQUEST";
    public static final String EXTRA_PODCAST_LIST = "EXTRA_RESULT_PODCAST_LIST";

    /** Retrieve the podcast toplist from gpodder.net. */
    public static final int REQUEST_TOPLIST = 0;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    private final EventBus eventBus = EventBus.getDefault();

    public PodderIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final int request = extras.getInt(EXTRA_REQUEST);

        Log.d(TAG, String.format("Received request %d", request));

        switch (request) {
        case REQUEST_TOPLIST:
            getToplist("gpodder.net");
            break;
        default:
            Log.w(TAG, String.format("Unknown request %d received", request));
        }
    }

    private void getToplist(String hostname) {
        Log.d(TAG, "getToplist() on " + Thread.currentThread().getId());

        final PublicClient pc = new PublicClient(hostname);

        try {
            final List<IPodcast> ipodcasts = pc.getToplist();

            /* Convert the list into podcasts. */

            final ArrayList<Podcast> podcasts = new ArrayList<Podcast>(ipodcasts.size());
            for (IPodcast ip : ipodcasts) {
                podcasts.add(new Podcast(ip));
            }

            eventBus.post(new ToplistResultEvent(RESULT_SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "getToplist IOException: " + e.getMessage());
            eventBus.post(new ToplistResultEvent(RESULT_FAILURE, null));
        }
    }


}
