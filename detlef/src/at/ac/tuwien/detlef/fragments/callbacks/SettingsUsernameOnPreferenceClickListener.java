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

import android.app.AlertDialog;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.settings.GpodderSettings;
/**
 * This {@link OnPreferenceClickListener} will pop up a warning if a user tries to change
 * a username that already has been verified. Because in this case all user data is
 * wiped and has to be synchronized again.
 *
 * @author moe
 *
 */
public class SettingsUsernameOnPreferenceClickListener
    extends SettingsGpodderNetExternalListener
    implements OnPreferenceClickListener {

    public SettingsUsernameOnPreferenceClickListener(SettingsGpodderNet pSender) {
        super(pSender);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (!getSettings().isAccountVerified()) {
            return true;
        }

        final AlertDialog.Builder b = new AlertDialog.Builder(preference.getContext());

        b.setTitle(R.string.warning);
        b.setMessage(R.string.you_are_changing_verified_username);

        b.setPositiveButton(android.R.string.ok, null);
        b.show();

        return true;
    }

    private GpodderSettings getSettings() {
        return Singletons.i().getGpodderSettings();
    }

}
