package eu.principalmedia.androidplayer.interfaces;

import eu.principalmedia.androidplayer.entities.Song;

/**
 * Created by Ovidiu on 2/11/2016.
 */
public interface OnTrackListener {
    void onPlayMediaPlayer(Song song);
    void onPauseMediaPlayer();
    void onAddPlayerFragment();
}