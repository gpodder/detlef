package at.ac.tuwien.detlef.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = DownloadBroadcastReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        /* TODO: Handle completed downloads. */
        Log.v(TAG, String.format("Received broadcast intent: %s", intent.getAction()));
    }

}
