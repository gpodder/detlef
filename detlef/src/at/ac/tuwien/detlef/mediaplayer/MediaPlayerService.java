
package at.ac.tuwien.detlef.mediaplayer;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import at.ac.tuwien.detlef.domain.Episode;

/**
 * A service that provides methods for playing episodes.
 * 
 * @author johannes
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, IMediaPlayerService {

    private static final String LOG_TAG = MediaPlayerService.class.getName();

    /**
     * Binder that allows local classes to communicate with the service.
     * 
     * @author johannes
     */
    public class MediaPlayerBinder extends Binder {
        public IMediaPlayerService getService() {
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
    private Episode activeEpisode;
    private Episode nextEpisode;

    private static boolean running = false;

    /*
     * (non-Javadoc)
     * @see
     * at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#getCurrentPosition()
     */
    @Override
    public int getCurrentPosition() {
        if (mediaPlayerPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#getDuration()
     */
    @Override
    public int getDuration() {
        if (mediaPlayerPrepared) {
            return mediaPlayer.getDuration();
        }
        return -1;
    }

    /**
     * @return Gets the URI of the active episode or null
     */
    private Uri getActiveUri() {
        if (activeEpisode == null) {
            return null;
        }
        if (!episodeFileOK(activeEpisode)) {
            return null;
        }
        return Uri.fromFile(new File(activeEpisode.getFilePath()));
    }

    /*
     * (non-Javadoc)
     * @see
     * at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#isCurrentlyPlaying()
     */
    @Override
    public boolean isCurrentlyPlaying() {
        return currentlyPlaying;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(getClass().getName(), "Completion");
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

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#pausePlaying()
     */
    @Override
    public IMediaPlayerService pausePlaying() {
        mediaPlayer.pause();
        currentlyPlaying = false;
        return this;
    }

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#seekTo(int)
     */
    @Override
    public IMediaPlayerService seekTo(int progress) {
        if (mediaPlayerPrepared) {
            int seekTo = Math.max(Math.min(progress, getDuration()), 0);
            Log.d(getClass().getName(), "Seek to " + seekTo + ", duration: " + getDuration());
            mediaPlayer.seekTo(seekTo);
        }
        return this;
    }

    /**
     * Switches next URI to the next file to be played and updates the active
     * episode.
     */
    private void chooseNextToPlay() {
        // TODO hook up with playlist.
    }

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#fastForward()
     */
    @Override
    public IMediaPlayerService fastForward() {
        mediaPlayer.stop();
        chooseNextToPlay();
        haveRunningEpisode = false;
        currentlyPlaying = false;
        startPlaying();
        return this;
    }

    @Override
    public boolean episodeFileOK(Episode ep) {
        if ((ep.getFilePath() == null) || ep.getFilePath().equals("")) {
            Log.e(getClass().getName(), "Episode " + ep.getGuid() + " has an empty file path");
            return false;
        }
        File f = new File(ep.getFilePath());
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            Log.e(getClass().getName(), "Episode " + ep.getGuid() + " has an invalid file path: "
                    + ep.getFilePath());
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#startPlaying()
     */
    @Override
    public IMediaPlayerService startPlaying() {
        if (haveRunningEpisode && (nextEpisode == activeEpisode)) {
            currentlyPlaying = true;
            mediaPlayer.start();
        } else {
            activeEpisode = nextEpisode;
            if (episodeFileOK(activeEpisode)) {
                prepareEpisodePlayback();
            } else {
                haveRunningEpisode = false;
                currentlyPlaying = false;
                mediaPlayerPrepared = false;
                mediaPlayer.reset();
            }
        }
        return this;
    }

    private MediaPlayerService prepareEpisodePlayback() {
        haveRunningEpisode = true;
        currentlyPlaying = true;
        mediaPlayerPrepared = false;
        mediaPlayer.reset();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer
                    .setDataSource(getApplicationContext(), getActiveUri());
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
        return this;
    }

    /*
     * (non-Javadoc)
     * @see
     * at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#hasRunningEpisode()
     */
    @Override
    public boolean hasRunningEpisode() {
        return haveRunningEpisode;
    }

    /*
     * (non-Javadoc)
     * @see
     * at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#setActiveEpisode(
     * at.ac.tuwien.detlef.domain.Episode)
     */
    @Override
    public IMediaPlayerService setNextEpisode(Episode ep) {
        this.nextEpisode = ep;
        return this;
    }
}
