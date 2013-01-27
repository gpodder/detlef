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
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.api.UpdateResult;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * A Runnable to fetch subscription changes. It should be started in its own
 * Thread and sends a reply via the specified callback. The user of the Task
 * needs to implement the Callback's handle & handleFailure methods.
 */
public class SyncSubscriptionsAsyncTask implements Runnable {

    private static final int HTTP_STATUS_FORBIDDEN = 401;
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int GENERIC_ERROR = -1;

    private final NoDataResultHandler<?> callback;

    public SyncSubscriptionsAsyncTask(NoDataResultHandler<?> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        boolean success = false;

        /* Retrieve settings. */
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        DeviceId id = gps.getDeviceId();
        if (id == null) {
            sendError(GENERIC_ERROR, Detlef.getAppContext().getString(
                    R.string.no_gpodder_account_configured));
            return;
        }

        PodcastDAO pDao = PodcastDAOImpl.i();

        String devId = id.toString();

        long lastUpdate = gps.getLastUpdate();

        MygPodderClient gpc = new MygPodderClient(
                gps.getUsername(),
                gps.getPassword(),
                gps.getApiHostname()
                );

        try {
            EnhancedSubscriptionChanges localChanges = new EnhancedSubscriptionChanges(
                    pDao.getLocallyAddedPodcasts(), pDao.getLocallyDeletedPodcasts(),
                    lastUpdate);

            UpdateResult result = gpc.updateSubscriptions(
                    devId,
                    localChanges.getAddUrls(),
                    localChanges.getRemoveUrls());

            /* Login and get subscription changes */
            SubscriptionChanges changes = gpc.pullSubscriptions(devId,
                    lastUpdate);

            /* Get the Details for the individual URLs. */

            PodcastDetailsRetriever pdr = new PodcastDetailsRetriever();
            EnhancedSubscriptionChanges remoteChanges = pdr.getPodcastDetails(changes);

            /* update the db here */
            PodcastDBAssistant dba = DependencyAssistant.getDependencyAssistant()
                    .getPodcastDBAssistant();
            dba.applySubscriptionChanges(Detlef.getAppContext(), localChanges);
            dba.applySubscriptionChanges(Detlef.getAppContext(), remoteChanges);

            /* apply the changed URLs */
            if (result.updateUrls != null && result.updateUrls.size() > 0) {
                for (String oldUrl : result.updateUrls.keySet()) {
                    Podcast p = pDao.getPodcastByUrl(oldUrl);
                    if (p == null) {
                        continue;
                    }

                    String newUrl = result.updateUrls.get(oldUrl);
                    if (newUrl == null) {
                        newUrl = "";
                    }

                    p.setUrl(newUrl);
                    pDao.updateUrl(p);
                }
            }

            /* Update last changed timestamp. */
            gps.setLastUpdate(remoteChanges.getTimestamp());

            DependencyAssistant.getDependencyAssistant()
            .getGpodderSettingsDAO(Detlef.getAppContext())
            .writeSettings(gps);

            success = true;
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
        } catch (AuthenticationException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        } catch (ClientProtocolException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        } catch (IOException e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        } catch (Exception e) {
            sendError(GENERIC_ERROR, e.getLocalizedMessage());
        }

        if (!success) {
            return;
        }

        /* Send the result. */
        callback.sendEvent(new NoDataResultHandler.NoDataSuccessEvent(callback));
    }

    /**
     * Called when the task encounters an error. The given error code and string
     * are sent. The Task should exit after this has been called.
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
         * Convert changes into EnhancedSubscriptionChanges. This accesses the
         * Network is may be sloooooooowwwww.
         *
         * @param changes
         * @return The converted SubscriptionChagnes.
         */
        public EnhancedSubscriptionChanges getPodcastDetails(SubscriptionChanges changes) {
            return new EnhancedSubscriptionChanges(getPodcastSetDetails(changes.add),
                    getPodcastSetDetails(changes.remove), changes.timestamp);
        }

        public IPodcast getPodcastDetails(String url) {
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
