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

package at.ac.tuwien.detlef.fragments.callbacks;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.gpodder.events.SubscriptionsChangedEvent;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import de.greenrobot.event.EventBus;
/**
 * Quite a lot is going on if the username preference is changed:
 *
 * <ul>
 *     <li>Generate a default device name out of the username</li>
 *     <li>If the use has changed a previously entered device name that has been successfully
 *     verified, all user data from the previous user has to be wiped.
 *     </li>
 * </ul>
 *
 * @author moe
 *
 */
public class SettingsUsernameOnPreferenceChangeListener
    extends SettingsGpodderNetExternalListener
    implements OnPreferenceChangeListener {

    public SettingsUsernameOnPreferenceChangeListener(SettingsGpodderNet pSender) {
        super(pSender);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary((String) newValue);

        if (!getSettings().getUsername().equals(newValue)) {
            // you have been warned!!
            updateSettings((String) newValue);
            deletePocasts();
        }

        getSender().getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                getSender().setUpTestConnectionButton();
                getSender().setUpRegisterDeviceButton();
            }
        }
        );

        return true;
    }

    private void deletePocasts() {
        Singletons.i()
        .getPodcastDAO()
        .deleteAllPodcasts();
        EventBus.getDefault().post(new SubscriptionsChangedEvent());
    }

    private void updateSettings(String newUsername) {
        Singletons.i().getGpodderSettingsDAO().writeSettings(
            getSettings().setUsername(newUsername).setAccountVerified(false).setDeviceId(null));
    }

    private GpodderSettings getSettings() {
        return Singletons.i().getGpodderSettings();
    }

}
