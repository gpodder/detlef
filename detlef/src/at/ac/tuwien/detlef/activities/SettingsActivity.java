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


package at.ac.tuwien.detlef.activities;

import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
/**
 * This activity handles the preferences. This activity can be accessed via two methods:
 * <ul>
 *     <li></li>
 * </ul>
 * @author moe
 *
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * The name for the extra which controls the behavior of the settings activity.
     * If a {@link Bundle} with this extra exists and it is set to <code>true</code> then
     * the {@link SettingsActivity} will run in a "set up" mode. This mode guides the user
     * through the initial steps that are needed in order to run {@link Detlef}.
     */
    public static final String BOOLEAN_EXTRA_SETUPSCREEN = "show_setup_screen";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }

}
