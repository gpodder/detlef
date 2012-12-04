
package at.ac.tuwien.detlef.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.db.PlaylistDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;

/**
 * A service that provides methods for playing episodes.
 * 
 * @author johannes
 */
public class MediaPlayerService extends Service implements IMediaPlayerService,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, PlaylistDAO.OnPlaylistChangeListener,
        EpisodeDAO.OnEpisodeChangeListener {

    private PlaylistDAO playlistDAO;
    private EpisodeDAO episodeDAO;
    private ArrayList<Episode> playlistItems;
    private boolean manual = false;
    private Episode manualEpisode;

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
    private int currentPlaylistPosition = 0;
    private static boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        playlistDAO = PlaylistDAOImpl.i();
        playlistItems = playlistDAO.getNonCachedEpisodes();
        playlistDAO.addPlaylistChangedListener(this);
        episodeDAO = EpisodeDAOImpl.i();
        episodeDAO.addEpisodeChangedListener(this);
        if ((nextEpisode == null) && !playlistItems.isEmpty()) {
            nextEpisode = playlistItems.get(0);
        }
    }

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
            if (activeEpisode != null) {
                activeEpisode.setStorageState(StorageState.NOT_ON_DEVICE);
                episodeDAO.updateStorageState(activeEpisode);
            }
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
        if (activeEpisode != null) {
            int playPosition = activeEpisode.getPlayPosition();
            Log.d(getClass().getName(), "Setting play position to " + playPosition);
            if (playPosition < mediaPlayer.getDuration() && playPosition > 0) {
                mediaPlayer.seekTo(playPosition);
            }
        }
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateEpisodeCompleted();
        haveRunningEpisode = false;
        currentlyPlaying = false;
        if (manual) {
            manual = false;
            manualEpisode = null;
            currentPlaylistPosition = 0;
            if (!playlistItems.isEmpty()) {
                setNextEpisode(playlistItems.get(currentPlaylistPosition));
            } else {
                nextEpisode = null;
            }
        } else {
            playlistDAO.removeEpisode(currentPlaylistPosition);
            if (!playlistItems.isEmpty()) {
                if (currentPlaylistPosition >= playlistItems.size()) {
                    currentPlaylistPosition = playlistItems.size() - 1;
                    setNextEpisode(playlistItems.get(currentPlaylistPosition));
                }
                startPlaying();
            } else {
                currentPlaylistPosition = 0;
                nextEpisode = null;
            }
        }
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
        updateEpisodePlayState();
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

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#fastForward()
     */
    @Override
    public IMediaPlayerService fastForward() {
        updateEpisodePlayState();
        if (manual && !playlistItems.isEmpty()) {
            currentPlaylistPosition = 0;
            nextEpisode = playlistItems.get(currentPlaylistPosition);
            manual = false;
        } else if (currentPlaylistPosition < (playlistItems.size() - 1)) {
            currentPlaylistPosition++;
            nextEpisode = playlistItems.get(currentPlaylistPosition);
            if (currentPlaylistPosition > 0) {
                manualEpisode = null;
            }
        } else if (!playlistItems.isEmpty()) {
            currentPlaylistPosition = 0;
            nextEpisode = playlistItems.get(currentPlaylistPosition);
        }
        return this;
    }

    @Override
    public IMediaPlayerService rewind() {
        updateEpisodePlayState();
        if ((currentPlaylistPosition == 0) && (manualEpisode != null)) {
            manual = true;
        } else {
            if (currentPlaylistPosition > 0) {
                currentPlaylistPosition--;
            }
            if (!playlistItems.isEmpty()) {
                nextEpisode = playlistItems.get(currentPlaylistPosition);
            }
        }
        return this;
    }

    @Override
    public IMediaPlayerService skipToPosition(int position) {
        updateEpisodePlayState();
        if (position >= playlistItems.size()) {
            Log.e(getClass().getName(), "Wrong playlist index: " + position + ", current size: "
                    + playlistItems.size());
            return this;
        }
        Log.d(getClass().getName(), "Skipping to position " + position);
        manual = false;
        currentPlaylistPosition = position;
        nextEpisode = playlistItems.get(currentPlaylistPosition);
        manualEpisode = null;
        return this;
    }

    @Override
    public boolean episodeFileOK(Episode ep) {
        if (ep == null) {
            return false;
        }
        if ((ep.getFilePath() == null) || ep.getFilePath().equals("")) {
            Log.e(getClass().getName(), "Episode " + ep.getGuid() + " has an empty file path");
            return false;
        }
        File f = new File(ep.getFilePath());
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            Log.e(getClass().getName(), "Episode " + ep.getGuid() + " has an invalid file path: "
                    + f.getAbsolutePath());
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
            activeEpisode = getNextEpisode();
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

    @Override
    public IMediaPlayerService setNextEpisode(Episode ep) {
        this.nextEpisode = ep;
        return this;
    }

    @Override
    public Episode getNextEpisode() {
        if (manual) {
            return manualEpisode;
        } else {
            return nextEpisode;
        }
    }

    @Override
    public void onPlaylistEpisodeAdded(int position, Episode episode) {
        playlistItems.add(position, episode);
        if (position <= currentPlaylistPosition) {
            currentPlaylistPosition = Math.min(currentPlaylistPosition + 1,
                    playlistItems.size() - 1);
        }
    }

    @Override
    public void onPlaylistEpisodePositionChanged(int firstPosition, int secondPosition) {
        Episode ep = playlistItems.remove(firstPosition);
        playlistItems.add(secondPosition, ep);
        if ((firstPosition <= currentPlaylistPosition)
                && (secondPosition >= currentPlaylistPosition)) {
            currentPlaylistPosition = Math.max(currentPlaylistPosition - 1, 0);
        } else if ((firstPosition >= currentPlaylistPosition)
                && (secondPosition <= currentPlaylistPosition)) {
            currentPlaylistPosition = Math.min(currentPlaylistPosition + 1,
                    playlistItems.size() - 1);
        }
    }

    @Override
    public void onPlaylistEpisodeRemoved(int position) {
        playlistItems.remove(position);
        if (position <= currentPlaylistPosition) {
            currentPlaylistPosition = Math.max(currentPlaylistPosition - 1, 0);
            if (!playlistItems.isEmpty()) {
                nextEpisode = playlistItems.get(currentPlaylistPosition);
            }
        }
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        // not our problem
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
        if (nextEpisode == null) {
            nextEpisode = episode;
        }
    }

    @Override
    public void onEpisodeDeleted(Episode episode) {
        if (activeEpisode == episode) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            activeEpisode = null;
        }
        if (nextEpisode == episode) {
            nextEpisode = null;
        }
    }

    @Override
    public Episode getManualEpisode() {
        return manualEpisode;
    }

    @Override
    public void setManualEpisode(Episode manualEpisode) {
        this.manualEpisode = manualEpisode;
        manual = true;
    }

    @Override
    public boolean isManual() {
        return manual;
    }

    private void updateEpisodeCompleted() {
        if (activeEpisode == null) {
            return;
        }
        activeEpisode.setPlayPosition(0);
        episodeDAO.updatePlayPosition(activeEpisode);
    }

    private void updateEpisodePlayState() {
        if (activeEpisode == null) {
            return;
        }
        if (mediaPlayerPrepared) {
            activeEpisode.setPlayPosition(mediaPlayer.getCurrentPosition());
            episodeDAO.updatePlayPosition(activeEpisode);
        }
    }

}
