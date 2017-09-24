package com.santoshkumarsingh.gxmusicplayer.Database.RealmDB;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by santoshsingh on 22/09/17.
 */

public class RealmContentProvider {
    Context context;

    // adding selected movie info which comes from ArratList (Tablet view)
    public void addFavorite(final Context context, final Audio audio) {
        this.context = context;

        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class)
                .equalTo("id", audio.getURL())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                FavoriteAudio favoriteBook = realm1.createObject(FavoriteAudio.class, audio.getURL());
                favoriteBook.setTITLE(audio.getTITLE());
                favoriteBook.setARTIST(audio.getARTIST());
                favoriteBook.setURL(audio.getURL());
                favoriteBook.setALBUM(audio.getALBUM());
                favoriteBook.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.FavoriteMarked, Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_exists, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        realm.close();
    }

    public RealmResults<FavoriteAudio> getFavorites() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class).findAllAsync();
        realm.close();
        return favoriteAudios;
    }


}
