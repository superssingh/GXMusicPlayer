package com.santoshkumarsingh.gxmusicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String TITLE, ARTIST, URL, ALBUM, DURATION;

    public Audio() {
    }

    public Audio(String TITLE, String ARTIST, String URL, String ALBUM, String DURATION) {
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
    }

    protected Audio(Parcel in) {
        TITLE = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(TITLE);
        parcel.writeString(ARTIST);
        parcel.writeString(URL);
        parcel.writeString(ALBUM);
        parcel.writeString(DURATION);
    }
}
