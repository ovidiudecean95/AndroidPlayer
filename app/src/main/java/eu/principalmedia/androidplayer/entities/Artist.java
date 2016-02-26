package eu.principalmedia.androidplayer.entities;

/**
 * Created by Ovidiu on 2/23/2016.
 */
public class Artist {

    private String artistId;
    private String artistName;
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
}
