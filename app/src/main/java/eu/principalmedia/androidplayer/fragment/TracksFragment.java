package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.service.MediaPlayerService;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class TracksFragment extends Fragment implements MediaPlayerService.MediaPlayerListener{

    public static final String TAG = TracksFragment.class.getSimpleName();

    public static final String KEY_REPOSITORY = "key_repository_song";
    public static final String TITLE_TRACKS = "Tracks";

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    SongRepository songRepository;
    List<Song> mSongList = new ArrayList<>();

    OnTrackListener trackListener;

    private Song mSong;
    private boolean isPlaying = false;

    public static TracksFragment newInstance(SongRepository songRepository) {
        TracksFragment tracksFragment = new TracksFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(KEY_REPOSITORY, songRepository);
//        tracksFragment.setArguments(bundle);
        return tracksFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        songRepository = (SongRepository) getArguments().getSerializable(KEY_REPOSITORY);
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

        songRepository.findSongs(new SongRepository.OnResultListener() {
            @Override
            public void onResult(List<Song> songList) {
                mSongList = songList;
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void setRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            trackListener = (OnTrackListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(TITLE_TRACKS);
    }

    @Override
    public void onTimeChanged(int current, int max) {

    }

    @Override
    public void onPlay(Song lastSong, Song currentSong) {
        isPlaying = true;
        mSong = currentSong;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause(Song song) {
        isPlaying = false;
        mAdapter.notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onPlayPauseClick(int position,  boolean play);
    }

    public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

//        private ToggleButton lastToggledButton;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

            public TextView musicTextView;
            public ImageView musicImageView;
            public ToggleButton playPauseToggleButton;
            private OnItemClickListener onItemClickListener;

            public ViewHolder(View itemView, OnItemClickListener onItemClickListener) {
                super(itemView);

                musicTextView = (TextView) itemView.findViewById(R.id.music_title_text_view);
                musicImageView = (ImageView) itemView.findViewById(R.id.music_image_view);
                playPauseToggleButton = (ToggleButton) itemView.findViewById(R.id.play_pause_button);

                this.onItemClickListener = onItemClickListener;
                itemView.setOnClickListener(this);
                playPauseToggleButton.setOnCheckedChangeListener(this);
            }

            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(getPosition());
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //check for user input
                if (buttonView.isPressed()) {
//                    boolean sameButton = buttonView == lastToggledButton || lastToggledButton == null;
//                    Log.e(TAG, "SAME_BUTTON " + sameButton);
                    onItemClickListener.onPlayPauseClick(getPosition(), isChecked);
//                    lastToggledButton = (ToggleButton) buttonView;
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_view_music_item, parent, false);

            return new ViewHolder(view, new OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Snackbar.make(view, position + " ", Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onPlayPauseClick(int position, boolean play) {
//                    Snackbar.make(view, position + " " + play, Snackbar.LENGTH_SHORT).show();
                    Log.e(TAG, position + " " + play);
                    if (play) {
                        Log.e(TAG, "ON PLAY MEDIA PLAYER");
                        trackListener.onPlayMediaPlayer(mSongList.get(position));
                        trackListener.onAddPlayerFragment();
//                        if (!sameButton) {
//                            lastToggledButton.setChecked(false);
//                        }
                    } else {
                        trackListener.onPauseMediaPlayer();
                    }
                }
            });
        }

        @Override
        public void onBindViewHolder(MusicAdapter.ViewHolder holder, int position) {
            Log.e("POS", position + "");
            holder.musicTextView.setText(mSongList.get(position).getTitle());
            if (isPlaying && mSong != null && mSong == mSongList.get(position)) {
                holder.playPauseToggleButton.setChecked(true);
            } else {
                holder.playPauseToggleButton.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return mSongList.size();
        }
    }
}
