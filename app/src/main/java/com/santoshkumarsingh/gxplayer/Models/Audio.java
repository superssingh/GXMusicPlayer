package com.santoshkumarsingh.gxplayer.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.santoshkumarsingh.gxplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.InstrumentalAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.MotivationalAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.NewAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.PartyAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.SoulAudio;

import java.util.ArrayList;
import java.util.List;

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
    private String ID, TITLE, ARTIST, URL, ALBUM, DURATION;
    public Audio() {
    }

    public Audio(NewAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }

    public Audio(PartyAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }

    public Audio(SoulAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }

    public Audio(MotivationalAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }

    public Audio(InstrumentalAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }

    public Audio(FavoriteAudio audios) {
        this.ID = audios.getID();
        this.TITLE = audios.getTITLE();
        this.ARTIST = audios.getARTIST();
        this.URL = audios.getURL();
        this.ALBUM = audios.getALBUM();
        this.DURATION = audios.getDURATION();
    }


    public Audio(String ID, String TITLE, String ARTIST, String URL, String ALBUM, String DURATION) {
        this.ID = ID;
        this.TITLE = TITLE;
        this.ARTIST = ARTIST;
        this.URL = URL;
        this.ALBUM = ALBUM;
        this.DURATION = DURATION;
    }

    protected Audio(Parcel in) {
        ID = in.readString();
        TITLE = in.readString();
        ARTIST = in.readString();
        URL = in.readString();
        ALBUM = in.readString();
        DURATION = in.readString();
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

    public List<Audio> getFavAudio(List<FavoriteAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (FavoriteAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    public List<Audio> getInstAudio(List<InstrumentalAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (InstrumentalAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    public List<Audio> getNewAudio(List<NewAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (NewAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    public List<Audio> getMotivAudio(List<MotivationalAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (MotivationalAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    public List<Audio> getSoulAudio(List<SoulAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (SoulAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    public List<Audio> getPartyAudio(List<PartyAudio> Audios) {
        List<Audio> audioList = new ArrayList<>();
        for (PartyAudio audio : Audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

}
