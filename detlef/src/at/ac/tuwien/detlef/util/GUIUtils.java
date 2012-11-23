
package at.ac.tuwien.detlef.util;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class GUIUtils {

    /**
     * Takes its best effort to display a toast message within the current
     * {@link Activity#getApplicationContext() application context}. This method
     * also works if it is called from within a thread whose belonging activity
     * already has been destroyed (e.g. if the screen has been rotated in the
     * mean time). This is particularly useful for the connection test. Note
     * that this method does not guarantee that the message is eventually shown
     * to the user. If the {@link #activity} is not accessible then no message
     * is shown at all.
     *
     * @param message
     */
    public GUIUtils showToast(final CharSequence message, final Activity activity, String tag) {
        Log.d(tag, String.format("showToast: message = %s, activity = %s", message, activity));

        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getApplicationContext();
                    Toast.makeText(
                            activity.getApplicationContext(),
                            message,
                            Toast.LENGTH_SHORT
                            ).show();
                }
            });
        } catch (Exception e) {
            // don't care - after all it's not that critical that the user
            // receives the result under any circumstances. so better
            // catch and log any exception instead of breaking the complete
            // application at some other point.
            Log.e(tag, e.getMessage(), e);
        }
        return this;
    }

}
