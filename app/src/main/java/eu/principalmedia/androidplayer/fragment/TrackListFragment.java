package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.principalmedia.androidplayer.FragmentUtils;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.activity.MainActivity;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.service.MediaPlayerService;

/**
 * Created by Ovidiu on 2/23/2016.
 */
public class TrackListFragment extends Fragment implements MediaPlayerService.MediaPlayerListener{

    public static final String TAG = TrackListFragment.class.getSimpleName();
    public static final String TITLE_TRACKS = "Tracks";

    public static final String TRACKS = "tracks";
    public static final String ALBUMS = "albums";
    public static final String ARTISTS = "artists";
    public static final String GENRES = "genres";

    public String type;
    private String entityId;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    SongRepository songRepository;
    List<Song> mSongList = new ArrayList<>();

    OnTrackListener trackListener;
    MediaPlayerService mMediaPlayerService;

    public static final int INDEFINITE = -1;

    private Song mSong;
    private boolean isPlaying = false;
    private int screenWidth = INDEFINITE;
    private int progress = INDEFINITE;
    private int duration = 0;
    private int currentPosition = 0;

    public TrackListFragment(String type, String id) {
        this.type = type;
        this.entityId = id;
    }

    public TrackListFragment() {
        super();
    }

    public static TrackListFragment newInstance(String type) {
        TrackListFragment trackListFragment = new TrackListFragment(type, null);
        return trackListFragment;
    }

    public static TrackListFragment newInstance(String type, String id) {
        TrackListFragment trackListFragment = new TrackListFragment(type, id);
        return trackListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        songRepository = (SongRepository) getArguments().getSerializable(KEY_REPOSITORY);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.sDisableFragmentAnimations) {
//            return null;

            Animation animation = new Animation() {};
            animation.setDuration(0);
            return animation;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.music_recycler_view);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MusicAdapter();
        mRecyclerView.setAdapter(mAdapter);


        songRepository.findSongs(new SongRepository.OnResultListener<Song>() {
            @Override
            public void onResult(List<Song> songList) {
                switch (type) {
                    case TRACKS:
                        mSongList = songList;
                        break;
                    case ALBUMS:
                        for (Song song : songList) {
                            if (song.getAlbumId().equals(entityId)) {
                                mSongList.add(song);
                            }
                        }
                        break;
                    case ARTISTS:
                        for (Song song : songList) {
                            if (song.getArtistId().equals(entityId)) {
                                mSongList.add(song);
                            }
                        }
                        break;
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    public void setRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public void setService(MediaPlayerService mediaPlayerService) {
        this.mMediaPlayerService = mediaPlayerService;
        this.mMediaPlayerService.setMediaPlayerListener(this);
        syncWithService();
    }

    @Override
    public void onDetach() {
        mMediaPlayerService.removeMediaPlayerListener(this);
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            trackListener = (OnTrackListener) activity;
            mMediaPlayerService =  ((MainActivity) activity).mMediaPlayerService;
            if (mMediaPlayerService != null) {
                mMediaPlayerService.setMediaPlayerListener(this);
                syncWithService();
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void syncWithService() {
        mSong = mMediaPlayerService.getSong();
        isPlaying = mMediaPlayerService.isPlaying();
        currentPosition = mMediaPlayerService.getCurrentPosition();
        duration =  mMediaPlayerService.getSongDuration();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (type.equals(TRACKS)) {
//            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(TITLE_TRACKS);
            ((TextView) (((MainActivity) getActivity()).mToolbar.findViewById(R.id.title_toolbar))).setText(TITLE_TRACKS);
        }
    }

    @Override
    public boolean onTimeChanged(int current, int max) {
        Log.e(TAG, TAG);

        currentPosition = current;
        duration = max;

        if (getActivity() == null) {
            return false;
        }

        final FrameLayout progressFrameLayout = ((MusicAdapter) mAdapter).getProgress();

        if (progressFrameLayout != null) {
            if (screenWidth == INDEFINITE) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
            }

            int progress = (int) ((float) current / max * screenWidth);
            this.progress = progress;
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    progress, ViewGroup.LayoutParams.MATCH_PARENT);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressFrameLayout.setLayoutParams(layoutParams);
                }
            });
        }
        return true;
    }

    @Override
    public void onPlay(Song lastSong, Song currentSong) {
        if (currentSong != mSong) {
            progress = INDEFINITE;
        }
        isPlaying = true;
        mSong = currentSong;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause(Song song) {
        isPlaying = false;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setFirstSong(Song song) {

    }

    public interface OnItemClickListener {
        void onItemClick(int position, boolean play);
        void onPlayPauseClick(int position,  boolean play);
    }

    public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

//        private ToggleButton lastToggledButton;

        FrameLayout frameLayout;

        public FrameLayout getProgress() {
            return frameLayout;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

            public TextView musicTextView;
            public CircleImageView musicImageView;
            public ToggleButton playPauseToggleButton;
            private OnItemClickListener onItemClickListener;
            private FrameLayout progressFrameLayout;

            public ViewHolder(View itemView, OnItemClickListener onItemClickListener) {
                super(itemView);

                musicTextView = (TextView) itemView.findViewById(R.id.music_title_text_view);
                musicImageView = (CircleImageView) itemView.findViewById(R.id.music_image_view);
                playPauseToggleButton = (ToggleButton) itemView.findViewById(R.id.play_pause_button);
                progressFrameLayout = (FrameLayout) itemView.findViewById(R.id.progress_frame_layout);

                this.onItemClickListener = onItemClickListener;
                itemView.setOnClickListener(this);
                playPauseToggleButton.setOnCheckedChangeListener(this);
            }

            @Override
            public void onClick(View v) {
                boolean play = !playPauseToggleButton.isChecked();
                onItemClickListener.onPlayPauseClick(getPosition(), play);
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
                public void onItemClick(int position, boolean play) {
//                    Snackbar.make(view, position + " ", Snackbar.LENGTH_SHORT).show();
                    if (play) {
                        trackListener.onPlayMediaPlayer(mSongList.get(position));
                        trackListener.onAddPlayerFragment();
//                        ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(position, 0);
//                        notifyDataSetChanged();
//                        if (!sameButton) {
//                            lastToggledButton.setChecked(false);
//                        }
                    } else {
                        trackListener.onAddRemovePlayerFragment();
                    }
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
            holder.musicTextView.setText(mSongList.get(position).getDisplayName());
            if (isPlaying && mSong != null && mSong == mSongList.get(position)) {
                holder.playPauseToggleButton.setChecked(true);
            } else {
                holder.playPauseToggleButton.setChecked(false);
            }

            if (mSong != null && mSongList.get(position) == mSong) {
                if (progress != INDEFINITE) {
                    holder.progressFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                            progress, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                holder.progressFrameLayout.setVisibility(View.VISIBLE);
                frameLayout = holder.progressFrameLayout;
                onTimeChanged(currentPosition, duration);
            } else {
                holder.progressFrameLayout.setVisibility(View.INVISIBLE);
            }

            Bitmap albumBitmap = songRepository.getAlbumBitmap(Integer.valueOf(mSongList.get(position).getAlbumId()));
            if (albumBitmap != null) {
                holder.musicImageView.setImageBitmap(albumBitmap);
            } else {
                holder.musicImageView.setImageResource(R.drawable.no_image);
            }
        }

        @Override
        public int getItemCount() {
            return mSongList.size();
        }
    }
}

