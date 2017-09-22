package com.santoshkumarsingh.gxmusicplayer.Database;

import android.content.Context;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Models.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.R;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by santoshsingh on 22/09/17.
 */

public class RealmContentProvider {
    Context context;
    FavoriteAudio favoriteList;

    // adding selected movie info which comes from ArratList (Tablet view)
    public void addFavorite(final Context context, final FavoriteAudio favoriteAudio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<FavoriteAudio> Fav_Book = realm.where(FavoriteAudio.class)
                .equalTo("URL", favoriteAudio.getURL())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                FavoriteAudio favoriteBook = bgRealm.createObject(FavoriteAudio.class, favoriteAudio.getURL());
                favoriteBook.setTITLE(favoriteAudio.getTITLE());
                favoriteBook.setARTIST(favoriteAudio.getARTIST());
                favoriteBook.setURL(favoriteAudio.getURL());
                favoriteBook.setALBUM(favoriteAudio.getALBUM());
                favoriteBook.setDURATION(favoriteAudio.getDURATION());
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
                Toast.makeText(context, R.string.Already_exists, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        realm.close();
    }

}
