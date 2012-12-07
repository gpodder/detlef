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

import at.ac.tuwien.detlef.domain.Podcast;

/**
 * This is the callback interface for jobs which return a list of podcasts on success.
 */
public interface PodcastListResultHandler extends ResultHandler {
    /**
     * Called to handle a successful fetching of a podcast list.
     * @param result List of strings that has been successfully fetched.
     */
    void handleSuccess(List<Podcast> result);
}
