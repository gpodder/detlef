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

package at.ac.tuwien.detlef.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Receives broadcast intents related to headphone and call states.
 * Pauses playback if headphones are unplugged or an active call exists. *
 */
public class MediaBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = MediaBroadcastReceiver.class.getName();
    private static final int UNPLUGGED = 0;

    private final MediaPlayerService service;

    public MediaBroadcastReceiver(MediaPlayerService mediaPlayerService) {
        this.service = mediaPlayerService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(TAG, String.format("Received intent: %s", action));

        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getExtras().getInt("state");
            if (state == UNPLUGGED) {
                pausePlayback();
            }
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                pausePlayback();
            }
        }
    }

    private void pausePlayback() {
        if (service.isCurrentlyPlaying()) {
            service.pausePlaying();
        }
    }

}
