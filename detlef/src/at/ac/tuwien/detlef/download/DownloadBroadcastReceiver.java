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


package at.ac.tuwien.detlef.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = DownloadBroadcastReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            downloadComplete(context, intent);
        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            notificationClicked(context, intent);
        }
    }

    private void notificationClicked(Context context, Intent intent) {
        long id = getId(intent);

        Log.v(TAG, String.format("Received broadcast intent: action = %s, id = %d",
                                 intent.getAction(), id));

        /* TODO: Implementation. */

    }

    private void downloadComplete(Context context, Intent intent) {
        long id = getId(intent);

        Log.v(TAG, String.format("Received broadcast intent: action = %s, id = %d",
                                 intent.getAction(), id));

        DetlefDownloadManager ddm = DependencyAssistant
                                    .getDependencyAssistant()
                                    .getDownloadManager(context);
        ddm.downloadComplete(id);
    }

    private long getId(Intent intent) {
        return intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
    }

}
