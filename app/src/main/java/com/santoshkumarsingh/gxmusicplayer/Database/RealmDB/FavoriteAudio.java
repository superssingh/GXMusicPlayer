package com.santoshkumarsingh.gxmusicplayer.Database.RealmDB;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class FavoriteAudio extends RealmObject implements Parcelable {

    public static final Creator<FavoriteAudio> CREATOR = new Creator<FavoriteAudio>() {
        @Override
        public FavoriteAudio createFromParcel(Parcel in) {
            return new FavoriteAudio(in);
        }

        @Override
        public FavoriteAudio[] newArray(int size) {
            return new FavoriteAudio[size];
        }
    };
    @PrimaryKey
    private String id;
    private String TITLE, ARTIST, URL, ALBUM, DURATION, GENRES;

    public FavoriteAudio(String id, String TITLE, String ARTIST, String URL, String ALBUM, String DURATION, String GENRES) {
        this.id = id;
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
        this.GENRES = GENRES;
    }

    protected FavoriteAudio(Parcel in) {
        id = in.readString();
        TITLE = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
        GENRES = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(TITLE);
        parcel.writeString(ARTIST);
        parcel.writeString(URL);
        parcel.writeString(ALBUM);
        parcel.writeString(DURATION);
        parcel.writeString(GENRES);
    }
}
