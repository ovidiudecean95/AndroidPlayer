package eu.principalmedia.androidplayer.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.repository.SongRepository;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class AlbumsFragment extends Fragment {

    public static final String TAG = AlbumsFragment.class.getSimpleName();
    public static final String ALBUM_TITLE = "Albums";

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    SongRepository songRepository;
    List<Album> albumsList = new ArrayList<>();

    AlbumListener albumListener;

    public interface AlbumListener {
        void onAlbumClick(Album album);
    }

    public static AlbumsFragment newInstance() {
        AlbumsFragment albumsFragment = new AlbumsFragment();
        return albumsFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((TextView) (((MainActivity) getActivity()).mToolbar.findViewById(R.id.title_toolbar))).setText(ALBUM_TITLE);
        albumListener = (AlbumListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.album_recycler_view);
//        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.grid_view_offset);
        mRecyclerView.addItemDecoration(itemDecoration);

        mAdapter = new AlbumsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        Log.e(TAG, "SongRepo " + (songRepository != null));

        songRepository.findAlbums(new SongRepository.OnResultListener<Album>() {
            @Override
            public void onResult(List<Album> albumList) {
                albumsList = albumList;
                mAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.sDisableFragmentAnimations) {
//            return super.onCreateAnimation(0, enter, 0);

            Animation animation = new Animation() {};
            animation.setDuration(0);
            return animation;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    public void setRepository(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    interface OnClickListener {
        void onClick(int position);
    }

    class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView albumImageView;
            TextView albumTitleTextView;

            public ViewHolder(View itemView, final OnClickListener listener) {
                super(itemView);
                albumImageView = (ImageView) itemView.findViewById(R.id.album_image_view);
                albumTitleTextView = (TextView) itemView.findViewById(R.id.album_title_text_view);

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
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item,
                                parent, false);

            return new ViewHolder(view, new OnClickListener() {
                @Override
                public void onClick(int position) {
                    albumListener.onAlbumClick(albumsList.get(position));
                }
            });
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Bitmap albumBitmap = albumsList.get(position).getAlbumImage();
            if (albumBitmap != null) {
                holder.albumImageView.setImageBitmap(albumBitmap);
            } else {
                holder.albumImageView.setImageResource(R.drawable.no_image);
            }

            holder.albumTitleTextView.setText(albumsList.get(position).getAlbumName());
        }

        @Override
        public int getItemCount() {
            return albumsList.size();
        }
    }

}
