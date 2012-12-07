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


package at.ac.tuwien.detlef.callbacks;

import java.util.LinkedHashMap;

/**
 * A container for callbacks.
 * 
 * This allows us to perform common operations (like registering the receiver) on multiple
 * callbacks.
 *
 * @param <Receiver> The type of the receiver.
 */
public class CallbackContainer<Receiver> {
    /** A map mapping a String to a Callback. */
    private final LinkedHashMap<String, Callback<Receiver> > callbacks =
            new LinkedHashMap<String, Callback<Receiver> >();

    /**
     * Adds the given callback to the map under the given key and calls the callback's init()
     * method.
     * 
     * If there was another callback registered under the same key, its destroy() method is called.
     * 
     * @param key The key.
     * @param cb The callback.
     */
    public synchronized void put(String key, Callback<Receiver> cb) {
        Callback<Receiver> oldCb = callbacks.put(key, cb);
        if (oldCb != null) {
            oldCb.destroy();
        }
        cb.init();
    }

    /**
     * Removes the callback stored under the given key and calls its destroy() method.
     * 
     * @param key The key identifying the callback to remove.
     */
    public synchronized void remove(String key) {
        Callback<? extends Receiver> cb = callbacks.remove(key);
        if (cb != null) {
            cb.destroy();
        }
    }

    /**
     * Returns the callback stored under the given key.
     * 
     * @param key The key identifying the callback to get.
     * @return The callback or null if there was none under the given key.
     */
    public synchronized Callback<Receiver> get(String key) {
        return callbacks.get(key);
    }

    /**
     * Calls registerReceiver() for all stored callbacks.
     * 
     * @param rcv The Receiver to register.
     */
    public synchronized void registerReceiver(Receiver rcv) {
        for (Callback<Receiver> cb : callbacks.values()) {
            if (cb != null) {
                cb.registerReceiver(rcv);
            }
        }
    }

    /**
     * Calls unregisterReceiver() for all stored callbacks.
     */
    public synchronized void unregisterReceiver() {
        for (Callback<Receiver> cb : callbacks.values()) {
            if (cb != null) {
                cb.unregisterReceiver();
            }
        }
    }

    /**
     * Removes all stored callbacks and calls destroy() on each of them.
     */
    public synchronized void clear() {
        for (Callback<? extends Receiver> cb : callbacks.values()) {
            if (cb != null) {
                cb.destroy();
            };
        }
        callbacks.clear();
    }
}
