package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.concurrent.TimeUnit;

import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.service.MediaPlayerService;
import eu.principalmedia.androidplayer.utils.TimeUtils;

/**
 * Created  by Ovidiu on 2/5/2016.
 */
public class PlayerFragment extends Fragment implements MediaPlayerService.MediaPlayerListener{

    public interface PlayerListener {
        void onViewCreatedPlayerFragment();
    }

    OnTrackListener trackListener;

    private PlayerListener playerListener;
    private TextView timeTextView;
    private TextView titleTextView;
    private SeekBar progressSeekBar;
    private ToggleButton playPauseButton;
    private ImageView songImageView;

    private SongRepository songRepository;
    private Song currentSong;
    boolean isPlaying;

    public static final String TAG = PlayerFragment.class.getSimpleName();

    public static PlayerFragment newInstance() {
        PlayerFragment playerFragment = new PlayerFragment();
        return playerFragment;
    }

    public void setSongRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public void setSong(Song song) {
        this.currentSong = song;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        timeTextView = (TextView) view.findViewById(R.id.time_text_view);
        progressSeekBar = (SeekBar) view.findViewById(R.id.playback_seekbar);
        titleTextView = (TextView) view.findViewById(R.id.title_text_view);
        playPauseButton = (ToggleButton) view.findViewById(R.id.play_pause_player_button);
        songImageView = (ImageView) view.findViewById(R.id.song_image_view);

        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e(TAG, "PROGRESS " + progress);
                trackListener.onSeekChanged(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        playPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(TAG, "Button on check changed" + playPauseButton.isChecked());
                Log.e(TAG, "isPressed " + buttonView.isPressed());
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        Log.e(TAG, "ON PLAY MEDIA PLAYER");
                        trackListener.onPlayMediaPlayer(currentSong);
                    } else {
                        trackListener.onPauseMediaPlayer();
                    }
                }
            }
        });

        Log.e(TAG, "ischecked " + playPauseButton.isChecked());
        return view;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onviewcreated");
        Log.e(TAG, "Button " + playPauseButton.isChecked());
        playerListener.onViewCreatedPlayerFragment();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "Button " + playPauseButton.isChecked());

        if (isPlaying) {
            Log.e(TAG, "isPlaying");
            playPauseButton.setChecked(true);
        }
        Log.e(TAG, "ischecked " + playPauseButton.isChecked());
        if (currentSong != null) {
            titleTextView.setText(currentSong.getDisplayName());
            Bitmap bitmap = songRepository.getAlbumBitmap(Integer.valueOf(currentSong.getAlbumId()));
            if (bitmap != null) {
                songImageView.setImageBitmap(bitmap);
            } else {
                songImageView.setImageResource(R.drawable.no_image_black);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            playerListener = (PlayerListener) activity;
            trackListener = (OnTrackListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onTimeChanged(final int current, final int duration) {
        final String currentText = TimeUtils.millisToTimeString(current);
        final String durationText = TimeUtils.millisToTimeString(duration);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeTextView.setText(String.format("%s/%s", currentText, durationText));
                progressSeekBar.setMax(duration);
                progressSeekBar.setProgress(current);
            }
        });
    }

    @Override
    public void onPlay(Song lastSong, Song currentSong) {
        Log.e(TAG, "isPlay");
        this.currentSong = currentSong;
        isPlaying = true;
        if (titleTextView != null) {
            titleTextView.setText(currentSong.getDisplayName());
            Bitmap bitmap = songRepository.getAlbumBitmap(Integer.valueOf(currentSong.getAlbumId()));
            if (bitmap != null) {
                songImageView.setImageBitmap(bitmap);
            } else {
                songImageView.setImageResource(R.drawable.no_image_black);
            }
        }
        if (playPauseButton != null) {
            playPauseButton.setChecked(true);
        }
    }

    @Override
    public void onPause(Song song) {
        Log.e(TAG, "isPause");
        isPlaying = false;
        if (playPauseButton != null) {
            playPauseButton.setChecked(false);
        }
    }
}
