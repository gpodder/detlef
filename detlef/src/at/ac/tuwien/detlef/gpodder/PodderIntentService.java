package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Podcast;

import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

public class PodderIntentService extends IntentService {

    private static final String TAG = PodderIntentService.class.getName();

    /**
     * The requested action. One of the REQUEST_* constants in this class.
     */
    public static final String EXTRA_REQUEST = "EXTRA_REQUEST";

    /**
     * The result receiver responsible for moving results from the service
     * into the main application space.
     */
    public static final String EXTRA_RESULT_RECEIVER = "EXTRA_RESULT_RECEIVER";

    public static final String EXTRA_RESULT_PODCAST_LIST = "EXTRA_RESULT_PODCAST_LIST";

    /**
     * Retrieve the podcast toplist from gpodder.net.
     */
    public static final int REQUEST_TOPLIST = 0;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    public PodderIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        int request = extras.getInt(EXTRA_REQUEST);
        ResultReceiver resultReceiver = extras.getParcelable(EXTRA_RESULT_RECEIVER);

        Log.d(TAG, String.format("Received request %d", request));

        switch (request) {
        case REQUEST_TOPLIST:
            getToplist(resultReceiver, "gpodder.net");
            break;
        default:
            Log.w(TAG, String.format("Unknown request %d received", request));
            resultReceiver.send(RESULT_FAILURE, null);
        }
    }

    private void getToplist(ResultReceiver resultReceiver, String hostname) {
        Log.d(TAG, "getToplist() on " + Thread.currentThread().getId());

        PublicClient pc = new PublicClient(hostname);

        try {
            List<IPodcast> ipodcasts = pc.getToplist();

            /* Convert the list into podcasts. */

            ArrayList<Podcast> podcasts = new ArrayList<Podcast>(ipodcasts.size());
            for (IPodcast ip : ipodcasts) {
                podcasts.add(new Podcast(ip));
            }

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(EXTRA_RESULT_PODCAST_LIST, podcasts);

            resultReceiver.send(RESULT_SUCCESS, bundle);
        } catch (IOException e) {
            Log.w(TAG, "getToplist IOException: " + e.getMessage());
            resultReceiver.send(RESULT_FAILURE, null);
        }
    }


}
