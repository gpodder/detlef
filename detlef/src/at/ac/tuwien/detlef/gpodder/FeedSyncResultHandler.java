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

import android.app.Activity;

/**
 * A class to handle replies from the PullFeedAsyncTask.
 *
 * The Receiver has to be registered with registerReceiver() before use and unregistered with
 * unregisterReceiver() once the Receiver is destroyed.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * FeedSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class FeedSyncResultHandler<Receiver extends Activity> extends
        BroadcastReceiverCallback<Receiver, FeedSyncResultHandler.FeedSyncEvent> {

    /**
     * This has to be implemented by the user and is called when the Task is done.
     */
    public abstract void handle();

    /**
     * This has to be implemented by the user and is called when the Task encountered an error.
     * @param e An Exception describing what went wrong.
     */
    public abstract void handleFailure(GPodderException e);

    @Override
    protected void deliverEvent(final FeedSyncEvent e) {
        getRcv().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                e.deliver();
            }

        });
    }

    public abstract static class FeedSyncEvent
    extends BroadcastReceiverCallback.BroadcastReceiverEvent {
        protected final FeedSyncResultHandler<?> callback;

        FeedSyncEvent(FeedSyncResultHandler<?> callback) {
            this.callback = callback;
        }
    }

    static class FeedSyncEventError extends FeedSyncEvent {
        private final GPodderException exception;

        FeedSyncEventError(FeedSyncResultHandler<?> callback,
                GPodderException exception) {
            super(callback);
            this.exception = exception;
        }

        @Override
        void deliver() {
            callback.handleFailure(exception);
        }

    }

    static class FeedSyncEventSuccess extends FeedSyncEvent {
        FeedSyncEventSuccess(FeedSyncResultHandler<?> callback) {
            super(callback);
        }

        @Override
        void deliver() {
            callback.handle();
        }

    }

}
