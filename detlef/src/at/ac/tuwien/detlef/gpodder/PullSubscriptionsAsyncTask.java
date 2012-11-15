package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * An IntentService to fetch subscription changes.
 *
 * It is started via an intent and sends a reply via the Intent action
 * "at.ac.tuwien.detlef.custom.intent.action.PULL_SUBSCRIPTIONS". The user of the Task needs a
 * receiver for this action.
 */
public class PullSubscriptionsAsyncTask extends IntentService {
    /** Logging tag. */
    private static final String TAG = "PullSubscriptionsAsyncTask";

    /** The reply-Intent's action. */
    static final String ACTION =
            "at.ac.tuwien.detlef.custom.intent.action.PULL_SUBSCRIPTIONS";

    /** Key for the state extra variable. */
    static final String EXTRA_STATE = "EXTRA_STATE";

    /** Key for the exception extra variable. */
    static final String EXTRA_EXCEPTION = "EXTRA_EXCEPTION";

    /** Key for the changes extra variable. */
    static final String EXTRA_CHANGES = "EXTRA_CHANGES";

    /** The Task was successful. */
    static final int TASK_SUCC = 0;

    /** The Task was failed. */
    static final int TASK_FAIL = 1;

    public PullSubscriptionsAsyncTask() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /* Retrieve settings. */
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant().getGpodderSettings(this);

        MygPodderClient gpc = new MygPodderClient(gps.getUsername(), gps.getPassword());

        EnhancedSubscriptionChanges enhanced = null;
        try {
            /* Login and get subscription changes */
            SubscriptionChanges changes = gpc.pullSubscriptions(gps.getDevicename(),
                    gps.getLastUpdate());
            PodcastDetailsRetriever pdr = new PodcastDetailsRetriever();

            /* Get the Details for the individual URLs. */
            enhanced = pdr.getPodcastDetails(changes);

            // TODO: We should think about updating the db here and set the update time last.
            DependencyAssistant.getDependencyAssistant().getPodcastDBAssistant().
            applySubscriptionChanges(this.getApplicationContext(), enhanced);



            /* Update last changed timestamp. */
            gps.setLastUpdate(changes.timestamp);
        } catch (ClientProtocolException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        } catch (IOException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        }

        if (enhanced == null) {
            return;
        }

        /* Send the result. */
        Intent reply = new Intent(ACTION);
        reply.putExtra(EXTRA_STATE, TASK_SUCC);
        reply.putExtra(EXTRA_CHANGES, enhanced);
        LocalBroadcastManager.getInstance(this).sendBroadcast(reply);
    }

    /**
     * Called when the task encounters an error.
     * The given Exception is sent. The Task should exit after this has been called.
     * @param e An Exception describing the error.
     */
    private void sendError(GPodderException e) {
        Intent reply = new Intent(ACTION);
        reply.putExtra(EXTRA_STATE, TASK_FAIL);
        reply.putExtra(EXTRA_EXCEPTION, e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(reply);
    }

    private static class PodcastDetailsRetriever {
        private final PublicClient pub;

        public PodcastDetailsRetriever() {
            pub = new PublicClient();
        }

        public EnhancedSubscriptionChanges getPodcastDetails(SubscriptionChanges changes) {
            return new EnhancedSubscriptionChanges(getPodcastSetDetails(changes.add),
                    getPodcastSetDetails(changes.remove), changes.timestamp);
        }

        private IPodcast getPodcastDetails(String url) {
            try {
                return pub.getPodcastData(url);
            } catch (ClientProtocolException e) {
                // TODO Somehow pass these errors to UI.
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Somehow pass these errors to UI.
                e.printStackTrace();
            }

            return null;
        }

        private List<IPodcast> getPodcastSetDetails(Set<String> urls) {
            List<IPodcast> podcasts = new ArrayList<IPodcast>(urls.size());
            for (String url : urls) {
                IPodcast podcast = getPodcastDetails(url);
                if (podcast != null) {
                    podcasts.add(getPodcastDetails(url));
                }
            }

            return podcasts;
        }
    }
}
