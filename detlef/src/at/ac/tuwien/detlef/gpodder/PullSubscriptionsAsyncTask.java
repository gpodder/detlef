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



package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;

import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * A Runnable to fetch subscription changes. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to implement
 * the Callback's handle & handleFailure methods.
 */
public class PullSubscriptionsAsyncTask implements Runnable {

    private static final int HTTP_STATUS_FORBIDDEN = 401;
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int GENERIC_ERROR = -1;

    private final NoDataResultHandler<?> callback;

    public PullSubscriptionsAsyncTask(NoDataResultHandler<?> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        /* Retrieve settings. */
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        DeviceId id = gps.getDeviceId();
        if (id == null) {
            sendError(GENERIC_ERROR, Detlef.getAppContext().getString(
                    R.string.no_gpodder_account_configured));
            return;
        }

        String devId = id.toString();

        MygPodderClient gpc = new MygPodderClient(
                gps.getUsername(),
                gps.getPassword(),
                gps.getApiHostname()
                );

        EnhancedSubscriptionChanges enhanced = null;
        try {
            /* Login and get subscription changes */
            SubscriptionChanges changes = gpc.pullSubscriptions(devId,
                    gps.getLastUpdate());
            PodcastDetailsRetriever pdr = new PodcastDetailsRetriever();

            /* Get the Details for the individual URLs. */
            enhanced = pdr.getPodcastDetails(changes);

            /* update the db here */
            DependencyAssistant.getDependencyAssistant().getPodcastDBAssistant().
            applySubscriptionChanges(Detlef.getAppContext(), enhanced);

            /* Update last changed timestamp. */
            gps.setLastUpdate(enhanced.getTimestamp());

            DependencyAssistant.getDependencyAssistant()
            .getGpodderSettingsDAO(Detlef.getAppContext())
            .writeSettings(gps);

        } catch (HttpResponseException e) {
            String eMsg = e.getLocalizedMessage();
            switch (e.getStatusCode()) {
                case HTTP_STATUS_FORBIDDEN:
                    eMsg = Detlef.getAppContext().getString(R.string.connectiontest_unsuccessful);
                    break;
                case HTTP_STATUS_NOT_FOUND:
                    eMsg = String.format(Detlef.getAppContext()
                            .getString(R.string.device_doesnt_exist_fmt), devId);
                    break;
                default:
                    break;
            }
            sendError(e.getStatusCode(), eMsg);
        } catch (AuthenticationException ae) {
            sendError(GENERIC_ERROR, ae.getLocalizedMessage());
        } catch (ClientProtocolException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        } catch (IOException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        }

        if (enhanced == null) {
            return;
        }

        /* Send the result. */
        callback.sendEvent(new NoDataResultHandler.NoDataSuccessEvent(callback));
    }

    /**
     * Called when the task encounters an error. The given error code and string are sent.
     * The Task should exit after this has been called.
     *
     * @param errCode The error code.
     * @param errString The error string.
     */
    private void sendError(int errCode, String errString) {
        callback.sendEvent(new ResultHandler.GenericFailureEvent(callback, errCode, errString));
    }

    /**
     * Wrapper class to turn mygpoclient-java SubscriptionChanges into our
     * EnhancedSubscriptionChanges.
     */
    private static class PodcastDetailsRetriever {
        private final PublicClient pub;

        public PodcastDetailsRetriever() {
            pub = new PublicClient();
        }

        /**
         * Convert changes into EnhancedSubscriptionChanges.
         * 
         * This accesses the Network is may be sloooooooowwwww.
         * @param changes
         * @return The converted SubscriptionChagnes.
         */
        public EnhancedSubscriptionChanges getPodcastDetails(SubscriptionChanges changes) {
            return new EnhancedSubscriptionChanges(getPodcastSetDetails(changes.add),
                    getPodcastSetDetails(changes.remove), changes.timestamp);
        }

        private IPodcast getPodcastDetails(String url) {
            try {
                return pub.getPodcastData(url);
            } catch (ClientProtocolException e) {
                /* do nothing */
            } catch (IOException e) {
                /* do nothing */
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
