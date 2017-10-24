package com.santoshkumarsingh.gxplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by santoshsingh on 08/10/17.
 */

public class Album implements Parcelable {

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    private String ID, ALBUM, ARTIST, ALBUM_ART;

    public Album() {
    }

    public Album(String ID, String ALBUM, String ARTIST, String ALBUM_ART) {
        this.ID = ID;
        this.ALBUM = ALBUM;
        this.ARTIST = ARTIST;
        this.ALBUM_ART = ALBUM_ART;
    }

    protected Album(Parcel in) {
        ID = in.readString();
        ALBUM = in.readString();
        ARTIST = in.readString();
        ALBUM_ART = in.readString();
    }

    public static Creator<Album> getCREATOR() {
        return CREATOR;
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
        dest.writeString(ALBUM_ART);
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

    public String getALBUM_ART() {
        return ALBUM_ART;
    }

    public void setALBUM_ART(String ALBUM_ART) {
        this.ALBUM_ART = ALBUM_ART;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Album))
            return false;

        return ID.equals(((Album) obj).getID());
    }

    @Override
    public int hashCode() {
        return (ID == null) ? 0 : ID.hashCode();
    }

}