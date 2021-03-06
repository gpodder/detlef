/* *************************************************************************
 *  Copyright 2012-2014 The detlef developers                              *
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.events.PullSubscriptionResultEvent;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.api.UpdateResult;

import de.greenrobot.event.EventBus;

/* TODO: Port to PodderIntentService. */

/**
 * A Runnable to fetch subscription changes. It should be started in its own
 * Thread and sends a reply via the specified callback. The user of the Task
 * needs to implement the Callback's handle & handleFailure methods.
 */
public class SyncSubscriptionsAsyncTask implements Runnable {

    private static final String TAG = SyncSubscriptionsAsyncTask.class.getName();

    private static final int HTTP_STATUS_FORBIDDEN = 401;
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int GENERIC_ERROR = -1;

    private final Bundle bundle;

    public SyncSubscriptionsAsyncTask(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void run() {
        boolean success = false;

        /* Retrieve settings. */
        GpodderSettings gps = Singletons.i().getGpodderSettings();

        DeviceId id = gps.getDeviceId();
        if (id == null) {
            sendError(GENERIC_ERROR, Detlef.getAppContext().getString(
                          R.string.no_gpodder_account_configured));
            return;
        }

        PodcastDAO pdao = Singletons.i().getPodcastDAO();

        String devId = id.toString();

        long lastUpdate = gps.getLastUpdate();

        MygPodderClient gpc = new MygPodderClient(
            gps.getUsername(),
            gps.getPassword(),
            gps.getApiHostname()
        );

        try {
            EnhancedSubscriptionChanges localChanges = new EnhancedSubscriptionChanges(
                pdao.getLocallyAddedPodcasts(), pdao.getLocallyDeletedPodcasts(),
                lastUpdate);

            /* This pushes the local changes. */
            UpdateResult result = gpc.updateSubscriptions(
                                      devId,
                                      localChanges.getAddUrls(),
                                      localChanges.getRemoveUrls());

            /*
             * Login and get subscription changes.
             *
             * BE CAREFUL:
             *
             * In most of the cases, this will also contain the local changes, because we
             * just pushed them to the remote server.
             *
             * But, if you e.g. locally delete a podcast which was already deleted remotely,
             * this local change won't be contained in the remote changes.
             *
             * TODO: make unit test :-)
             */
            SubscriptionChanges changes = gpc.pullSubscriptions(devId,
                                          lastUpdate);

            /* Convert the URLs in changes to 'Detlef Enhanced Subscription Changes' */
            EnhancedSubscriptionChanges remoteChanges = getEnhancedSubscriptionChanges(changes);

            /* Update the db here */

            /* Handle remote changes, which probably include most of the local changes */
            applySubscriptionChanges(Detlef.getAppContext(), remoteChanges);

            /*
             * Handle local changes, for the cases we missed.
             * This will repeat some of the remote changes, but I guess the performance loss
             * is negligible.
             * */
            applySubscriptionChanges(Detlef.getAppContext(), localChanges);


            /* apply the changed URLs */
            if (result.updateUrls != null && result.updateUrls.size() > 0) {
                for (String oldUrl : result.updateUrls.keySet()) {
                    Podcast p = pdao.getPodcastByUrl(oldUrl);
                    if (p == null) {
                        continue;
                    }

                    String newUrl = result.updateUrls.get(oldUrl);
                    if (newUrl == null) {
                        newUrl = "";
                    }

                    p.setUrl(newUrl);
                    pdao.update(p);
                }
            }

            /* Update last changed timestamp. */
            gps.setLastUpdate(remoteChanges.getTimestamp());

            Singletons.i().getGpodderSettingsDAO().writeSettings(gps);

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
        EventBus.getDefault().post(new PullSubscriptionResultEvent(ErrorCode.SUCCESS, bundle));
    }

    /**
     * Called when the task encounters an error. The given error code and string
     * are sent. The Task should exit after this has been called.
     *
     * @param errCode The error code.
     * @param errString The error string.
     */
    private void sendError(int errCode, String errString) {
        EventBus.getDefault().post(new PullSubscriptionResultEvent(ErrorCode.GENERIC_FAILURE, bundle));
    }

    /**
     * This method is only called to save changes which are in sync.
     *
     * Either we save remote changes we pulled from the server.
     * Or we save local changes after we pushed them to the server.
     *
     * @param context
     * @param changes
     */
    private void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes) {
        Log.d(TAG, "Applying changes");

        PodcastDAO dao = Singletons.i().getPodcastDAO();
        for (Podcast p : changes.getAdd()) {
            /* save podcast as remote podcast */
            PodcastSaver.savePodcast(p, true);
        }

        for (Podcast p : changes.getRemove()) {
            Podcast pod = dao.getPodcastByUrl(p.getUrl());
            if (pod != null) {
                dao.deletePodcast(pod);
            }
        }
    }

    /**
     * Converts mygpoclient changes to detlef changes.
     *
     * @param changes subscription changes as delivered by mygpoclient
     * @return subscription changes as used by Detlef
     */
    private EnhancedSubscriptionChanges getEnhancedSubscriptionChanges(SubscriptionChanges changes) {
        return new EnhancedSubscriptionChanges(
                getPodcastStubSet(changes.add),
                getPodcastStubSet(changes.remove),
                changes.timestamp);
    }

    /**
     * Returns a list of podcast stubs.
     *
     * A podcast stub only contains an url. It will get all of its details
     * the first time its feed items are pulled.
     *
     * @param urls Source-urls for the podcast stubs.
     * @return A list of podcast stubs.
     */
    private List<Podcast> getPodcastStubSet(Set<String> urls) {
        List<Podcast> stubs = new ArrayList<Podcast>(urls.size());
        for (String url : urls) {
            Podcast podcast = getStub(url);
            if (podcast != null) {
                stubs.add(podcast);
            }
        }

        return stubs;
    }

    /**
     * Returns a podcast stub, containing only an url and no details.
     *
     * @param url Url to save in the podcast object
     * @return A podcast object containing only an url.
     */
    public static Podcast getStub(String url) {
        Podcast result = new Podcast();
        result.setUrl(url);
        return result;
    }
}
