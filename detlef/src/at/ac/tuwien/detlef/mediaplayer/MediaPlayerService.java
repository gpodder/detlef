package at.ac.tuwien.detlef.mediaplayer;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import at.ac.tuwien.detlef.R;

/**
 * A service that provides methods for playing episodes.
 * 
 * @author johannes
 * 
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    /**
     * Binder that allows local classes to communicate with the service.
     * 
     * @author johannes
     * 
     */
    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public static boolean isRunning() {
        return running;
    }

    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MediaPlayerBinder();

    private boolean haveRunningEpisode = false;
    private boolean currentlyPlaying = false;
    private boolean mediaPlayerPrepared = false;

    private static boolean running = false;

    /**
     * @return The current media player position.
     */
    public int getCurrentPosition() {
        if (mediaPlayerPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * @return Returns the duration of the currently played piece.
     */
    public int getDuration() {
        if (mediaPlayerPrepared) {
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    /**
     * TODO fix this to get the actual next episode.
     * 
     * @return Gets the URI of the next episode to be played: The active
     *         episode, the next episode in the playlist or null.
     */
    private Uri getNextUri() {
        return Uri.parse("android.resource://at.ac.tuwien.detlef/"
                + R.raw.testsong_20_sec);
    }

    /**
     * @return Returns if the player is currently playing or paused/stopped.
     */
    public boolean isCurrentlyPlaying() {
        return currentlyPlaying;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        haveRunningEpisode = false;
        currentlyPlaying = false;
    }

    @Override
    public void onDestroy() {
        running = false;
        mediaPlayerPrepared = false;
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(getClass().getCanonicalName(),
                "Error while playing media! What: " + what + ", extra: "
                        + extra);
        mediaPlayerPrepared = false;
        mp.reset();
        haveRunningEpisode = false;
        currentlyPlaying = false;
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        haveRunningEpisode = true;
        currentlyPlaying = true;
        mediaPlayerPrepared = true;
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        return Service.START_NOT_STICKY;
    }

    public void pausePlaying() {
        mediaPlayer.pause();
        currentlyPlaying = false;
    }

    /**
     * Sets the media player progress.
     * 
     * @param progress
     *            The progress to set the media player to.
     */
    public void seekTo(int progress) {
        if (mediaPlayerPrepared) {
            mediaPlayer.seekTo(progress);
        }
    }

    /**
     * Switches next URI to the next file to be played and updates the active
     * episode.
     */
    private void chooseNextToPlay() {
        // TODO hook up with playlist.
    }

    public void fastForward() {
        mediaPlayer.stop();
        chooseNextToPlay();
        haveRunningEpisode = false;
        currentlyPlaying = false;
        startPlaying();
    }

    /**
     * Starts playback of the active episode (if any).
     * 
     * (TODO THIS DOES NOT WORK YET:) Otherwise, gets the first episode from the
     * playlist and starts on that.
     * 
     * If there is no active episode and the playlist is empty, does nothing.
     */
    public void startPlaying() {
        if (haveRunningEpisode) {
            currentlyPlaying = true;
            mediaPlayer.start();
        } else {
            haveRunningEpisode = true;
            currentlyPlaying = true;
            mediaPlayerPrepared = false;
            mediaPlayer.reset();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer
                        .setDataSource(getApplicationContext(), getNextUri());
                mediaPlayer.prepareAsync(); // prepare async to not block main
                // thread
            } catch (IllegalStateException e) {
                Log.e(getClass().getCanonicalName(),
                        "Media Player startup failed!", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(getClass().getCanonicalName(),
                        "Media Player startup failed!", e);
                e.printStackTrace();
            }
        }

    }

    /**
     * @return Whether the player service has a currently active episode (paused
     *         or not). This becomes false when a file has been played to its
     *         end.
     */
    public boolean hasRunningEpisode() {
        return haveRunningEpisode;
    }
}