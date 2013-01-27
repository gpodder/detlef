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
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * provides database access for podcasts.
 */
public interface PodcastDBAssistant {

    /**
     * delivers all podcasts which are stored in the database.
     *
     * @param context: the application context eg.
     *            Activity.getApplicationContext());
     * @return returns a list of all podcasts
     */
    List<Podcast> getAllPodcasts(Context context);

    /**
     * Saves the subscriptionchanges into the database.
     *
     * @param context: the application context
     */
    void applySubscriptionChanges(Context context, EnhancedSubscriptionChanges changes);
}
