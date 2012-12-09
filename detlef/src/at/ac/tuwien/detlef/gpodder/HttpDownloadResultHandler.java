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
 * This is the callback interface for {@link GPodderSync#addHttpDownloadJob(String,
 * HttpDownloadResultHandler))}.
 * @author ondra
 */
public interface HttpDownloadResultHandler<Receiver> extends ResultHandler<Receiver> {
    /**
     * Called to handle a successful HTTP download.
     * @param bytes The bytes downloaded from the server.
     */
    void handleSuccess(byte[] bytes);

    /**
     * Called to handle an HTTP download progress update.
     * @param have Number of bytes that have been downloaded.
     * @param total Total size of file being downloaded, or -1 if unknown.
     */
    void handleProgress(int have, int total);

    static class HttpSuccessEvent implements ResultEvent {
        private final HttpDownloadResultHandler<?> cb;
        private final byte[] bytes;

        public HttpSuccessEvent(HttpDownloadResultHandler<?> cb, byte[] bytes) {
            this.cb = cb;
            this.bytes = bytes;
        }

        public void deliver() {
            cb.handleSuccess(bytes);
        }
    }

    static class HttpProgressEvent implements ResultEvent {
        private final HttpDownloadResultHandler<?> cb;
        private final int have;
        private final int total;

        public HttpProgressEvent(HttpDownloadResultHandler<?> cb, int have, int total) {
            this.cb = cb;
            this.have = have;
            this.total = total;
        }

        public void deliver() {
            cb.handleProgress(have, total);
        }
    }
}
