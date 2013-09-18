package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.SearchResultEvent;
import at.ac.tuwien.detlef.gpodder.events.SuggestionsResultEvent;
import at.ac.tuwien.detlef.gpodder.events.ToplistResultEvent;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

import de.greenrobot.event.EventBus;

public class PodderIntentService extends IntentService {

    private static final String TAG = PodderIntentService.class.getName();

    /** The requested action. One of the REQUEST_* constants in this class. */
    public static final String EXTRA_REQUEST     = "EXTRA_REQUEST";
    public static final String EXTRA_CLIENT_INFO = "EXTRA_CLIENT_INFO";
    public static final String EXTRA_QUERY       = "EXTRA_QUERY";

    /** Retrieve the podcast toplist from gpodder.net. */
    public static final int REQUEST_TOPLIST = 0;
    public static final int REQUEST_SUGGESTIONS = 1;
    public static final int REQUEST_SEARCH = 3;

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    private static final int DEFAULT_SUGGESTIONS_COUNT = 15;
    
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
            getToplist(extras);
            break;
        case REQUEST_SUGGESTIONS:
            getSuggestions(extras);
            break;
        case REQUEST_SEARCH:
            searchPodcasts(extras);
            break;
        default:
            Log.w(TAG, String.format("Unknown request %d received", request));
        }
    }

    private void getToplist(Bundle extras) {
        Log.d(TAG, "getToplist() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);

        final PublicClient pc = new PublicClient(cinfo.getHostname());

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

    private void getSuggestions(Bundle extras) {
        Log.d(TAG, "getSuggestions() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);

        MygPodderClient mpc = new MygPodderClient(
            cinfo.getUsername(),
            cinfo.getPassword(),
            cinfo.getHostname());

        try {
            final List <? extends IPodcast > ipodcasts = mpc.getSuggestions(DEFAULT_SUGGESTIONS_COUNT);

            /* Convert the list into podcasts. */

            final List<Podcast> podcasts = new ArrayList<Podcast>(ipodcasts.size());
            for (IPodcast ip : ipodcasts) {
                podcasts.add(new Podcast(ip));
            }

            eventBus.post(new SuggestionsResultEvent(RESULT_SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "getSuggestions IOException: " + e.getMessage());
            eventBus.post(new SuggestionsResultEvent(RESULT_FAILURE, null));
        } catch (AuthenticationException e) {
            eventBus.post(new SuggestionsResultEvent(RESULT_FAILURE, null));
        }
    }

    private void searchPodcasts(Bundle extras) {
        Log.d(TAG, "searchPodcasts() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);
        String query = extras.getString(EXTRA_QUERY);

        final PublicClient pc = new PublicClient(cinfo.getHostname());

        try {
            final List<IPodcast> ipodcasts = pc.searchPodcast(query);

            /* Convert the list into podcasts. */

            final List<Podcast> podcasts = new ArrayList<Podcast>(ipodcasts.size());
            for (IPodcast ip : ipodcasts) {
                podcasts.add(new Podcast(ip));
            }

            eventBus.post(new SearchResultEvent(RESULT_SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "searchPodcasts IOException: " + e.getMessage());
            eventBus.post(new SearchResultEvent(RESULT_FAILURE, null));
        }
    }


}
