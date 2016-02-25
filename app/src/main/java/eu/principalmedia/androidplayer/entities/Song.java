package eu.principalmedia.androidplayer.entities;

import android.net.Uri;

import java.io.File;

/**
 * Created by Ovidiu on 2/2/2016.
 */
public class Song {

    private String title;
    private String path;
    private String displayName;
    private String albumId;
    private String albumName;
    private Uri uri;
    private String artistId;
    private String artistName;
    private String genre;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Uri getUri() {
        return uri;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
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
