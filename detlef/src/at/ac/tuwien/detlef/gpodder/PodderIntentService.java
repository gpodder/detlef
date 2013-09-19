
package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.AuthCheckResultEvent;
import at.ac.tuwien.detlef.gpodder.events.ConnectionErrorEvent;
import at.ac.tuwien.detlef.gpodder.events.PodcastInfoResultEvent;
import at.ac.tuwien.detlef.gpodder.events.RegisterDeviceResultEvent;
import at.ac.tuwien.detlef.gpodder.events.SearchResultEvent;
import at.ac.tuwien.detlef.gpodder.events.SuggestionsResultEvent;
import at.ac.tuwien.detlef.gpodder.events.ToplistResultEvent;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;
import com.dragontek.mygpoclient.simple.SimpleClient;

import de.greenrobot.event.EventBus;

public class PodderIntentService extends IntentService {

    private static final String TAG = PodderIntentService.class.getName();

    /** The requested action. One of the REQUEST_* constants in this class. */
    public static final String EXTRA_REQUEST     = "EXTRA_REQUEST";
    public static final String EXTRA_CLIENT_INFO = "EXTRA_CLIENT_INFO";
    public static final String EXTRA_QUERY       = "EXTRA_QUERY";
    public static final String EXTRA_DEVICE_ID   = "EXTRA_DEVICE_ID";
    public static final String EXTRA_DEVICE_NAME = "EXTRA_DEVICE_NAME";
    public static final String EXTRA_URIS        = "EXTRA_URIS";

