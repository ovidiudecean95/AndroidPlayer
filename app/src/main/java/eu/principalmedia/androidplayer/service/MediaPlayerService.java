package eu.principalmedia.androidplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.principalmedia.androidplayer.NotificationPlayback;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.activity.MainActivity;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.repository.SongRepository;

/**
 * Created by Ovidiu on 2/3/2016.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
                MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener,
                MediaPlayer.OnCompletionListener{

    public static final String TAG = MediaPlayerService.class.getSimpleName();

    private final IBinder mBinder = new MediaPlayerBinder();
    private List<MediaPlayerListener> mediaPlayerListeners = new ArrayList<>();

    private MediaPlayer mMediaPlayer;
    private Song mSong;
    private boolean isPlaying = false;
    NotificationPlayback notificationBuilder;
    private SongRepository mSongRepository;

    @Override
    public void onCreate() {
        Log.e(TAG, "OnCreate");
        super.onCreate();
        notificationBuilder = new NotificationPlayback(this);
        notificationBuilder.setSmallIcon(R.drawable.no_image);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationPlayback.ACTION_CANCEL_NOTIFICATION);
        filter.addAction(NotificationPlayback.ACTION_PLAY_PAUSE_NOTIFICATION);
        filter.addAction(NotificationPlayback.ACTION_PLAY_NEXT_NOTIFICATION);
        registerReceiver(notificationReceiver, filter);
    }

    public void setSongRepository(SongRepository songRepository) {
        this.mSongRepository = songRepository;
        notificationBuilder.setSongRepository(songRepository);
    }

    public SongRepository getSongRepository() {
        return mSongRepository;
    }

    public void play(Song song) throws IOException {
        isPlaying = true;
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

    public void nextSong() throws IOException {
        play(mSongRepository.nextSong(mSong));
    }

    public void setProgress(int progress) {
        stopTimeListener();
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(progress);
        mMediaPlayer.start();
        startTimeListener();
    }

    public void pause() {
        isPlaying = false;
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

    public Song getSong() {
        return mSong;
    }

    public boolean isPlaying() {
        return isPlaying;
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
        stopTimeListener();
        try {
            nextSong();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: handle exception
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "OnDestroy");
        unregisterReceiver(notificationReceiver);
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
        if (!mediaPlayerListeners.contains(listener)) {
            mediaPlayerListeners.add(listener);
        }
    }

    public void removeMediaPlayerListener(MediaPlayerListener listener) {
        mediaPlayerListeners.remove(listener);
    }

    public interface MediaPlayerListener {
        boolean onTimeChanged(int current, int max);
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
                        for (Iterator<MediaPlayerListener> iterator = mediaPlayerListeners.iterator(); iterator.hasNext();) {
                            MediaPlayerListener mediaPlayerListener = iterator.next();
                            if (mediaPlayerListener != null) {
//                                Log.e(TAG, "NOT NULL");
//                                Log.e(TAG, mediaPlayerListener.onTimeChanged(currentPosition, duration) + "");
                                if (!mediaPlayerListener.onTimeChanged(currentPosition, duration)) {
                                    iterator.remove();
                                }

                            } else {
                                Log.e(TAG, "NULL");
                            }
                        }
                    }
                },
                0, //initialDelay
                500, //delay
                TimeUnit.MILLISECONDS);
    }

    private void stopTimeListener() {
        if (myScheduledExecutorService != null) {
            myScheduledExecutorService.shutdownNow();
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case NotificationPlayback.ACTION_CANCEL_NOTIFICATION:
                    pause();
                    stopForeground(true);
                    break;
                case NotificationPlayback.ACTION_PLAY_PAUSE_NOTIFICATION:
                    if (isPlaying) {
                        pause();
                    } else {
                        try {
                            play(mSong);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case NotificationPlayback.ACTION_PLAY_NEXT_NOTIFICATION:
                    try {
                        nextSong();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

}
