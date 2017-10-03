package com.santoshkumarsingh.gxmusicplayer.Interfaces;

import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;

import io.realm.RealmResults;

/**
 * Created by santoshsingh on 26/09/17.
 */

public interface FavoriteOnClickListener {
    void OnClick(RealmResults<FavoriteAudio> audios, int position);
}