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

package at.ac.tuwien.detlef.domain;


public class LocalEpisodeAction {

    private final Podcast podcast;
    private final String episode;
    private final Episode.ActionState action;
    private final Integer started;
    private final Integer position;
    private final Integer total;

    /**
     * Constructs a new LocalEpisodeAction for insertion into the db.
     *
     * If action is != PLAY, started, position and total may be null, otherwise they must not.
     *
     * @param podcast The Podcast object this action belongs to. Mote that this must already be in
     *                  the databse.
     * @param episode The URL or GUID of the episode this action belongs to.
     * @param action The type of action. This is one of DOWNLOAD, PLAY, DELETE and NEW.
     * @param started The starting position of a playback action (in seconds).
     * @param position The current position of a playback action (in seconds).
     * @param total The total length of the episode file (in seconds).
     */
    public LocalEpisodeAction(Podcast podcast, String episode, Episode.ActionState action,
                              Integer started, Integer position, Integer total) {
        this.podcast = podcast;
        this.episode = episode;
        this.action = action;
        this.started = started;
        this.position = position;
        this.total = total;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public String getEpisode() {
        return episode;
    }

    public Episode.ActionState getAction() {
        return action;
    }

    public String getActionString() {
        return action.toString().toLowerCase();
    }

    public Integer getStarted() {
        return started;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getTotal() {
        return total;
    }
}
