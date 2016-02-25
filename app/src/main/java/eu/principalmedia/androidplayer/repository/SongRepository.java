package eu.principalmedia.androidplayer.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.principalmedia.androidplayer.entities.Album;
import eu.principalmedia.androidplayer.entities.Artist;
import eu.principalmedia.androidplayer.entities.Song;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class SongRepository /*implements Serializable*/{

    public static final String TAG = SongRepository.class.getSimpleName();

    public static final String GENRE_NAME = "genre_name";

    Map<Integer, Album> albums = new HashMap<>();
    Map<Integer, Artist> artists = new HashMap<>();

    FindMusicAsyncTask findMusicAsyncTask = new FindMusicAsyncTask();
    private boolean findSongsFinish = false;

    public interface OnResultListener<E> {
        void onResult(List<E> list);
    }

    List<Song> mSongList = new ArrayList<>();
    ContentResolver contentResolver;

    public SongRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        findMusicAsyncTask.execute();
    }

    public void findSongs(OnResultListener<Song> onResultListener) {
        if (findSongsFinish) {
            onResultListener.onResult(mSongList);
        } else {
            findMusicAsyncTask.setOnResultListener(onResultListener, Song.class);
        }
    }

    public void findAlbums(final OnResultListener<Album> onResultListener) {
        if (findSongsFinish) {
            onResultListener.onResult(new ArrayList<>(albums.values()));
        } else {
            findMusicAsyncTask.setOnResultListener(onResultListener, Album.class);
        }
    }

    public void findArtists(final OnResultListener<Artist> onResultListener) {
        if (findSongsFinish) {
            onResultListener.onResult(new ArrayList<>(artists.values()));
        } else {
            findMusicAsyncTask.setOnResultListener(onResultListener, Artist.class);
        }
    }

    public Song nextSong(Song song) {
        int index = mSongList.indexOf(song);
        ++index;
        if (index == -1 || index >= mSongList.size()) {
            return mSongList.get(0);
        }
        return mSongList.get(index);
    }

    class FindMusicAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<Pair<OnResultListener, Class>> onResultListeners = new ArrayList<>();

        public void setOnResultListener(OnResultListener resultListener, Class classType) {
            onResultListeners.add(new Pair<>(resultListener, classType));
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            findSongsFinish = true;
            for (Pair<OnResultListener, Class> pair: onResultListeners) {
                if (pair.second == Song.class) {
                    pair.first.onResult(mSongList);
                }
                if (pair.second == Album.class) {
                    pair.first.onResult(new ArrayList<>(albums.values()));
                }
                if (pair.second == Artist.class) {
                    pair.first.onResult(new ArrayList<>(artists.values()));
                }
            }
        }

        @Override
        protected Void doInBackground(Void[] params) {

            Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String order = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
            String[] projections = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                        /*GENRE_NAME,*/ MediaStore.Audio.Media.ARTIST_ID};
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
                    String artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String artistId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
//                    String genre = cursor.getString(cursor.getColumnIndex(GENRE_NAME));

//                    if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)).equals("367")) {
                        for (int i = 0; i < cursor.getColumnCount(); ++i) {
                            Log.e("INDEX", cursor.getColumnName(i) + ":  " + cursor.getString(i));
                        }
//                    }

                    Song song = new Song();
                    song.setTitle(title);
                    song.setPath(path);
                    song.setDisplayName(displayName);
                    song.setAlbumId(albumId);
                    song.setAlbumName(albumName);
                    song.setArtistId(artistId);
                    song.setArtistName(artistName);
//                    song.setGenre(genre);
                    mSongList.add(song);

                    Log.e(TAG, "Album " + albumId);
                    if (!albums.containsKey(Integer.valueOf(albumId))) {
                        Album album = new Album();
                        album.setAlbumId(albumId);
                        album.setAlbumName(albumName);

                        addAlbum(Integer.valueOf(albumId), album);
                    }

                    if (!artists.containsKey(Integer.valueOf(artistId))) {
                        Artist artist = new Artist();
                        artist.setArtistName(artistName);
                        artist.setArtistId(artistId);
                        artists.put(Integer.valueOf(artistId), artist);
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

            album.setAlbumImage(bitmap);
            albums.put(albumId, album);
        } catch (Exception e) {
            album.setAlbumImage(null);
            albums.put(albumId, album);

            e.printStackTrace();
        }
    }

    public Bitmap getAlbumBitmap(int albumId) {
        return albums.get(albumId).getAlbumImage();
    }

}
