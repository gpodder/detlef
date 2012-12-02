
package at.ac.tuwien.detlef.fragments;

import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;

public class PlayerFragment extends Fragment implements PlaylistDAO.OnPlaylistChangeListener {

    private static final int PROGRESS_BAR_UPDATE_INTERVAL = 1000;

    private final Handler playProgressUpdateHandler = new Handler();
    private IMediaPlayerService service;
    private Episode activeEpisode = null;

    private boolean fragmentPaused = true;
    private boolean progressUpdaterRunning = false;
    private boolean bound = false;

    private ImageButton buttonPlayStop;
    private SeekBar seekBar;
    private TextView alreadyPlayed;
    private TextView remainingTime;
    private ImageButton buttonFF;
    private ImageButton buttonRew;

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private ServiceConnection connection = new ServiceConnection() {

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
    private PlayerFragment initPlayingControls() {
        initButtonPlayStop();
        initButtonFF();
        initButtonRew();
        initSeekBar();

        alreadyPlayed = (TextView) getActivity().findViewById(R.id.playerAlreadyPlayed);
        remainingTime = (TextView) getActivity().findViewById(R.id.playerRemainingTime);
        return this;
    }

    private void initSeekBar() {
        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);
        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int progress = seekBar.getProgress();
                if (bound && (service != null)) {
                    alreadyPlayed.setText(getAlreadyPlayed(progress));
                    remainingTime.setText("-"
                            + getRemainingTime(service.getDuration(),
                                    progress));
                }
                service.seekTo(progress);
                return false;
            }
        });
    }

    private void initButtonRew() {
        buttonRew = (ImageButton) getActivity().findViewById(R.id.ButtonRewind);
        buttonRew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rewind();
            }
        });
    }

    private void initButtonFF() {
        buttonFF = (ImageButton) getActivity().findViewById(R.id.ButtonFF);
        buttonFF.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fastForward();
            }
        });
    }

    private void initButtonPlayStop() {
        buttonPlayStop =
                (ImageButton) getActivity().findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!MediaPlayerService.isRunning()) {
            Intent serviceIntent =
                    new Intent(Detlef.getAppContext(), MediaPlayerService.class);
            Detlef.getAppContext().startService(serviceIntent);
        }
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
        initPlayingControls();
        setEpisodeInfoControls(activeEpisode);
    }

    @Override
    public void onResume() {
        super.onResume();
        PlaylistDAOImpl.i().addPlaylistChangedListener(this);
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
        PlaylistDAOImpl.i().removePlaylistChangeListener(this);
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
        setSeekBarAndTime();

        if (service.isCurrentlyPlaying()) {
            buttonPlayStop
                    .setImageResource(android.R.drawable.ic_media_pause);
            Runnable notification = new Runnable() {
                @Override
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            playProgressUpdateHandler.postDelayed(notification, PROGRESS_BAR_UPDATE_INTERVAL);
        } else {
            buttonPlayStop
                    .setImageResource(android.R.drawable.ic_media_play);
            if (!service.hasRunningEpisode()) {
                seekBar.setMax(1);
                seekBar.setProgress(0);
            }
            progressUpdaterRunning = false;
        }
    }

    private void setSeekBarAndTime() {
        seekBar.setMax(service.getDuration());
        seekBar.setProgress(service.getCurrentPosition());
        alreadyPlayed.setText(getAlreadyPlayed(service.getCurrentPosition()));
        remainingTime.setText("-"
                + getRemainingTime(service.getDuration(), service.getCurrentPosition()));
    }

    public PlayerFragment startPlaying() {
        if (service == null) {
            return this;
        }

        if (!service.isCurrentlyPlaying()) {
            service.setNextEpisode(activeEpisode);
            service.startPlaying();
            buttonPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            startPlayProgressUpdater();
        }
        return this;
    }

    public PlayerFragment stopPlaying() {
        if (service == null) {
            return this;
        }
        if (service.isCurrentlyPlaying()) {
            service.pausePlaying();
            buttonPlayStop.setImageResource(android.R.drawable.ic_media_play);
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
        if (service.isCurrentlyPlaying()) {
            stopPlaying();
        } else {
            startPlaying();
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
        service.fastForward();
        if (service.getNextEpisode() != activeEpisode) {
            setActiveEpisode(service.getNextEpisode());
            if (service.isCurrentlyPlaying()) {
                stopPlaying();
                startPlaying();
            }
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
        service.rewind();
        if (service.getNextEpisode() != activeEpisode) {
            setActiveEpisode(service.getNextEpisode());
            if (service.isCurrentlyPlaying()) {
                stopPlaying();
                startPlaying();
            }
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

        if ((ep == null) && (service != null)) {
            Log.d(getClass().getName(), "Getting next episode from service");
            ep = service.getNextEpisode();
        }

        if (ep == null) {
            episode.setText(
                    getActivity().getText(R.string.no_episode_selected)
                            .toString());
            podcast.setText("");
            episodeDescription.loadData("", "text/html; charset=UTF-8", null);
        } else {
            episodeDescription.loadData(ep.getDescription() == null ? "" : ep.getDescription(),
                    "text/html; charset=UTF-8", null);
            podcast.setText(ep.getPodcast().getTitle() == null ? "" : ep.getPodcast()
                    .getTitle());
            episode.setText(ep.getTitle() == null ? "" : ep.getTitle());
        }
        return this;
    }

    public PlayerFragment setActiveEpisode(Episode ep) {
        if (ep != activeEpisode) {
            activeEpisode = ep;
            setEpisodeInfoControls(ep);
        }
        return this;
    }

    private String getAlreadyPlayed(int progress) {
        if (service != null) {
            return String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(progress),
                    TimeUnit.MILLISECONDS.toSeconds(progress)
                            -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                    );
        }
        return "00:00";
    }

    private String getRemainingTime(int duration, int progress) {
        if (service != null) {
            return String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration - progress),
                    TimeUnit.MILLISECONDS.toSeconds(duration - progress)
                            -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration
                                    - progress))
                    );
        }
        return "00:00";
    }

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        Log.d(getClass().getName(), "Getting episode at position " + position);
        if ((activeEpisode == null) && (position == 0)) {
            Log.d(getClass().getName(), "setting active episode to " + episode);
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

}
