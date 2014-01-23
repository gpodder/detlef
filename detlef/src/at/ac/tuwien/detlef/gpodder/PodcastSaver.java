/* *************************************************************************
 *  Copyright 2012-2014 The detlef developers                              *
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

import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * Class to save or update podcast or stub.
 *
 * There might be a better place to define this function. I am not
 * sure yet where.
 */
public final class PodcastSaver {
    private static final String TAG = PodcastSaver.class.getName();

    /**
     * Adds or updates a podcast into the database.
     *
     * @param p podcast to be saved
     * @param asRemote if true, the podcast is saved as remote podcast. If false, local/remote is untouched.
     */
    public static void savePodcast(Podcast p, boolean asRemote) {
        /*
         * If the podcast has no title, it is probably a stub.
         * Temporarily use the url as title.
         */
        if (p.getTitle() == null || p.getTitle().isEmpty()){
            p.setTitle(p.getUrl());
        }

        PodcastDAO dao = Singletons.i().getPodcastDAO();

        Podcast pod = dao.getPodcastByUrl(p.getUrl());
        if (pod != null) {
            /* The podcast may already be in the local add/delete table. */
            if (asRemote && (pod.isLocalAdd() || pod.isLocalDel())) {
                dao.setRemotePodcast(pod);
                /* Now the podcast is assumed to be remote. */
            }

            /* Overwrite the details. */
            dao.update(p);
            return;
        }

        if (p.getUrl() != null) {
            /* If no title was available, we've copied the url */
            assert(p.getTitle() != null);
            dao.insertPodcast(p);
        } else {
            Log.w(TAG, "Cannot insert podcast without url");
        }
    }
}

