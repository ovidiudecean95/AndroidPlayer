package eu.principalmedia.androidplayer.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.principalmedia.androidplayer.entities.Album;
import eu.principalmedia.androidplayer.entities.Song;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class SongRepository /*implements Serializable*/{

    public static final String TAG = SongRepository.class.getSimpleName();

    public static final String GENRE_NAME = "genre_name";

    Map<Integer, Bitmap> albumsBitmaps = new HashMap<>();
    Map<Integer, Album> albums = new HashMap<>();

    public interface OnResultListener<E> {
        void onResult(List<E> songList);
    }

    List<Song> mSongList = new ArrayList<>();
    ContentResolver contentResolver;

    public SongRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void findSongs(OnResultListener<Song> onResultListener) {
        if (mSongList.size() != 0) {
            onResultListener.onResult(mSongList);
        } else {
            new FindMusicAsyncTask(onResultListener).execute();
        }
    }

    public void findAlbums(final OnResultListener<Album> onResultListener) {
        if (albums.size() != 0) {
            onResultListener.onResult(new ArrayList<>(albums.values()));
        } else {
            new FindMusicAsyncTask(onResultListener) {
                @Override
                protected void onPreExecute() {
                    onResultListener.onResult(new ArrayList<>(albums.values()));
                }
            }.
            execute();
        }
    }


    public Song nextSong(Song song) {
        int index = mSongList.indexOf(song);
        ++index;
        if (index < mSongList.size()) {
            return mSongList.get(index);
        }
        return mSongList.get(0);
    }

    class FindMusicAsyncTask extends AsyncTask<Void, Void, Void> {

        private OnResultListener onResultListener;

        public FindMusicAsyncTask(OnResultListener onResultListener) {
            this.onResultListener = onResultListener;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onResultListener.onResult(mSongList);
        }

        @Override
        protected Void doInBackground(Void[] params) {

            Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String order = MediaStore.Audio.Media.TITLE + " ASC";
            String[] projections = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                        GENRE_NAME};
            Cursor cursor = contentResolver.query(mediaUri, projections, selection, null, order);

            if (cursor == null) {
                // query failed, handle error.
            } else if (!cursor.moveToFirst()) {
                // no media on the device
            } else {
                int count = cursor.getCount();
                Log.e("COUNT", count + "");

                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String path  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String genre = cursor.getString(cursor.getColumnIndex(GENRE_NAME));

//                    if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)).equals("367")) {
//                        for (int i = 0; i < cursor.getColumnCount(); ++i) {
//                            Log.e("INDEX", cursor.getColumnName(i) + ":  " + cursor.getString(i));
//                        }
//                    }

                    Song song = new Song();
                    song.setTitle(title);
                    song.setPath(path);
                    song.setDisplayName(displayName);
                    song.setAlbumId(albumId);
                    song.setAlbumName(albumName);
                    song.setArtist(artist);
                    song.setGenre(genre);
                    mSongList.add(song);

                    Log.e(TAG, "Album " + albumId);
                    if (!albumsBitmaps.containsKey(Integer.valueOf(albumId))) {
                        Album album = new Album();
                        album.setAlbumId(albumId);
                        album.setAlbumName(albumName);

                        addAlbum(Integer.valueOf(albumId), album);
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }
            return null;
        }
    }

    private void addAlbum(int albumId, Album album) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver, albumArtUri);
            albumsBitmaps.put(albumId, bitmap);

            album.setAlbumImage(bitmap);
            albums.put(albumId, album);
        } catch (Exception e) {
            album.setAlbumImage(null);
            albums.put(albumId, album);

            e.printStackTrace();
        }
    }

    public Bitmap getAlbumBitmap(int albumId) {
        return albumsBitmaps.get(albumId);
    }

}
