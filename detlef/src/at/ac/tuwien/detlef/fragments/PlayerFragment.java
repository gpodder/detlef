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

package at.ac.tuwien.detlef.fragments;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;

public class PlayerFragment extends Fragment implements PlaylistDAO.OnPlaylistChangeListener,
    EpisodeDAO.OnEpisodeChangeListener {

    private static final int PROGRESS_BAR_UPDATE_INTERVAL = 500;

    private final Handler playProgressUpdateHandler = new Handler();
    private IMediaPlayerService service;
    private Episode activeEpisode = null;

    private boolean fragmentPaused = true;
    private boolean progressUpdaterRunning = false;
    private boolean bound = false;
    private boolean trackingTouch = false;

    private ImageButton buttonPlayStop;
    private SeekBar seekBar;
    private TextView alreadyPlayed;
    private TextView remainingTime;
    private ImageButton buttonFF;
    private ImageButton buttonRew;
    private ImageView podcastIcon;

    /** Logging Tag */
    private static final String TAG = PlayerFragment.class.getCanonicalName();

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            bound = true;
            MediaPlayerService.MediaPlayerBinder binder =
                (MediaPlayerService.MediaPlayerBinder) iBinder;
            service = binder.getService();
            activeEpisode = service.getNextEpisode();
            setEpisodeInfoControls(activeEpisode);
            if (!progressUpdaterRunning) {
                progressUpdaterRunning = true;
                startPlayProgressUpdater();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    /**
     * Initializes the buttons play, ff, rew and the slide etc. to perform their
     * tasks when needed.
     */
    private PlayerFragment initPlayingControls(View v) {
        initButtonPlayStop(v);
        initButtonFF(v);
        initButtonRew(v);
        initSeekBar(v);

        alreadyPlayed = (TextView) v.findViewById(R.id.playerAlreadyPlayed);
        remainingTime = (TextView) v.findViewById(R.id.playerRemainingTime);
        return this;
    }

    private void initSeekBar(View v) {
        seekBar = (SeekBar) v.findViewById(R.id.SeekBar01);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {
                service.seekTo(seekBar1.getProgress());
                if (activeEpisode != null) {
                    activeEpisode.setPlayPosition(seekBar1.getProgress());
                }
                trackingTouch = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {
                trackingTouch = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar1, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (bound && (service != null)) {
                    alreadyPlayed.setText(getAlreadyPlayed(progress));
                    remainingTime.setText("-"
                                          + getRemainingTime(service.getDuration(),
                                                  progress));
                }
            }
        });
    }

    private void initButtonRew(View v) {
        buttonRew = (ImageButton) v.findViewById(R.id.ButtonRewind);
        buttonRew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v1) {
                rewind();
            }
        });
    }

    private void initButtonFF(View v) {
        buttonFF = (ImageButton) v.findViewById(R.id.ButtonFF);
        buttonFF.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v1) {
                fastForward();
            }
        });
    }

    private void initButtonPlayStop(View v) {
        buttonPlayStop =
            (ImageButton) v.findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v1) {
                startStop();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaylistDAOImpl.i().addPlaylistChangedListener(this);
        Singletons.i().getEpisodeDAO().addEpisodeChangedListener(this);

        if (!MediaPlayerService.isRunning()) {
            Intent serviceIntent =
                new Intent(Detlef.getAppContext(), MediaPlayerService.class);
            Detlef.getAppContext().startService(serviceIntent);
        }
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        PlaylistDAOImpl.i().removePlaylistChangeListener(this);
        Singletons.i().getEpisodeDAO().removeEpisodeChangedListener(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.player_layout, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
            getActivity().unbindService(connection);
            bound = false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPlayingControls(view);
        setEpisodeInfoControls(activeEpisode);
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentPaused = false;
        setEpisodeInfoControls(activeEpisode);
        if (!progressUpdaterRunning) {
            progressUpdaterRunning = true;
            startPlayProgressUpdater();
        }
    }

    @Override
    public void onPause() {
        fragmentPaused = true;
        if (service != null) {
            service.stopStreamingIfPaused();
        }
        super.onPause();
    }

    /**
     * Handles the updates of the seek/progressbar as well as the state of the
     * play/pause button.
     */
    private PlayerFragment startPlayProgressUpdater() {
        if (fragmentPaused) {
            progressUpdaterRunning = false;
            return this;
        }
        if (service != null) {
            updateControls();
        } else {
            progressUpdaterRunning = false;
        }
        return this;
    }

    private void updateControls() {
        if ((service.getNextEpisode() != null) && (service.getNextEpisode() != activeEpisode)) {
            setActiveEpisode(service.getNextEpisode());
        }
        if (service.isCurrentlyPlaying()) {
            buttonPlayStop
            .setImageResource(android.R.drawable.ic_media_pause);
            setPlayingSeekBarAndTime();
        } else {
            buttonPlayStop
            .setImageResource(android.R.drawable.ic_media_play);
            setNotPlayingSeekBarAndTime(activeEpisode);
        }
        if ((activeEpisode != null)
                && (activeEpisode.getStorageState() != StorageState.DOWNLOADED)) {
            seekBar.setSecondaryProgress(service.getDownloadProgress());
        } else {
            seekBar.setSecondaryProgress(0);
        }
        Runnable notification = new Runnable() {
            @Override
            public void run() {
                startPlayProgressUpdater();
            }
        };
        playProgressUpdateHandler.postDelayed(notification, PROGRESS_BAR_UPDATE_INTERVAL);
    }

    public PlayerFragment startPlaying() {
        if (service == null) {
            return this;
        }

        try {
            if (!service.isCurrentlyPlaying()) {
                service.setNextEpisode(activeEpisode);
                service.startPlaying();
                buttonPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            }
        } catch (Exception e) {
            showGenericError();
            Log.e(TAG, "startPlaying() Exception", e);
        }
        return this;
    }

    /**
     * Shows a generic error message as Toast.
     */
    private void showGenericError() {
        try {
            Toast.makeText(
                this.getActivity(),
                R.string.error_occured,
                Toast.LENGTH_SHORT
            ).show();
        } catch (Exception e) {
        }
    }

    public PlayerFragment stopPlaying() {
        if (service == null) {
            return this;
        }

        try {
            if (service.isCurrentlyPlaying()) {
                service.pausePlaying();
                buttonPlayStop.setImageResource(android.R.drawable.ic_media_play);
            }
        } catch (Exception e) {
            showGenericError();
            Log.e(TAG, "stopPlaying() Exception", e);
        }
        return this;
    }

    /**
     * Starts or pauses the playback and updates the UI fields accordingly.
     */
    private PlayerFragment startStop() {
        if (service == null) {
            return this;
        }
        try {
            if (service.isCurrentlyPlaying()) {
                stopPlaying();
            } else {
                startPlaying();
            }
        } catch (Exception e) {
            showGenericError();
            Log.e(TAG, "startStop() Exception", e);
        }

        return this;
    }

    /**
     * Handles clicks on the fastForward button.
     */
    private PlayerFragment fastForward() {
        if (service == null) {
            return this;
        }
        try {
            service.fastForward();
            if (service.getNextEpisode() != activeEpisode) {
                setActiveEpisode(service.getNextEpisode());
                if (service.isCurrentlyPlaying()) {
                    stopPlaying();
                    startPlaying();
                }
            }
        } catch (Exception e) {
            showGenericError();
            Log.e(TAG, "fastForward() Exception", e);
        }

        return this;
    }

    /**
     * Handles clicks on the rewind button.
     */
    private PlayerFragment rewind() {
        if (service == null) {
            return this;
        }

        try {
            service.rewind();
            if (service.getNextEpisode() != activeEpisode) {
                setActiveEpisode(service.getNextEpisode());
                if (service.isCurrentlyPlaying()) {
                    stopPlaying();
                    startPlaying();
                }
            }
        } catch (Exception e) {
            showGenericError();
            Log.e(TAG, "fastForward() Exception", e);
        }

        return this;
    }

    private PlayerFragment setEpisodeInfoControls(Episode ep) {
        View view = getView();
        if (view == null) {
            return this;
        }
        WebView episodeDescription = (WebView) getView().findViewById(
                                         R.id.playerEpisodeDescription);
        TextView podcast = (TextView) getView().findViewById(R.id.playerPodcast);
        TextView episode = (TextView) getView().findViewById(R.id.playerEpisode);

        podcastIcon = (ImageView) getView().findViewById(R.id.imageView1);

        if ((ep == null) && (service != null)) {
            ep = service.getNextEpisode();
        }
        // reset episodeDescription in any case, otherwise there will
        // be problems if html text is refreshed.
        episodeDescription.loadData("", "text/html; charset=UTF-8", null);


        if (ep == null) {
            episode.setText(
                getActivity().getText(R.string.no_episode_selected)
                .toString());
            podcast.setText("");
            podcastIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_feed_icon));
        } else {
            episodeDescription.loadData("", "text/html; charset=UTF-8", null);
            episodeDescription.loadData(ep.getDescription() == null ? "" : ep.getDescription(),
                                        "text/html; charset=UTF-8", null);
            podcast.setText(ep.getPodcast().getTitle() == null ? "" : ep.getPodcast()
                            .getTitle());
            episode.setText(ep.getTitle() == null ? "" : ep.getTitle());
            remainingTime.setText("-00:00");
            alreadyPlayed.setText("00:00");
            seekBar.setMax(1);
            seekBar.setProgress(0);
            seekBar.setSecondaryProgress(0);
            setNotPlayingSeekBarAndTime(ep);
            podcastIcon.setImageDrawable(ep.getPodcast().getLogoIcon());
        }

        return this;
    }

    private void setNotPlayingSeekBarAndTime(Episode ep) {
        if (service == null) {
            return;
        }
        if (trackingTouch) {
            return;
        }
        try {
            if (service.episodeFileOK(ep)) {
                MediaMetadataRetriever metaData = new MediaMetadataRetriever();
                metaData.setDataSource(ep.getFilePath());
                String durationString = metaData
                                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int duration = Integer.parseInt(durationString);
                int playingPosition = ep.getPlayPosition();
                int pos = 0;
                if (((playingPosition) > 0) && (playingPosition < duration)) {
                    pos = playingPosition;
                }
                String minutesSecondsRemaining = getRemainingTime(duration, pos);
                String minutesSecondsAlreadyPlayed = getAlreadyPlayed(pos);
                remainingTime.setText("-" + minutesSecondsRemaining);
                alreadyPlayed.setText(minutesSecondsAlreadyPlayed);
                seekBar.setMax(duration);
                seekBar.setProgress(pos);
                seekBar.setSecondaryProgress(0);
            } else if ((service.getNextEpisode() == ep) && service.hasRunningEpisode()) {
                seekBar.setMax(service.getDuration());
                seekBar.setProgress(service.getCurrentPosition());
                alreadyPlayed.setText(getAlreadyPlayed(service.getCurrentPosition()));
                remainingTime.setText("-"
                                      + getRemainingTime(service.getDuration(), service.getCurrentPosition()));
            }
        } catch (Exception ex) {
            Log.d(getClass().getName(),
                  "Error while retrieving duration of episode: " + ex.getMessage());
        }
    }

    private void setPlayingSeekBarAndTime() {
        if (service == null) {
            return;
        }
        if (trackingTouch) {
            return;
        }
        seekBar.setMax(service.getDuration());
        seekBar.setProgress(service.getCurrentPosition());
        alreadyPlayed.setText(getAlreadyPlayed(service.getCurrentPosition()));
        remainingTime.setText("-"
                              + getRemainingTime(service.getDuration(), service.getCurrentPosition()));
    }

    private PlayerFragment setActiveEpisode(Episode ep) {
        if (ep != activeEpisode) {
            activeEpisode = ep;
            setEpisodeInfoControls(ep);
        }
        return this;
    }

    private String getAlreadyPlayed(int progress) {
        return String.format(
                   "%02d:%02d",
                   TimeUnit.MILLISECONDS.toMinutes(progress),
                   TimeUnit.MILLISECONDS.toSeconds(progress)
                   -
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
               );
    }

    private String getRemainingTime(int duration, int progress) {
        if (duration < progress) {
            return "00:00";
        }
        return String.format(
                   "%02d:%02d",
                   TimeUnit.MILLISECONDS.toMinutes(duration - progress),
                   TimeUnit.MILLISECONDS.toSeconds(duration - progress)
                   -
                   TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration
                                              - progress))
               );
    }

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        if ((activeEpisode == null) && (position == 0)) {
            activeEpisode = episode;
            setEpisodeInfoControls(activeEpisode);
        }
    }

    @Override
    public void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition) {
        // not our problem here
    }

    @Override
    public void onPlaylistEpisodeRemoved(int position) {
        // not of interest here.
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        // don't care - let's suppose this doesn't happen,
        // and even if the info is miraculously updated
        // during playback, I'll just ignore this
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
        // thankfully I can be totally indifferent about this
    }

    @Override
    public void onEpisodeDeleted(final Episode episode) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activeEpisode == episode) {
                    stopPlaying();
                    activeEpisode = null;
                    setEpisodeInfoControls(activeEpisode);
                }
            }
        });
    }

    public PlayerFragment setManualEpisode(Episode episode) {
        if (service == null) {
            return this;
        }
        service.setManualEpisode(episode);
        setActiveEpisode(episode);
        stopPlaying();
        startPlaying();
        return this;
    }

}
