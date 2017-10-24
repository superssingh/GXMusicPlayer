package com.santoshkumarsingh.gxplayer.Database.RealmDB;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.santoshkumarsingh.gxplayer.Models.Audio;
import com.santoshkumarsingh.gxplayer.R;

import io.reactivex.annotations.NonNull;
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
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                FavoriteAudio audiodata = realm1.createObject(FavoriteAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.FavoriteMarked, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_exists, Toast.LENGTH_SHORT)
                        .show();
                return;
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

    public void deleteAll() {
        final Realm realm = Realm.getDefaultInstance();
        realm.deleteAll();
        realm.close();
    }

    public void delete(int position) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class).findAll();
        realm.beginTransaction();
        FavoriteAudio fm = favoriteAudios.get(position);
        fm.deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public void addNewSong(final Context context, final Audio audio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<NewAudio> Audios = realm.where(NewAudio.class)
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                NewAudio audiodata = realm1.createObject(NewAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_added, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        });
        realm.close();
    }

    public void addPartySong(final Context context, final Audio audio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<PartyAudio> Audios = realm.where(PartyAudio.class)
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                PartyAudio audiodata = realm1.createObject(PartyAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_added, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        });
        realm.close();
    }

    public void addRelaxedSong(final Context context, final Audio audio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<SoulAudio> Audios = realm.where(SoulAudio.class)
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                SoulAudio audiodata = realm1.createObject(SoulAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_added, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        });
        realm.close();
    }

    public void addMotivationSong(final Context context, final Audio audio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<MotivationalAudio> Audios = realm.where(MotivationalAudio.class)
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                MotivationalAudio audiodata = realm1.createObject(MotivationalAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_added, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        });
        realm.close();
    }


    public void addInstpirationalSong(final Context context, final Audio audio) {
        this.context = context;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<InstrumentalAudio> Audios = realm.where(InstrumentalAudio.class)
                .equalTo("ID", audio.getID())
                .findAllAsync();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm1) {
                InstrumentalAudio audiodata = realm1.createObject(InstrumentalAudio.class, audio.getID());
                audiodata.setTITLE(audio.getTITLE());
                audiodata.setARTIST(audio.getARTIST());
                audiodata.setURL(audio.getURL());
                audiodata.setALBUM(audio.getALBUM());
                audiodata.setDURATION(audio.getDURATION());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT)
                        .show();
            }

        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                //delete method (for un-favorite) when it already exists--------------------------
                Log.e("Error: ", error.toString());
                Toast.makeText(context, R.string.Already_added, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        });
        realm.close();
    }



}
