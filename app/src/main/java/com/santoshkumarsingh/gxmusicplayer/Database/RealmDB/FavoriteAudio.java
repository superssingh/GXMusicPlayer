package com.santoshkumarsingh.gxmusicplayer.Database.RealmDB;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class FavoriteAudio extends RealmObject {

    @PrimaryKey
    private String id;
    private String songTITLE;
    private String songARTIST;
    private String songURL;
    private String songALBUM;
    private String songDURATION;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSongTITLE() {
        return songTITLE;
    }

    public void setSongTITLE(String songTITLE) {
        this.songTITLE = songTITLE;
    }

    public String getSongARTIST() {
        return songARTIST;
    }

    public void setSongARTIST(String songARTIST) {
        this.songARTIST = songARTIST;
    }

    public String getSongURL() {
        return songURL;
    }

    public void setSongURL(String songURL) {
        this.songURL = songURL;
    }

    public String getSongALBUM() {
        return songALBUM;
    }

    public void setSongALBUM(String songALBUM) {
        this.songALBUM = songALBUM;
    }

    public String getSongDURATION() {
        return songDURATION;
    }

    public void setSongDURATION(String songDURATION) {
        this.songDURATION = songDURATION;
    }
}
