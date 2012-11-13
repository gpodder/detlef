package at.ac.tuwien.detlef.fragments;

import java.io.File;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;

public class PlayerFragment extends Fragment {

    private static final int PROGRESS_BAR_UPDATE_INTERVAL = 1000;

    private Episode activeEpisode;

    private ImageButton buttonPlayStop;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;

    private boolean playing = false;

    private DependencyAssistant assistant = DependencyAssistant
            .getDependencyAssistant();

    private final Handler handler = new Handler();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("playing", playing);
        savedInstanceState.putLong("episodeID", activeEpisode.getId());
        // etc.
        super.onSaveInstanceState(savedInstanceState);
    }

    private void initViews() {
        buttonPlayStop =
                (ImageButton) getActivity().findViewById(R.id.ButtonPlayStop);
        buttonPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playing) {
                    startPlaying();
                } else {
                    pausePlaying();
                }
            }
        });

        mediaPlayer =
                MediaPlayer.create(this.getActivity(),
                        Uri.fromFile(new File("/media/sdcard/play.mp3")));

        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int progress = seekBar.getProgress();
                setProgress(progress);
                return false;
            }
        });
    }

    private void startStop() {
        if (playing) {
            startPlaying();
        } else {
            pausePlaying();
        }
    }

    public void startPlayProgressUpdater() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                @Override
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, PROGRESS_BAR_UPDATE_INTERVAL);
        } else {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        playing = savedInstanceState.getBoolean("playing");
        activeEpisode = null;
        // TODO Episode

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.player_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView =
                (TextView) getActivity().findViewById(
                        R.id.playerEpisodeDescription);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void setEpisode(Episode episode) {
        activeEpisode = episode;
    }

    public void startPlaying() {
        buttonPlayStop.setImageDrawable(getResources().getDrawable(
                android.R.drawable.ic_media_play));
        try {
            mediaPlayer.start();
            startPlayProgressUpdater();
        } catch (IllegalStateException e) {
            mediaPlayer.pause();
        }
    }

    public void pausePlaying() {
        buttonPlayStop.setImageDrawable(getResources().getDrawable(
                android.R.drawable.ic_media_pause));
        mediaPlayer.pause();
    }

    public void nextEpisode() {
        // TODO
    }

    public void rewindEpisode() {
        // TODO
    }

    public void setProgress(int progress) {
        mediaPlayer.seekTo(progress);
    }
}
