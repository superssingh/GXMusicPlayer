package com.santoshkumarsingh.gxmusicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by santoshsingh on 08/10/17.
 */

public class Albums implements Parcelable {
    public static final Creator<Albums> CREATOR = new Creator<Albums>() {
        @Override
        public Albums createFromParcel(Parcel in) {
            return new Albums(in);
        }

        @Override
        public Albums[] newArray(int size) {
            return new Albums[size];
        }
    };
    private String ID, ALBUM, ARTIST, URL, ALBUM_ART, DURATION, GENRES;

    public Albums() {
    }

    public Albums(String ID, String ALBUM, String ARTIST, String URL, String ALBUM_ART, String DURATION, String GENRES) {
        this.ID = ID;
        this.ALBUM = ALBUM;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM_ART = ALBUM_ART;
        this.DURATION = DURATION;
        this.GENRES = GENRES;
    }

    protected Albums(Parcel in) {
        ID = in.readString();
        ALBUM = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM_ART = in.readString();
        DURATION = in.readString();
        GENRES = in.readString();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getALBUM() {
        return ALBUM;
    }

    public void setALBUM(String ALBUM) {
        this.ALBUM = ALBUM;
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

    public String getALBUM_ART() {
        return ALBUM_ART;
    }

    public void setALBUM_ART(String ALBUM_ART) {
        this.ALBUM_ART = ALBUM_ART;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(ALBUM);
        dest.writeString(ARTIST);
        dest.writeString(URL);
        dest.writeString(ALBUM_ART);
        dest.writeString(DURATION);
        dest.writeString(GENRES);
    }
}
