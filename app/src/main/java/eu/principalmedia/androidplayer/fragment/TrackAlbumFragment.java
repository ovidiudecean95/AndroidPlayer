package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.entities.Album;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.repository.SongRepository;

/**
 * Created by Ovidiu on 2/19/2016.
 */
public class TrackAlbumFragment extends TrackListFragment {

    private Album mAlbum;

    public TrackAlbumFragment() {
        super();
    }

    public static TrackAlbumFragment newInstance() {

        TrackAlbumFragment trackAlbumFragment = new TrackAlbumFragment();
        return trackAlbumFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.music_recycler_view);
        mRecyclerView.hasFixedSize();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MusicAdapter();
        mRecyclerView.setAdapter(mAdapter);

        songRepository.findSongs(new SongRepository.OnResultListener<Song>() {
            @Override
            public void onResult(List<Song> songList) {
                for (Song song : songList) {
                    if (song.getAlbumId().equals(mAlbum.getAlbumId())) {
                        mSongList.add(song);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
