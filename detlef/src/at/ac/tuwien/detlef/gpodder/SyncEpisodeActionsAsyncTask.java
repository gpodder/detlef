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

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.EpisodeActionDAO;
import at.ac.tuwien.detlef.db.EpisodeActionDAOImpl;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.FeedUpdate;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.domain.RemoteEpisodeAction;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.EpisodeAction;
import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.feeds.FeedServiceClient;
import com.dragontek.mygpoclient.feeds.FeedServiceResponse;

/**
 * A Runnable to sync episode actions. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to
 * implement the Callback's handleSuccess & handleFailure methods.
 */
public class SyncEpisodeActionsAsyncTask implements Runnable {

    private final NoDataResultHandler<?> callback;

    public SyncEpisodeActionsAsyncTask(NoDataResultHandler<?> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        Context context = Detlef.getAppContext();

        /* Retrieve settings.*/
        GpodderSettings gps = DependencyAssistant.getDependencyAssistant()
                .getGpodderSettings(Detlef.getAppContext());

        DeviceId devId = gps.getDeviceId();
        if (devId == null) {
            sendError(context.getString(R.string.no_gpodder_account_configured));
            return;
        }
        String deviceID = gps.getDeviceId().toString();
        String username = gps.getUsername();
        String password = gps.getPassword();

        MygPodderClient gpc = new MygPodderClient(username, password, gps.getApiHostname());

        EpisodeActionChanges changes = null;
        try {
            /* Send our episode actions */
            EpisodeActionDAO eaDao = EpisodeActionDAOImpl.i();
            List<RemoteEpisodeAction> localChanges = eaDao.getAllEpisodeActions();
            
            List<EpisodeAction> sndLocalChanges = new ArrayList<EpisodeAction>(localChanges.size());
            for (EpisodeAction a : localChanges) {
                sndLocalChanges.add(a);
            }

            /* Sadly, this returns always 0, hence we can't use it to fetch new episode actions. */
            gpc.uploadEpisodeActions(sndLocalChanges);

            /* Get episode actions */
            changes = gpc.downloadEpisodeActions(gps.getLastEpisodeActionUpdate(), deviceID);

            DependencyAssistant.getDependencyAssistant().getEpisodeDBAssistant()
                .applyActionChanges(Detlef.getAppContext(), changes);
            
            gps.setLastEpisodeActionUpdate(changes.since);

        } catch (AuthenticationException e) {
            sendError(e.getLocalizedMessage());
        } catch (ClientProtocolException e) {
            sendError(e.getLocalizedMessage());
        } catch (IOException e) {
            sendError(e.getLocalizedMessage());
        }

        if (changes == null) {
            return;
        }

        /* Tell receiver we're done.. */
        callback.sendEvent(new NoDataResultHandler.NoDataSuccessEvent(callback));
    }

    /**
     * Called when the task encounters an error. The given String is sent. The
     * Task should exit after this has been called.
     * 
     * @param errString A String describing the error.
     */
    private void sendError(String errString) {
        callback.sendEvent(new ResultHandler.GenericFailureEvent(callback, 0, errString));
    }
}
