package eu.principalmedia.androidplayer.entities;

import android.net.Uri;

import java.io.File;

/**
 * Created by Ovidiu on 2/2/2016.
 */
public class Song {

    private String title;
    private String artist;
    private String path;
    private String displayName;
    private Uri uri;

    public Uri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String mTitle) {
        this.title = mTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String mArtist) {
        this.artist = mArtist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String mPath) {
        this.path = mPath;
        this.uri = Uri.fromFile(new File(mPath));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Song) {
            return this.getTitle().equals(((Song) o).getTitle());
        } else {
            return super.equals(o);
        }
    }
}