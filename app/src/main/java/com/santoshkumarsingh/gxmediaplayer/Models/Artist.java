package com.santoshkumarsingh.gxmediaplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by santoshsingh on 08/10/17.
 */

public class Artist implements Parcelable {

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
    private String ARTIST, ALBUM, ALBUM_ART;

    public Artist() {
    }

    public Artist(String ARTIST, String ALBUM, String ALBUM_ART) {
        this.ARTIST = ARTIST;
        this.ALBUM = ALBUM;
        this.ALBUM_ART = ALBUM_ART;
    }

    protected Artist(Parcel in) {
        ARTIST = in.readString();
        ALBUM = in.readString();
        ALBUM_ART = in.readString();
    }

    public String getARTIST() {
        return ARTIST;
    }

    public void setARTIST(String ARTIST) {
        this.ARTIST = ARTIST;
    }

    public String getALBUM() {
        return ALBUM;
    }

    public void setALBUM(String ALBUM) {
        this.ALBUM = ALBUM;
    }

    public String getALBUM_ART() {
        return ALBUM_ART;
    }

    public void setALBUM_ART(String ALBUM_ART) {
        this.ALBUM_ART = ALBUM_ART;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ARTIST);
        dest.writeString(ALBUM);
        dest.writeString(ALBUM_ART);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Artist))
            return false;

        return ARTIST.equals(((Artist) obj).getARTIST());
    }

    @Override
    public int hashCode() {
        return (ARTIST == null) ? 0 : ARTIST.hashCode();
    }

}