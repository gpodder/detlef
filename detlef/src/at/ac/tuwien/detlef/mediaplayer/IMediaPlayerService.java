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

package at.ac.tuwien.detlef.mediaplayer;

import at.ac.tuwien.detlef.domain.Episode;

public interface IMediaPlayerService {

    /**
     * @return The current media player position.
     */
    int getCurrentPosition();

    /**
     * @return Returns the duration of the currently played piece.
     */
    int getDuration();

    /**
     * @return Returns if the player is currently playing or paused/stopped.
     */
    boolean isCurrentlyPlaying();

    IMediaPlayerService pausePlaying();

    /**
     * Sets the media player progress.
     * 
     * @param progress The progress to set the media player to.
     */
    IMediaPlayerService seekTo(int progress);

    /**
     * Starts the next piece from the playlist.
     * 
     * @return this
     */
    IMediaPlayerService fastForward();

    /**
     * Starts the previous piece from the playlist.
     * 
     * @return this
     */
    IMediaPlayerService rewind();

    /**
     * @param position Skips to the specified position in the playlist.
     */
    IMediaPlayerService skipToPosition(int position);

    /**
     * Starts playback of the active episode (if any). Otherwise, gets the first
     * episode from the playlist and starts on that. If there is no active
     * episode and the playlist is empty, does nothing.
     */
    IMediaPlayerService startPlaying();

    /**
     * @return Whether the player service has a currently active episode (paused
     *         or not). This becomes false when a file has been played to its
     *         end.
     */
    boolean hasRunningEpisode();

    /**
     * Sets the episode that should be prepared and played on the next call to
     * startPlaying().
     * 
     * @param ep The episode to be played next
     * @return this
     */
    IMediaPlayerService setNextEpisode(Episode ep);

    /**
     * Gets the episode that should be prepared and played on the next call to
     * startPlaying().
     * 
     * @return thiThe episode to be played next
     */
    Episode getNextEpisode();

    /**
     * Checks if the episode file path exists and points to a valid file.
     * 
     * @param ep The episode whose file path to check
     * @return this
     */
    boolean episodeFileOK(Episode ep);

    /**
     * @return The episode that should be played when the user manually selects
     *         an episode.
     */
    Episode getManualEpisode();

    /**
     * Sets the episode that should be played when the user manually selects an
     * episode.
     * 
     * @param manualEpisode The manual episode
     */
    void setManualEpisode(Episode manualEpisode);

    /**
     * @return If we are in manual mode. In this mode, the manual episode is
     *         played instead of the next episode.
     */
    boolean isManual();

    /**
     * @return Gets the current download progress of an episode, if it is
     *         currently being downloaded.
     */
    int getDownloadProgress();

    /**
     * Stops streaming from the internet if paused.
     */
    void stopStreamingIfPaused();
}
