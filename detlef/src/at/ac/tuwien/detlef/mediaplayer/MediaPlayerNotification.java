
package at.ac.tuwien.detlef.mediaplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;

public class MediaPlayerNotification {

    private MediaPlayerNotification() {
        /* Fully static class. */
    }

    /**
     * Creates and posts the notification layout containing a mini player.
     */
    public static void create(Context context) {

        /*
         * Begin by creating the intents to be sent when clicking notification
         * buttons. Clicking on the logo returns to the activity, and clicking
         * on the media buttons controls the player (without bringing the
         * activity to the foreground).
         */

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent logoIntent = PendingIntent.getActivity(context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent prevIntent = getPendingIntent(context, MediaPlayerService.EXTRA_PREVIOUS);
        PendingIntent playIntent = getPendingIntent(context, MediaPlayerService.EXTRA_PLAY_PAUSE);
        PendingIntent nextIntent = getPendingIntent(context, MediaPlayerService.EXTRA_NEXT);

        /* Construct the remote view to pass as the notification content. */

        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.notification_player);
        v.setOnClickPendingIntent(R.id.logo, logoIntent);
        v.setOnClickPendingIntent(R.id.previous, prevIntent);
        v.setOnClickPendingIntent(R.id.play, playIntent);
        v.setOnClickPendingIntent(R.id.next, nextIntent);

        /*
         * Finally, build and post the notification. Note that we mark it as an
         * ongoing event.
         */

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContent(v);
        builder.setSmallIcon(R.drawable.ic_launcher); /* TODO: Decent icon. */

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.id.notification, notification);
    }

    private static PendingIntent getPendingIntent(Context context, int extra) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.putExtra(MediaPlayerService.EXTRA_MEDIA_CONTROL, extra);
        intent.setAction(Integer.toString(extra));

        /*
         * Without FLAG_UPDATE_CURRENT, extras are not sent. Additionally,
         * setAction() is required to make filterEquals() return false;
         * otherwise the same extra is delivered on each intent.
         */

        return PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
