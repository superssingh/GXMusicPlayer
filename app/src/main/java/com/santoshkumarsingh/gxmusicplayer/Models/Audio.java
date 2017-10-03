package com.santoshkumarsingh.gxmusicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class Audio implements Parcelable {

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };
    private String ID, TITLE, ARTIST, URL, ALBUM, DURATION, GENRES;

    public Audio() {
    }

    public Audio(FavoriteAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
        this.GENRES = audios.getGENRES();
    }


    public Audio(String ID, String TITLE, String ARTIST, String URL, String ALBUM, String DURATION, String GENRES) {
        this.ID = ID;
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
        this.GENRES = GENRES;
    }

    protected Audio(Parcel in) {
        ID = in.readString();
        TITLE = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
        GENRES = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(TITLE);
        dest.writeString(ARTIST);
        dest.writeString(URL);
        dest.writeString(ALBUM);
        dest.writeString(DURATION);
        dest.writeString(GENRES);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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

    public String getGENRES() {
        return GENRES;
    }

    public void setGENRES(String GENRES) {
        this.GENRES = GENRES;
    }
}
