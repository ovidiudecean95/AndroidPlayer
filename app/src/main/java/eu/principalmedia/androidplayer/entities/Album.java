package eu.principalmedia.androidplayer.entities;

import android.graphics.Bitmap;

/**
 * Created by Ovidiu on 2/19/2016.
 */
public class Album {

    private String albumId;
    private String albumName;
    private Bitmap albumImage;
    private int numberOfSongs = 1;

    public void incNumberSongs() {
        ++ numberOfSongs;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(Bitmap albumImage) {
        this.albumImage = albumImage;
    }
}
