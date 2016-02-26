package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.principalmedia.androidplayer.FragmentUtils;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.activity.MainActivity;
import eu.principalmedia.androidplayer.entities.Album;
import eu.principalmedia.androidplayer.entities.Artist;
import eu.principalmedia.androidplayer.repository.SongRepository;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class ArtistsFragment extends Fragment {

    public static final String TAG = ArtistsFragment.class.getSimpleName();
    public static final String ARTISTS_TITLE = "Artists";

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    SongRepository songRepository;
    List<Artist> artistsList = new ArrayList<>();

    ArtistsListener artistsListener;

    public interface ArtistsListener {
        void onArtistClick(Artist artist);
    }

    public static ArtistsFragment newInstance() {
        ArtistsFragment artistsFragment = new ArtistsFragment();
        return artistsFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        ((AppCompatActivity) activity).getSupportActionBar().setTitle(ARTISTS_TITLE);
        ((TextView) (((MainActivity) getActivity()).mToolbar.findViewById(R.id.title_toolbar))).setText(ARTISTS_TITLE);
        artistsListener = (ArtistsListener) activity;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.sDisableFragmentAnimations) {
//            return super.onCreateAnimation(0, enter, 0);

            Animation animation = new Animation() {};
            animation.setDuration(0);
            return animation;
        }
        return super.onCreateAnimation(transit, enter, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.music_recycler_view);
//        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ArtistsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        Log.e(TAG, "SongRepo " + (songRepository != null));

        songRepository.findArtists(new SongRepository.OnResultListener<Artist>() {
            @Override
            public void onResult(List<Artist> artistList) {
                artistsList = artistList;
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void setRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    interface OnClickListener {
        void onClick(int position);
    }

    class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView artistTitleTextView;
            TextView artistNumberSongs;

            public ViewHolder(View itemView, final OnClickListener listener) {
                super(itemView);
                artistTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
                artistNumberSongs = (TextView) itemView.findViewById(R.id.number_tracks_text_view);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(getPosition());
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_artists_item,
                    parent, false);

            return new ViewHolder(view, new OnClickListener() {
                @Override
                public void onClick(int position) {
                    artistsListener.onArtistClick(artistsList.get(position));
                }
            });
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.artistTitleTextView.setText(artistsList.get(position).getArtistName());
            holder.artistNumberSongs.setText(String.format("%d Tracks", artistsList.get(position).getNumberOfSongs()));
        }

        @Override
        public int getItemCount() {
            return artistsList.size();
        }
    }

}
