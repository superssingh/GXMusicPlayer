package com.santoshkumarsingh.gxmediaplayer.Database.RealmDB;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class InstrumentalAudio extends RealmObject implements Parcelable {

    public static final Creator<InstrumentalAudio> CREATOR = new Creator<InstrumentalAudio>() {
        @Override
        public InstrumentalAudio createFromParcel(Parcel in) {
            return new InstrumentalAudio(in);
        }

        @Override
        public InstrumentalAudio[] newArray(int size) {
            return new InstrumentalAudio[size];
        }
    };

    @PrimaryKey
    private String ID;
    private String TITLE, ARTIST, URL, ALBUM, DURATION;

    public InstrumentalAudio() {
    }

    public InstrumentalAudio(String ID, String TITLE, String ARTIST, String URL, String ALBUM, String DURATION) {
        this.ID = ID;
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
    }

    protected InstrumentalAudio(Parcel in) {
        ID = in.readString();
        TITLE = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(TITLE);
        dest.writeString(ARTIST);
        dest.writeString(URL);
        dest.writeString(ALBUM);
        dest.writeString(DURATION);
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

}
