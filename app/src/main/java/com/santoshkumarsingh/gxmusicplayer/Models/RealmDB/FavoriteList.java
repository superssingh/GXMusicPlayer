package com.santoshkumarsingh.gxmusicplayer.Models.RealmDB;

import java.util.List;

import io.realm.RealmObject;

/**
 * Created by santoshsingh on 22/09/17.
 */

public class FavoriteList extends RealmObject {
    private List<FavoriteAudio> TABLENAME;

    public List<FavoriteAudio> getTABLENAME() {
        return TABLENAME;
    }

    public void setTABLENAME(List<FavoriteAudio> TABLENAME) {
        this.TABLENAME = TABLENAME;
    }
}
