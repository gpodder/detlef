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
