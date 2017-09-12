package com.santoshkumarsingh.gxmusicplayer.Models;

/**
 * Created by santoshsingh on 17/08/17.
 */

public class Audio {
    private String TITLE, ARTIST, URL, ALBUM;

    public Audio() {
    }

    public Audio(String TITLE, String ARTIST, String URL, String ALBUM) {
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
    }

    public String getTITLE() {
        return TITLE;
    }

    public String getARTIST() {
        return ARTIST;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getALBUM() {
        return ALBUM;
    }
}
