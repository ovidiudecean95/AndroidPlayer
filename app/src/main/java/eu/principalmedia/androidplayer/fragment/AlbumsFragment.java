package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class AlbumsFragment extends Fragment {

    public static final String TAG = AlbumsFragment.class.getSimpleName();
    public static final String ALBUM_TITLE = "Albums";

    public static AlbumsFragment newInstance() {
        AlbumsFragment albumsFragment = new AlbumsFragment();
        return albumsFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(ALBUM_TITLE);
    }
}
