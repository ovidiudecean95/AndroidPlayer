package eu.principalmedia.androidplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.service.MediaPlayerService;

/**
 * Created by Ovidiu on 2/15/2016.
 */
public class NotificationPlayback extends NotificationCompat.Builder implements MediaPlayerService.MediaPlayerListener{

    public static final String TAG = NotificationPlayback.class.getSimpleName();

    public static final String ACTION_CANCEL_NOTIFICATION = "cancel_notification";
    public static final String ACTION_PLAY_PAUSE_NOTIFICATION = "play_pause_notification";
    public static final String ACTION_PLAY_NEXT_NOTIFICATION = "play_next_notification";

    public static final int NOTIFICATION_ID = 1;

    SongRepository songRepository;
    MediaPlayerService mMediaPlayerService;
    Context mContext;

    public NotificationPlayback(Context context) {
        super(context);
        this.mContext = context;
        Intent intent = new Intent(context, MediaPlayerService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setSongRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaPlayerService = ((MediaPlayerService.MediaPlayerBinder) service).getService();
            mMediaPlayerService.setMediaPlayerListener(NotificationPlayback.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onTimeChanged(int current, int max) {
        Log.e(TAG, TAG);
        return true;
    }

    @Override
    public void onPlay(Song lastSong, Song currentSong) {
        Log.e(TAG, "onPlay");
        updateNotification(currentSong);
    }

    @Override
    public void onPause(Song song) {
        updateNotification(song);
    }

    private void updateNotification(Song song) {
        Notification notification = this.build();

        RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);

        contentView.setTextViewText(R.id.title_text_view, song.getDisplayName());
        int resourceId = mMediaPlayerService.isPlaying() ? R.drawable.pause_notification : R.drawable.play_notification;
        contentView.setImageViewResource(R.id.play_pause_button_iv, resourceId);

        Bitmap albumImage = songRepository.getAlbumBitmap(Integer.valueOf(song.getAlbumId()));
        if (albumImage != null) {
            contentView.setImageViewBitmap(R.id.album_image_view, albumImage);
        } else {
            contentView.setImageViewResource(R.id.album_image_view, R.drawable.no_image);
        }

        Intent cancelNotification = new Intent(NotificationPlayback.ACTION_CANCEL_NOTIFICATION);
        contentView.setOnClickPendingIntent(R.id.close_button, PendingIntent.getBroadcast(mContext, 0, cancelNotification, 0));

        Intent playPuaseNotification = new Intent(NotificationPlayback.ACTION_PLAY_PAUSE_NOTIFICATION);
        contentView.setOnClickPendingIntent(R.id.play_pause_button_iv, PendingIntent.getBroadcast(mContext, 0, playPuaseNotification, 0));

        Intent playNextNotification = new Intent(NotificationPlayback.ACTION_PLAY_NEXT_NOTIFICATION);
        contentView.setOnClickPendingIntent(R.id.next_button_iv, PendingIntent.getBroadcast(mContext, 0, playNextNotification, 0));

        notification.contentView = contentView;

        mMediaPlayerService.startForeground(NotificationPlayback.NOTIFICATION_ID, notification);
    }
}
