package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class GenresFragment extends Fragment {

    public static final String TAG = GenresFragment.class.getSimpleName();
    public static final String GENRES_FRAGMENT = "Genres";

    public static GenresFragment newInstance() {
        GenresFragment genresFragment = new GenresFragment();
        return genresFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(GENRES_FRAGMENT);
    }

}
