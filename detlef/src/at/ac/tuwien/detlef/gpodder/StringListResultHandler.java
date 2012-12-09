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

/**
 * This is the callback interface for jobs which return a list of strings on success. Examples of
 * methods starting such jobs are {@link
 * GPodderSync#addDownloadPodcastListJob(StringListResultHandler)}.
 * @author ondra
 */
public interface StringListResultHandler<Receiver> extends ResultHandler<Receiver> {
    /**
     * Called to handle a successful fetching of a string list.
     * @param strs List of strings that has been successfully fetched.
     */
    void handleSuccess(List<String> strs);

    static class StringListSuccessEvent implements ResultEvent {
        private final StringListResultHandler<?> cb;
        private final List<String> strs;

        public StringListSuccessEvent(StringListResultHandler<?> cb, List<String> strs) {
            this.cb = cb;
            this.strs = strs;
        }

        public void deliver() {
            cb.handleSuccess(strs);
        }
    }
}
