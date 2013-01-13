/* *************************************************************************
 *  Copyright 2012-2013 The detlef developers                              *
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

package at.ac.tuwien.detlef.gpodder;

import android.app.Fragment;
import at.ac.tuwien.detlef.callbacks.Callback;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 *
 */
public abstract class ConnectionTestCallback<Receiver extends Fragment>
    implements Callback<Receiver> {
    
    private Receiver rcv = null;

    @Override
    public void init() {
        /* nothing */
    }

    @Override
    public void destroy() {
        /* nothing */
    }

    @Override
    public void registerReceiver(Receiver rcv) {
        this.rcv = rcv;
    }

    @Override
    public void unregisterReceiver() {
        this.rcv = null;
    }

    
    /**
     * Called if the provided settings are valid, i.e. the username/password
     * combination is recognized as valid account.
     * @param settings
     */
    public abstract void connectionIsValid(GpodderSettings settings);
    
    /**
     * Called if the provided settings are not valid.
     */
    public abstract void connectionIsNotValid();
    
    /**
     * Called if an error occurs while connecting.
     */
    public abstract void connectionFailed();

    public Receiver getRcv() {
        return rcv;
    }

}
