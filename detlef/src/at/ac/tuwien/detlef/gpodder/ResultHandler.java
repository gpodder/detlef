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
import at.ac.tuwien.detlef.callbacks.Callback;

/**
 * General callback interface.
 * @author ondra
 */
public interface ResultHandler<Receiver> extends Callback<Receiver> {

    /**
     * Called to handle a failed HTTP download.
     * @param errCode The error code; see {@link PodderService.ErrorCode}.
     * @param errStr A string describing the error.
     */
    void handleFailure(int errCode, String errStr);

    void sendEvent(ResultEvent e);

    interface ResultEvent {
        /**
         * Called upon delivery.
         */
        abstract void deliver();
    }

    static class GenericFailureEvent implements ResultEvent {
        private final ResultHandler<?> cb;
        private final int errCode;
        private final String errString;

        public GenericFailureEvent(ResultHandler<?> cb, int errCode,
                                   String errString) {
            this.cb = cb;
            this.errCode = errCode;
            this.errString = errString;
        }

        @Override
        public void deliver() {
            cb.handleFailure(errCode, errString);
        }
    }

    /**
     * This can be used to pass a {@link Bundle} with some extra data to this result handler.
     * This is useful if to pass a state to the receiver so it can perform different
     * actions when the handle is called.
     * @param pBundle
     * @return Fluent Interface.
     */
    ResultHandler<Receiver> setBundle(Bundle pBundle);

    /**
     * @return The {@link Bundle} that was set via {@link #setBundle(Bundle)} or an empty Bundle
     *     if nothing was set.
     */
    Bundle getBundle();
}
