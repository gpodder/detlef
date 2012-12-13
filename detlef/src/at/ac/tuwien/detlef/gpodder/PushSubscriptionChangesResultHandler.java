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

import java.util.List;
import java.util.Map;

import at.ac.tuwien.detlef.domain.Podcast;

/**
 * This is the callback interface for jobs which return a list of podcasts on success.
 */
public interface PushSubscriptionChangesResultHandler<Receiver> extends ResultHandler<Receiver> {
    /**
     * Called to handle a successful update of subscription changes.
     * @param timestamp A new timestamp to use for the next updating API-call.
     * @param updateUrls Sanitized URLs for the added podcasts.
     */
    void handleSuccess(long timestamp, Map<String, String> updateUrls);

    static class PushSubscriptionChangesSuccessEvent implements ResultEvent {
        private final PushSubscriptionChangesResultHandler<?> cb;
        private final long timestamp;
        private final Map<String, String> updateUrls;

        public PushSubscriptionChangesSuccessEvent(PushSubscriptionChangesResultHandler<?> cb,
                long timestamp, Map<String, String> updateUrls) {
            this.cb = cb;
            this.timestamp = timestamp;
            this.updateUrls = updateUrls;
        }

        public void deliver() {
            cb.handleSuccess(timestamp, updateUrls);
        }
    }
}