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



package at.ac.tuwien.detlef.gpodder;

import android.os.Bundle;
import at.ac.tuwien.detlef.callbacks.ReliableCallback;

public abstract class ReliableResultHandler<Receiver> extends ReliableCallback<Receiver,
ResultHandler.ResultEvent> implements ResultHandler<Receiver> {
    private Receiver rcv = null;

    /** A bundle to pass extra data. */
    private Bundle bundle = new Bundle();

    /**
     * Returns the currently registered receiver or null.
     * @return
     */
    protected final Receiver getRcv() {
        return rcv;
    }

    @Override
    public final synchronized void registerReceiver(Receiver rcv) {
        if (isReady()) {
            unregisterReceiver();
        }

        this.rcv = rcv;

        super.registerReceiver(rcv);
    }

    @Override
    public final synchronized void unregisterReceiver() {
        if (isReady()) {
            rcv = null;
        }
    }

    @Override
    public void init() {
        /* nothing to do */
    }

    @Override
    public void destroy() {
        /* nothing to do */
    }

    /**
     * Order the callback to deliver the event e.
     *
     * If the receiver is not ready, the event is queued.
     *
     * @param e The event to deliver.
     */
    @Override
    public final synchronized void sendEvent(ResultEvent e) {
        if (!isReady()) {
            queueEvent(e);
            return;
        }

        deliverEvent(e);
    }

    @Override
    protected final boolean isReady() {
        return rcv != null;
    }

    @Override
    protected void deliverEvent(ResultEvent e) {
        e.deliver();
    }

    @Override
    public ReliableResultHandler<Receiver> setBundle(Bundle pBundle) {
        bundle = pBundle;
        return this;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }
}
