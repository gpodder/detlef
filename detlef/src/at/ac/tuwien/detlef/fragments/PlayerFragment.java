package at.ac.tuwien.detlef.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.mediaplayer.MediaPlayerService;

public class PlayerFragment extends Fragment {

    private static final int PROGRESS_BAR_UPDATE_INTERVAL = 200;

    private ImageButton buttonPlayStop;
    private SeekBar seekBar;
    private boolean bound = false;
    private MediaPlayerService service;

    private final Handler handler = new Handler();

    /**
     * Handles the connection to the MediaPlayerService that plays music.
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void
                onServiceConnected(ComponentName className, IBinder iBinder) {
            MediaPlayerService.MediaPlayerBinder binder =
                    (MediaPlayerService.MediaPlayerBinder) iBinder;
            service = binder.getService();
            bound = true;

            if (service.hasRunningEpisode()) {
                startPlayProgressUpdater();
            }
            if (service.isCurrentlyPlaying()) {
                buttonPlayStop
                        .setImageResource(android.R.drawable.ic_media_pause);
            } else {
                buttonPlayStop
                        .setImageResource(android.R.drawable.ic_media_play);
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
    private void initPlayingControls() {
        buttonPlayStop =
                (ImageButton) getActivity().findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop();
            }
        });

        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);
        // TODO
        // seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int progress = seekBar.getProgress();
                // TODO
                service.seekTo(progress);
                return false;
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

        TextView textView =
                (TextView) getActivity().findViewById(
                        R.id.playerEpisodeDescription);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * Handles the updates of the seek/progressbar as well as the state of the
     * play/pause button.
     */
    private void startPlayProgressUpdater() {
        seekBar.setMax(service.getDuration());
        seekBar.setProgress(service.getCurrentPosition());
        if (service.hasRunningEpisode()) {
            Runnable notification = new Runnable() {
                @Override
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, PROGRESS_BAR_UPDATE_INTERVAL);
        }
    }

    /**
     * Starts or pauses the playback and updates the UI fields accordingly.
     */
    private void startStop() {
        if (service.isCurrentlyPlaying()) {
            service.pausePlaying();
            buttonPlayStop.setImageResource(android.R.drawable.ic_media_play);
        } else {
            service.startPlaying();
            buttonPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            startPlayProgressUpdater();
        }
    }
}
