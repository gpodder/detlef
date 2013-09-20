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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.PlaylistDAO;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.gpodder.events.PlaylistChangedEvent;
import de.greenrobot.event.EventBus;

/**
 * A service that provides methods for playing episodes.
 *
 * @author johannes
 */
public class MediaPlayerService extends Service implements IMediaPlayerService,
    MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, PlaylistDAO.OnPlaylistChangeListener,
    EpisodeDAO.OnEpisodeChangeListener, OnBufferingUpdateListener {

    private static final double HUNDRED_PERCENT = 100.0d;

    private static final String TAG = MediaPlayerService.class.getName();

    public static final String EXTRA_MEDIA_CONTROL = "EXTRA_MEDIA_CONTROL";
    public static final int EXTRA_PREVIOUS = 0;
    public static final int EXTRA_PLAY_PAUSE = 1;
    public static final int EXTRA_NEXT = 2;
    public static final int EXTRA_CLOSE_NOTIFICATION = 3;

    private PlaylistDAO playlistDAO;
    private EpisodeDAO episodeDAO;
    private ArrayList<Episode> playlistItems;
    private boolean manual = false;
    private Episode manualEpisode;
    private TelephonyManager telManager;
    private int bufferState;

    private BroadcastReceiver mediaBroadcastReceiver;

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
    private boolean wasPlayingBeforeCall = false;

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                if (isCurrentlyPlaying()) {
                    wasPlayingBeforeCall = true;
                    pausePlaying();
                } else {
                    wasPlayingBeforeCall = false;
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (wasPlayingBeforeCall) {
                    startPlaying();
                    wasPlayingBeforeCall = false;
                }
                break;
            default:
                Log.d(getClass().getName(), "Unknown call state: " + state);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        /* The HEADSET_PLUG broadcast intent is sent with the
         * Intent.FLAG_RECEIVER_REGISTERED_ONLY flag, which means
         * we need to register the broadcast receiver here instead of
         * letting the manifest handle it for us. Additionally,
         * since we need a reference to the Service, it's most convenient to
         * handle the receiver construction here. */

        mediaBroadcastReceiver = new MediaBroadcastReceiver(this);
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        receiverFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mediaBroadcastReceiver, receiverFilter);

        playlistDAO = Singletons.i().getPlaylistDAO();
        playlistItems = playlistDAO.getNonCachedEpisodes();
        playlistDAO.addPlaylistChangedListener(this);

        episodeDAO = Singletons.i().getEpisodeDAO();
        episodeDAO.addEpisodeChangedListener(this);

        if ((nextEpisode == null) && !playlistItems.isEmpty()) {
            nextEpisode = playlistItems.get(0);
        }

        telManager = (TelephonyManager) Detlef.getAppContext().getSystemService(
                         Context.TELEPHONY_SERVICE);
        telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
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
        if (activeEpisode.getStorageState() != StorageState.DOWNLOADED) {
            return Uri.parse(activeEpisode.getUrl());
        }
        if (!episodeFileOK(activeEpisode)) {
            activeEpisode.setStorageState(StorageState.NOT_ON_DEVICE);
            episodeDAO.update(activeEpisode);
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

        unregisterReceiver(mediaBroadcastReceiver);

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
        setCurrentlyPlaying(false);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        haveRunningEpisode = true;
        setCurrentlyPlaying(true);
        mediaPlayerPrepared = true;
        if (activeEpisode != null) {
            int playPosition = activeEpisode.getPlayPosition();
            Log.d(getClass().getName(), "Setting play position to " + playPosition);
            if ((playPosition < mediaPlayer.getDuration()) && (playPosition > 0)) {
                mediaPlayer.seekTo(playPosition);
            }
            if ((activeEpisode.getActionState() == ActionState.NEW)
                    || (activeEpisode.getActionState() == ActionState.DOWNLOAD)) {
                activeEpisode.setActionState(ActionState.PLAY);
                episodeDAO.update(activeEpisode);
            }
        }
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateEpisodeCompleted();
        haveRunningEpisode = false;
        setCurrentlyPlaying(false);
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
            EventBus.getDefault().post(new PlaylistChangedEvent());
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
        if (!running) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            running = true;
        }

        if (intent.hasExtra(EXTRA_MEDIA_CONTROL)) {
            handleMediaControlIntent(intent.getIntExtra(EXTRA_MEDIA_CONTROL, EXTRA_PLAY_PAUSE));
        }

        return Service.START_NOT_STICKY;
    }

    /**
     * Handles an incoming media control intent (which can be sent from a
     * notification).
     *
     * @param command The intent's EXTRA_MEDIA_CONTRL extra. One of
     *            EXTRA_PLAY_PAUSE, EXTRA_PREVIOUS, EXTRA_NEXT.
     */
    private void handleMediaControlIntent(int command) {
        Log.d(TAG, String.format("Incoming intent with extra %d", command));

        /*
         * The logic for PREVIOUS and NEXT is nicked from PlayerFragment and
         * could be improved.
         */

        switch (command) {
        case EXTRA_PREVIOUS:
            rewind();

            if ((getNextEpisode() == activeEpisode) || !isCurrentlyPlaying()) {
                return;
            }

            pausePlaying();
            startPlaying();
            break;
        case EXTRA_PLAY_PAUSE:
            if (isCurrentlyPlaying()) {
                pausePlaying();
            } else {
                startPlaying();
            }
            break;
        case EXTRA_NEXT:
            fastForward();

            if ((getNextEpisode() == activeEpisode) || !isCurrentlyPlaying()) {
                return;
            }

            pausePlaying();
            startPlaying();
            break;
        case EXTRA_CLOSE_NOTIFICATION:
            mediaPlayer.reset();
            haveRunningEpisode = false;
            MediaPlayerNotification.cancel(this);
            break;
        default:
            Log.w(TAG, String.format("Invalid incomind media control intent: %d", command));
            break;
        }
    }

    /*
     * (non-Javadoc)
     * @see at.ac.tuwien.detlef.mediaplayer.IMediaPlayerService#pausePlaying()
     */
    @Override
    public IMediaPlayerService pausePlaying() {
        mediaPlayer.pause();
        updateEpisodePlayState();
        setCurrentlyPlaying(false);
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
            mediaPlayer.seekTo(seekTo);
        } else if (getNextEpisode() != null) {
            getNextEpisode().setPlayPosition(progress);
            episodeDAO.update(getNextEpisode());
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
            return false;
        }
        File f = new File(ep.getFilePath());
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            Log.d(getClass().getName(), "Episode " + ep.getGuid() + " has an invalid file path: "
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
        if (haveRunningEpisode && (nextEpisode == activeEpisode)
                && (activeEpisode.getStorageState() != StorageState.DOWNLOADED)) {
            setCurrentlyPlaying(true);
            mediaPlayer.start();
        } else {
            activeEpisode = getNextEpisode();
            if (activeEpisode != null) {
                prepareEpisodePlayback();
            } else {
                haveRunningEpisode = false;
                setCurrentlyPlaying(false);
                mediaPlayerPrepared = false;
                mediaPlayer.reset();
            }
        }
        return this;
    }

    private MediaPlayerService prepareEpisodePlayback() {
        haveRunningEpisode = true;
        setCurrentlyPlaying(true);
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
            haveRunningEpisode = false;
            setCurrentlyPlaying(false);
            mediaPlayerPrepared = false;
            mediaPlayer.reset();
        } catch (IOException e) {
            Log.e(getClass().getCanonicalName(),
                  "Media Player startup failed!", e);
            haveRunningEpisode = false;
            setCurrentlyPlaying(false);
            mediaPlayerPrepared = false;
            mediaPlayer.reset();
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
        }
        return nextEpisode;
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
        if (manualEpisode == episode) {
            manualEpisode = null;
        }
    }

    @Override
    public void setManualEpisode(Episode manualEpisode) {
        this.manualEpisode = manualEpisode;
        manual = true;
    }

    private void updateEpisodeCompleted() {
        if (activeEpisode == null) {
            return;
        }
        activeEpisode.setPlayPosition(0);
        activeEpisode.setActionState(ActionState.DELETE);
        episodeDAO.update(activeEpisode);
    }

    private void updateEpisodePlayState() {
        if (activeEpisode == null) {
            return;
        }
        if (mediaPlayerPrepared) {
            activeEpisode.setPlayPosition(mediaPlayer.getCurrentPosition());
            episodeDAO.update(activeEpisode);
        }
    }

    private void setCurrentlyPlaying(boolean currentlyPlaying) {
        this.currentlyPlaying = currentlyPlaying;

        /* Update the notification. */
        MediaPlayerNotification.create(this, currentlyPlaying,
                                       (activeEpisode == null) ? null : activeEpisode.getTitle());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (!mediaPlayerPrepared) {
            this.bufferState = percent;
        } else {
            this.bufferState = (int)((mp.getDuration() * percent) / HUNDRED_PERCENT);
        }
    }

    @Override
    public int getDownloadProgress() {
        return bufferState;
    }

    @Override
    public void stopStreamingIfPaused() {
        if (!currentlyPlaying) {
            mediaPlayer.reset();
            haveRunningEpisode = false;
        }
    }

}
