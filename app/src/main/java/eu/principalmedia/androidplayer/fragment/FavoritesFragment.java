package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class FavoritesFragment extends Fragment {

    public static final String TAG = FavoritesFragment.class.getSimpleName();
    public static final String FAVORITES_TITLE = "Favorites";

    public static FavoritesFragment newInstance() {
        FavoritesFragment favoritesFragment = new FavoritesFragment();
        return favoritesFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AppCompatActivity) activity).getSupportActionBar().setTitle(FAVORITES_TITLE);
    }

}
