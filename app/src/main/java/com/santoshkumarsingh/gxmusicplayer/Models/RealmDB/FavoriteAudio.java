package com.santoshkumarsingh.gxmusicplayer.Models.RealmDB;

import io.realm.RealmObject;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class FavoriteAudio extends RealmObject {
    private String TITLE, ARTIST, URL, ALBUM, DURATION;

    public FavoriteAudio() {
    }

    public FavoriteAudio(String TITLE, String ARTIST, String URL, String ALBUM, String DURATION) {
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
    }

    public String getTITLE() {
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public String getARTIST() {
        return ARTIST;
    }

    public void setARTIST(String ARTIST) {
        this.ARTIST = ARTIST;
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

    public void setALBUM(String ALBUM) {
        this.ALBUM = ALBUM;
    }

    public String getDURATION() {
        return DURATION;
    }

    public void setDURATION(String DURATION) {
        this.DURATION = DURATION;
    }
}
