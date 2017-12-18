package com.santoshkumarsingh.gxmediaplayer.Interfaces;

import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.InstrumentalAudio;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.MotivationalAudio;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.NewAudio;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.PartyAudio;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.SoulAudio;

import io.realm.RealmResults;

/**
 * Created by santoshsingh on 26/09/17.
 */

public interface PlayListOnClickListener {
    void OnFavClick(RealmResults<FavoriteAudio> audios, int position);

    void OnNewClick(RealmResults<NewAudio> audios, int position);

    void OnPartyClick(RealmResults<PartyAudio> audios, int position);

    void OnSoulClick(RealmResults<SoulAudio> audios, int position);

    void OnMotivationClick(RealmResults<MotivationalAudio> audios, int position);

    void OnInstrumentClick(RealmResults<InstrumentalAudio> audios, int position);
}