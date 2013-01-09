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


package at.ac.tuwien.detlef;

import android.app.Application;
import android.content.Context;

/**
 * Detlef.
 * 
 * <ol>
 *     <li>A given name for males mainly used in Germany.</li>
 *     <li>The code name for a gpodder.net client running on Android.</li>
 * </ol>
 * 
 * @author INSO_ASE_GROUP1 WS2012
 *
 */
public class Detlef extends Application {

    /**
     * The user agent used by the download manager.
     * TODO: We statically set the version, application name and url.
     */
    public static final String USER_AGENT = "Detlef/1 (https://github.com/schuay/detlef/";

    /** App context. */
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Detlef.context = getApplicationContext();

        /* Append our user agent to the one used by mygpoclient-java. */

        com.dragontek.mygpoclient.Global.USER_AGENT = String.format("%s %s",
                com.dragontek.mygpoclient.Global.USER_AGENT, USER_AGENT);
    }

    /**
     * Provides means to make the application context available via a static method.
     * http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
     * @return The Application's context.
     */
    public static Context getAppContext() {
        return Detlef.context;
    }
}
