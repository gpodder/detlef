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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.EpisodeActionDAO;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.RemoteEpisodeAction;
import at.ac.tuwien.detlef.gpodder.events.EpisodeActionResultEvent;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.EpisodeAction;
import com.dragontek.mygpoclient.api.EpisodeActionChanges;
import com.dragontek.mygpoclient.api.MygPodderClient;

import de.greenrobot.event.EventBus;

/* TODO: Port to PodderIntentService. */

/**
 * A Runnable to sync episode actions. It should be started in its own Thread
 * and sends a reply via the specified callback. The user of the Task needs to
 * implement the Callback's handleSuccess & handleFailure methods.
 */
public class SyncEpisodeActionsAsyncTask implements Runnable {

    private static final String TAG = SyncEpisodeActionsAsyncTask.class.getName();

    private final Bundle bundle;

    public SyncEpisodeActionsAsyncTask(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void run() {
        Context context = Detlef.getAppContext();

        /* Retrieve settings.*/
        GpodderSettings gps = Singletons.i().getGpodderSettings();

        DeviceId devId = gps.getDeviceId();
        if (devId == null) {
            sendError(context.getString(R.string.no_gpodder_account_configured));
            return;
        }
        String username = gps.getUsername();
        String password = gps.getPassword();

        MygPodderClient gpc = new MygPodderClient(username, password, gps.getApiHostname());

        EpisodeActionChanges changes = null;
        try {
            /* Send our episode actions */
            EpisodeActionDAO eaDao = Singletons.i().getEpisodeActionDAO();
            List<RemoteEpisodeAction> localChanges = eaDao.getAllEpisodeActions();

            List<EpisodeAction> sndLocalChanges = new ArrayList<EpisodeAction>(localChanges.size());
            for (EpisodeAction a : localChanges) {
                sndLocalChanges.add(a);
            }

            long since = gpc.uploadEpisodeActions(sndLocalChanges);

            /* Get episode actions. */
            changes = gpc.downloadEpisodeActions(gps.getLastEpisodeActionUpdate());

            applyActionChanges(Detlef.getAppContext(), changes);

            /* Sadly, changes.since is always 0, hence we can't use it to fetch new
             * episode actions. So we use the timestamp returned by the upload. */
            gps.setLastEpisodeActionUpdate(since);

            Singletons.i().getGpodderSettingsDAO().writeSettings(gps);

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
        EventBus.getDefault().post(new EpisodeActionResultEvent(ErrorCode.SUCCESS, bundle));
    }

    /**
     * Called when the task encounters an error. The given String is sent. The
     * Task should exit after this has been called.
     *
     * @param errString A String describing the error.
     */
    private void sendError(String errString) {
        EventBus.getDefault().post(new EpisodeActionResultEvent(ErrorCode.GENERIC_FAILURE, bundle));
    }

    private void applyActionChanges(Context context, EpisodeActionChanges changes) {
        EpisodeDAO dao = Singletons.i().getEpisodeDAO();

        for (EpisodeAction action : changes.actions) {
            // update playposition
            Episode ep = dao.getEpisodeByUrlOrGuid(action.episode, action.episode);
            if (ep != null) {
                ActionState newActionState = ActionState.NEW;
                if (action.action.equals("play")) {
                    newActionState = ActionState.PLAY;
                    Log.i(TAG, "updating play position from: " + action.episode + " pos: "
                          + action.position + " started:" + action.started + " total: "
                          + action.total);
                    /* Episode uses milliseconds. */
                    ep.setPlayPosition(action.position * 1000);
                    if (dao.update(ep) != 1) {
                        Log.w(TAG, "update play position went wrong: " + ep.getLink());
                    }

                } else {
                    if (action.action.equals("download")) {
                        newActionState = ActionState.DOWNLOAD;
                    } else {
                        if (action.action.equals("delete")) {
                            newActionState = ActionState.DELETE;
                        }
                    }
                }
                ep.setActionState(newActionState);
                dao.update(ep);
            }
        }

    }
}
