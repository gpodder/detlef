
package at.ac.tuwien.detlef.fragments;

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
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;

public class PlayerFragment extends Fragment {

    private static final int PROGRESS_BAR_UPDATE_INTERVAL = 1000;

    // TODO icon for service

    private ImageButton buttonPlayStop;
    private SeekBar seekBar;
    private boolean bound = false;
    private IMediaPlayerService service;

    private final Handler handler = new Handler();

    private Episode activeEpisode = null;
    private boolean fragmentPaused = true;
    private boolean playProgressUpdaterRunning = false;

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void
                onServiceConnected(ComponentName className, IBinder iBinder) {
            bound = true;
            MediaPlayerService.MediaPlayerBinder binder =
                    (MediaPlayerService.MediaPlayerBinder) iBinder;
            service = binder.getService();
            activeEpisode = service.getNextEpisode();
            setEpisodeInfoControls(activeEpisode);
            startPlayProgressUpdater();
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
        buttonPlayStop =
                (ImageButton) getActivity().findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });

        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);
        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int progress = seekBar.getProgress();
                service.seekTo(progress);
                return false;
            }
        });
        return this;
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
        Log.d(getClass().getCanonicalName(), "onResume");
        setPaused(false);
        setEpisodeInfoControls(activeEpisode);
        startPlayProgressUpdater();
    }

    @Override
    public void onPause() {
        setPaused(true);
        super.onPause();
    }

    /**
     * Handles the updates of the seek/progressbar as well as the state of the
     * play/pause button.
     */
    private synchronized PlayerFragment startPlayProgressUpdater() {
        Log.d(getClass().getCanonicalName(), "startPlayProgressUpdater");
        if (playProgressUpdaterRunning) {
            Log.d(getClass().getCanonicalName(),
                    "PlayProgressUpdater already running, not starting again");
            return this;
        }
        if (getPaused()) {
            Log.d(getClass().getCanonicalName(), "fragmentPaused");
            playProgressUpdaterRunning = false;
            return this;
        }
        if (service != null) {
            updateControls();
        }
        return this;
    }

    private synchronized void updateControls() {
        Log.d(getClass().getCanonicalName(), "setting seekbar etc.");
        seekBar.setMax(service.getDuration());
        seekBar.setProgress(service.getCurrentPosition());
        if (service.isCurrentlyPlaying()) {
            playProgressUpdaterRunning = true;
            Log.d(getClass().getCanonicalName(), "is currently playing");
            buttonPlayStop
                    .setImageResource(android.R.drawable.ic_media_pause);
            Runnable notification = new Runnable() {
                @Override
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, PROGRESS_BAR_UPDATE_INTERVAL);
        } else {
            playProgressUpdaterRunning = false;
            Log.d(getClass().getCanonicalName(), "is paused");
            buttonPlayStop
                    .setImageResource(android.R.drawable.ic_media_play);
            if (!service.hasRunningEpisode()) {
                seekBar.setMax(1);
                seekBar.setProgress(0);
            }
        }
    }

    public PlayerFragment startPlaying() {
        if (!service.isCurrentlyPlaying()) {
            service.startPlaying();
            buttonPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            startPlayProgressUpdater();
        }
        return this;
    }

    public PlayerFragment stopPlaying() {
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
        if (service.isCurrentlyPlaying()) {
            stopPlaying();
        } else {
            startPlaying();
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
            stopPlaying();
            activeEpisode = ep;
            service.setNextEpisode(activeEpisode);
            setEpisodeInfoControls(ep);
        }
        return this;
    }

    private synchronized void setPaused(boolean p) {
        this.fragmentPaused = p;
    }

    private synchronized boolean getPaused() {
        return fragmentPaused;
    }

}