    /** Retrieve the podcast toplist from gpodder.net. */
    public static final int REQUEST_TOPLIST     = 0;
    public static final int REQUEST_SUGGESTIONS = 1;
    public static final int REQUEST_SEARCH      = 2;
    public static final int REQUEST_AUTH_CHECK  = 3;
    public static final int REQUEST_REGISTER    = 4;
    public static final int REQUEST_INFO        = 5;

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
            case REQUEST_AUTH_CHECK:
                authCheck(extras);
                break;
            case REQUEST_REGISTER:
                registerDevice(extras);
                break;
            case REQUEST_INFO:
                podcastInfo(extras);
                break;
            default:
                Log.w(TAG, String.format("Unknown request %d received", request));
        }
    }

    public static void startToplistJob(Activity activity, GpoNetClientInfo clientInfo) {
        activity.startService(new Intent(activity, PodderIntentService.class)
                .putExtra(
                        PodderIntentService.EXTRA_REQUEST,
                        PodderIntentService.REQUEST_TOPLIST)
                .putExtra(
                        PodderIntentService.EXTRA_CLIENT_INFO,
                        clientInfo));
    }

    public static void startSuggestionsJob(Activity activity, GpoNetClientInfo clientInfo) {
        activity.startService(new Intent(activity, PodderIntentService.class)
                .putExtra(
                        PodderIntentService.EXTRA_REQUEST,
                        PodderIntentService.REQUEST_SUGGESTIONS)
                .putExtra(
                        PodderIntentService.EXTRA_CLIENT_INFO,
                        clientInfo));
    }

    public static void startSearchJob(Activity activity, GpoNetClientInfo clientInfo, String query) {
        activity.startService(new Intent(activity, PodderIntentService.class)
                .putExtra(
                        PodderIntentService.EXTRA_REQUEST,
                        PodderIntentService.REQUEST_SEARCH)
                .putExtra(
                        PodderIntentService.EXTRA_CLIENT_INFO,
                        clientInfo)
                .putExtra(
                        PodderIntentService.EXTRA_QUERY,
                        query));
    }

    public static void startInfoJob(Activity activity, GpoNetClientInfo clientInfo, ArrayList<String> uris) {
        activity.startService(new Intent(activity, PodderIntentService.class)
        .putExtra(
                PodderIntentService.EXTRA_REQUEST,
                PodderIntentService.REQUEST_INFO)
        .putExtra(
                PodderIntentService.EXTRA_CLIENT_INFO,
                clientInfo)
        .putExtra(
                PodderIntentService.EXTRA_URIS,
                uris));
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

            eventBus.post(new ToplistResultEvent(ErrorCode.SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "getToplist IOException: " + e.getMessage());
            eventBus.post(new ToplistResultEvent(ErrorCode.GENERIC_FAILURE, null));
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
            final List<? extends IPodcast> ipodcasts = mpc
                    .getSuggestions(DEFAULT_SUGGESTIONS_COUNT);

            /* Convert the list into podcasts. */

            final List<Podcast> podcasts = new ArrayList<Podcast>(ipodcasts.size());
            for (IPodcast ip : ipodcasts) {
                podcasts.add(new Podcast(ip));
            }

            eventBus.post(new SuggestionsResultEvent(ErrorCode.SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "getSuggestions IOException: " + e.getMessage());
            eventBus.post(new SuggestionsResultEvent(ErrorCode.IO_PROBLEM, null));
        } catch (AuthenticationException e) {
            eventBus.post(new SuggestionsResultEvent(ErrorCode.AUTHENTICATION_FAILED, null));
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

            eventBus.post(new SearchResultEvent(ErrorCode.SUCCESS, podcasts));
        } catch (IOException e) {
            Log.w(TAG, "searchPodcasts IOException: " + e.getMessage());
            eventBus.post(new SearchResultEvent(ErrorCode.IO_PROBLEM, null));
        }
    }

    private void authCheck(Bundle extras) {
        Log.d(TAG, "authCheck() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);

        // try authenticating
        SimpleClient sc = performGpoLogin(cinfo);

        if (sc != null) {
            eventBus.post(new AuthCheckResultEvent(ErrorCode.SUCCESS));
        }
    }

    private SimpleClient performGpoLogin(GpoNetClientInfo cinfo) {
        if (!isOnline()) {
            Log.w(TAG, "device is offline");
            eventBus.post(new ConnectionErrorEvent(ErrorCode.OFFLINE));
            return null;
        }

        SimpleClient sc = new SimpleClient(cinfo.getUsername(), cinfo.getPassword(),
                cinfo.getHostname());

        boolean ok = sc.authenticate(cinfo.getUsername(), cinfo.getPassword());

        if (!ok) {
            eventBus.post(new ConnectionErrorEvent(ErrorCode.AUTHENTICATION_FAILED));
            return null;
        }

        return sc;
    }

    private static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Detlef.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    private void registerDevice(Bundle extras) {
        Log.d(TAG, "registerDevice() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);
        String deviceId = extras.getString(EXTRA_DEVICE_ID);
        String deviceName = extras.getString(EXTRA_DEVICE_NAME);

        MygPodderClient gpc = new MygPodderClient(
                cinfo.getUsername(),
                cinfo.getPassword(),
                cinfo.getHostname()
                );

        try {
            gpc.updateDeviceSettings(deviceId, deviceName, "mobile");
            eventBus.post(new RegisterDeviceResultEvent(ErrorCode.SUCCESS, deviceId));
        } catch (AuthenticationException e) {
            eventBus.post(new RegisterDeviceResultEvent(ErrorCode.AUTHENTICATION_FAILED, null));
        } catch (IOException e) {
            eventBus.post(new RegisterDeviceResultEvent(ErrorCode.IO_PROBLEM, null));
        }
    }

    private void podcastInfo(Bundle extras) {
        Log.d(TAG, "getPodcastInfo() on " + Thread.currentThread().getId());

        GpoNetClientInfo cinfo = extras.getParcelable(EXTRA_CLIENT_INFO);
        ArrayList<String> uris = extras.getParcelable(EXTRA_URIS);

        PublicClient pc = new PublicClient(cinfo.getHostname());
        String url = uris.get(0);
        try {
            com.dragontek.mygpoclient.simple.Podcast podcast = pc.getPodcastData(url);
            eventBus.post(new PodcastInfoResultEvent(ErrorCode.SUCCESS, new Podcast(podcast)));
        } catch (IOException e) {
            Log.w(TAG, "getPodcastInfo IOException: " + e.getMessage());
            eventBus.post(new PodcastInfoResultEvent(ErrorCode.IO_PROBLEM, null));
        }

    }

}
