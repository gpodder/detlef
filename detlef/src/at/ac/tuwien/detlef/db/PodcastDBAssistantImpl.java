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



package at.ac.tuwien.detlef.db;

import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodcastDBAssistantImpl implements PodcastDBAssistant {
    /** Logging tag. */
    private static final String TAG = "PodcastDBAssistant";

    @Override
    public List<Podcast> getAllPodcasts(Context context) {
        PodcastDAO dao = PodcastDAOImpl.i();
        return dao.getAllPodcasts();
    }

    @Override
    public void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes) {
        Log.d(TAG, "applying changes");
        PodcastDAO dao = PodcastDAOImpl.i();
        for (Podcast p : changes.getAdd()) {
            if ((p.getTitle() != null) && (p.getUrl() != null)) {
                dao.insertPodcast(p);
            } else {
                Log.w(TAG, "Cannot insert podcast without title/url");
            }
        }

        for (Podcast p : changes.getRemove()) {
            Podcast pod = dao.getPodcastByUrl(p.getUrl());
            if (pod != null) {
                dao.deletePodcast(pod);
            }
        }
    }

}
