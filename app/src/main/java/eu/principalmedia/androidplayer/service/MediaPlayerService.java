package eu.principalmedia.androidplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.principalmedia.androidplayer.entities.Song;

/**
 * Created by Ovidiu on 2/3/2016.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
                MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener,
                MediaPlayer.OnCompletionListener{

    public static final String TAG = MediaPlayerService.class.getSimpleName();

    private final IBinder mBinder = new MediaPlayerBinder();
    private List<MediaPlayerListener> mediaPlayerListeners = new ArrayList<>();

    MediaPlayer mMediaPlayer;
    Song mSong;

    public void play(Song song) throws IOException {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this, song.getUri());
            mMediaPlayer.prepareAsync();
        } else if (song.getUri().getPath().equals(mSong.getUri().getPath())) {
            mMediaPlayer.start();
            startTimeListener();
        } else {
            stopTimeListener();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, song.getUri());
            mMediaPlayer.prepareAsync();
        }

        for (MediaPlayerListener mediaPlayerListener : mediaPlayerListeners) {
            if (mediaPlayerListener != null) {
                Log.e(TAG, "PLAY NOT NULL");
                mediaPlayerListener.onPlay(mSong, song);
            } else {
                Log.e(TAG, "PLAY NULL");
            }
        }

        this.mSong = song;
    }

    public void pause() {
        if (mMediaPlayer != null) {
            stopTimeListener();
            mMediaPlayer.pause();
        } else {
//            Log.e(TAG, "PAUSE NULL");
        }

        for (MediaPlayerListener mediaPlayerListener : mediaPlayerListeners) {
            if (mediaPlayerListener != null) {
                Log.e(TAG, "PAUSE NOT NULL");
                mediaPlayerListener.onPause(mSong);
            } else {
                Log.e(TAG, "PAUSE NULL");
            }
        }
    }

    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        } else {
            Log.e(TAG, "RESET NULL");
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "Media Player Error " + what + " " + extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        startTimeListener();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "Complete");
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public void setMediaPlayerListener(MediaPlayerListener listener) {
        this.mediaPlayerListeners.add(listener);
    }

    public interface MediaPlayerListener {
        void onTimeChanged(int current, int max);
        void onPlay(Song lastSong, Song currentSong);
        void onPause(Song song);
    }

    ScheduledExecutorService myScheduledExecutorService;
    private void startTimeListener() {
        myScheduledExecutorService = Executors.newScheduledThreadPool(1);

        myScheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        int duration = mMediaPlayer.getDuration();
                        int currentPosition = mMediaPlayer.getCurrentPosition();
//                        Log.e(TAG, duration + " " + currentPosition);
                        for (MediaPlayerListener mediaPlayerListener : mediaPlayerListeners) {
                            if (mediaPlayerListener != null) {
//                                Log.e(TAG, "NOT NULL");
                                mediaPlayerListener.onTimeChanged(currentPosition, duration);
                            } else {
//                                Log.e(TAG, "NULL");
                            }
                        }
                    }
                },
                200, //initialDelay
                200, //delay
                TimeUnit.MILLISECONDS);
    }

    private void stopTimeListener() {
        if (myScheduledExecutorService != null) {
            myScheduledExecutorService.shutdownNow();
        }
    }

}
