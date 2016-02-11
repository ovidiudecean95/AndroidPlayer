package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class ArtistsFragment extends Fragment {

    public static final String TAG = ArtistsFragment.class.getSimpleName();
    public static final String ARTISTS_TITLE = "Artists";

    public static ArtistsFragment newInstance() {
        ArtistsFragment artistsFragment = new ArtistsFragment();
        return artistsFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(ARTISTS_TITLE);
    }

}
