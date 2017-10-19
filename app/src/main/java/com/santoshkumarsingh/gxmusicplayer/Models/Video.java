package com.santoshkumarsingh.gxmusicplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

public class Video implements Parcelable {

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    private String TITLE, URL, ALBUM, DURATION, THUMBNAIL;

    public Video() {
    }

    public Video(String TITLE, String URL, String ALBUM, String DURATION, String THUMBNAIL) {
        this.TITLE = TITLE;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
        this.THUMBNAIL = THUMBNAIL;
    }

    protected Video(Parcel in) {
        TITLE = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
        THUMBNAIL = in.readString();
    }

    public String getTITLE() {
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
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

    public String getTHUMBNAIL() {
        return THUMBNAIL;
    }

    public void setTHUMBNAIL(String THUMBNAIL) {
        this.THUMBNAIL = THUMBNAIL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TITLE);
        dest.writeString(URL);
        dest.writeString(ALBUM);
        dest.writeString(DURATION);
        dest.writeString(THUMBNAIL);
    }


}
