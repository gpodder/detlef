
package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * An IntentService to fetch subscription changes. It is started via an intent
 * and sends a reply via the Intent action
 * "at.ac.tuwien.detlef.custom.intent.action.PULL_SUBSCRIPTIONS". The user of
 * the Task needs a receiver for this action.
 */
public class PullSubscriptionsAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = "PullSubscriptionsAsyncTask";

    private final PodcastSyncResultHandler<? extends Activity> callback;

    public PullSubscriptionsAsyncTask(PodcastSyncResultHandler<? extends Activity> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        /* Retrieve settings. */
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        MygPodderClient gpc = new MygPodderClient(gps.getUsername(), gps.getPassword());

        EnhancedSubscriptionChanges enhanced = null;
        try {
            /* Login and get subscription changes */
            SubscriptionChanges changes = gpc.pullSubscriptions(gps.getDevicename(),
                    gps.getLastUpdate());
            PodcastDetailsRetriever pdr = new PodcastDetailsRetriever();

            /* Get the Details for the individual URLs. */
            enhanced = pdr.getPodcastDetails(changes);

            /* update the db here */
            DependencyAssistant.getDependencyAssistant().getPodcastDBAssistant().
                    applySubscriptionChanges(Detlef.getAppContext(), enhanced);

            /* Update last changed timestamp. */
            gps.setLastUpdate(enhanced.getTimestamp());
        } catch (ClientProtocolException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        } catch (IOException e) {
            sendError(new GPodderException(e.getLocalizedMessage()));
        }

        if (enhanced == null) {
            return;
        }

        /* Send the result. */
        callback.sendEvent(new PodcastSyncResultHandler.PodcastSyncEventSuccess(callback));
    }

    /**
     * Called when the task encounters an error. The given Exception is sent.
     * The Task should exit after this has been called.
     *
     * @param e An Exception describing the error.
     */
    private void sendError(GPodderException e) {
        callback.sendEvent(new PodcastSyncResultHandler.PodcastSyncEventError(callback, e));
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
