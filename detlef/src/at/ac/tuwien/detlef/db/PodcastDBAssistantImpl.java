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

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.util.Log;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.EpisodePersistence;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.domain.PodcastImgPersistance;

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
            /* The podcast may already be in the local add/delete table. */
            Podcast pod = dao.getPodcastByUrl(p.getUrl());
            if (pod != null) {
                if (pod.isLocalAdd() || pod.isLocalDel()) {
                    dao.setRemotePodcast(pod);
                    if (pod.getLogoUrl() != null && !pod.getLogoUrl().equals("")) {
                        try {
                            PodcastImgPersistance.download(pod);
                        } catch (IOException e) {
                            Log.e(TAG, "error downloading podcast img: " + e.getMessage());
                        }
                    }
                }

                /* Never insert a podcast twice. */
                continue;
            }

            if ((p.getTitle() != null) && (p.getUrl() != null)) {
                
                if (p.getLogoUrl() != null && !p.getLogoUrl().equals("")) {
                    try {
                        PodcastImgPersistance.download(p);
                    } catch (IOException e) {
                        Log.e(TAG, "error downloading podcast img: " + e.getMessage());
                    }
                }
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
