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

import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
/**
 * This {@link OnPreferenceClickListener} will open up the browser and call
 * the registration site of gpodder.net so that the user can register a
 * new username.
 *
 * @author moe
 *
 */
public class SettingsRegisterOnPreferenceClickListener
    extends SettingsGpodderNetExternalListener
    implements OnPreferenceClickListener
{

    public SettingsRegisterOnPreferenceClickListener(SettingsGpodderNet pSender) {
        super(pSender);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        Intent browserIntent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://gpodder.net/register")
        );
        getSender().startActivity(browserIntent);

        return true;
    }
}
