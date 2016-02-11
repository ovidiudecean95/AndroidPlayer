package eu.principalmedia.androidplayer.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.principalmedia.androidplayer.entities.Song;

/**
 * Created by Ovidiu on 2/5/2016.
 */
public class SongRepository /*implements Serializable*/{

    public interface OnResultListener {
        void onResult(List<Song> songList);
    }

    List<Song> mSongList = new ArrayList<>();
    ContentResolver contentResolver;

    public SongRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void findSongs(OnResultListener onResultListener) {
        if (mSongList.size() != 0) {
            onResultListener.onResult(mSongList);
        } else {
            new FindMusicAsyncTask(onResultListener).execute();
        }
    }

    class FindMusicAsyncTask extends AsyncTask<Void, Void, Void> {

        OnResultListener onResultListener;

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
            Cursor cursor = contentResolver.query(mediaUri, null, selection, null, order);

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
//                    if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)).equals("367")) {
//                        for (int i = 0; i < cursor.getColumnCount(); ++i) {
//                            Log.e("INDEX", cursor.getColumnName(i) + ":  " + cursor.getString(i));
//                        }
//                    }
                    Song song = new Song();
                    song.setTitle(title);
                    song.setPath(path);
                    song.setDisplayName(displayName);
                    mSongList.add(song);
                } while (cursor.moveToNext());

                cursor.close();
            }
            return null;
        }
    }

}
