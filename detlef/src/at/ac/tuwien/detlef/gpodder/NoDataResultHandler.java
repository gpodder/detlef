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

/**
 * This is the callback interface for any job which doesn't return data on success. Examples of
 * methods starting such jobs are {@link GPodderSync#addAuthCheckJob(String, String, String,
 * NoDataResultHandler)))}.
 * @author ondra
 */
public interface NoDataResultHandler<Receiver> extends ResultHandler<Receiver> {
    /**
     * Called to handle a successful operation.
     */
    void handleSuccess();

    static class NoDataSuccessEvent implements ResultEvent {
        private final NoDataResultHandler<?> cb;

        public NoDataSuccessEvent(NoDataResultHandler<?> cb) {
            this.cb = cb;
        }

        @Override
        public void deliver() {
            cb.handleSuccess();
        }
    }
}
