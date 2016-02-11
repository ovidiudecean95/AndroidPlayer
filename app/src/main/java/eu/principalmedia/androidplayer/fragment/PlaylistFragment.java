package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class PlaylistFragment extends Fragment {

    public static final String TAG = GenresFragment.class.getSimpleName();
    public static final String PLAYLIST_TITLE = "Playlist";

    public static PlaylistFragment newInstance() {
        PlaylistFragment playlistFragment = new PlaylistFragment();
        return playlistFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(PLAYLIST_TITLE);
    }

}
