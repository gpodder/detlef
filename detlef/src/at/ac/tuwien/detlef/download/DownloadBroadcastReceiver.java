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
