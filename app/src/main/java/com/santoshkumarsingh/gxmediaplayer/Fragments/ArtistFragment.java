package com.santoshkumarsingh.gxmediaplayer.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.santoshkumarsingh.gxmediaplayer.Adapters.ArtistRecyclerAdapter;
import com.santoshkumarsingh.gxmediaplayer.Interfaces.ArtistOnClickListener;
import com.santoshkumarsingh.gxmediaplayer.Models.Artist;
import com.santoshkumarsingh.gxmediaplayer.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ArtistFragment extends Fragment implements ArtistOnClickListener {

    @BindView(R.id.artist_recyclerView)
    RecyclerView recyclerView;
    ArtistRecyclerAdapter recyclerAdapter;
    List<Artist> artistList;
    CompositeDisposable disposable;
    private OnArtistFragmentInteractionListener mListener;
    private View view;

    public ArtistFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_artist, container, false);
        ButterKnife.bind(this, view);
        artistList = new ArrayList<Artist>();
        disposable = new CompositeDisposable();
        Log.d("Artist", "Bharat");

        Load_Artists();

        return view;
    }

    private Observable<Integer> getArtist() {
        return Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 0;
            }
        });
    }


    private void Load_Artists() {

        disposable.add(getAudio()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<Artist>>() {
                    @Override
                    public void accept(List<Artist> artists) throws Exception {
                        Log.d("Artist", "Bharat");
                        artistList = artists;
                        configRecycleView(artistList);
                        Log.d("Hi", "Ok ");
                    }
                })
                .subscribeWith(new DisposableObserver<List<Artist>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Artist> audios) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error::Albums ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("OnComplete:: ", "Completed");

                    }
                }));
    }

    private Observable<List<Artist>> getAudio() {
        return Observable.fromCallable(new Callable<List<Artist>>() {
            @Override
            public List<Artist> call() throws Exception {
                return loadArtistsList();
            }
        });
    }

    private void configRecycleView(List<Artist> ArtistList) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerAdapter = new ArtistRecyclerAdapter(this, ArtistList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArtistFragmentInteractionListener) {
            mListener = (OnArtistFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public List<Artist> loadArtistsList() {
        List<Artist> artists = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = "LOWER(" + MediaStore.Audio.Media.ARTIST + ") ASC";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String art = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                    Artist data = new Artist(artist, name, art);
                    artists.add(data);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        return removeDuplicates(artists);
    }

    private List<Artist> removeDuplicates(List<Artist> artists) {
        List<Artist> listContacts = new ArrayList<Artist>();
        //LinkedHashSet preserves the order of the original list
        Set<Artist> unique = new LinkedHashSet<Artist>(artists);
        listContacts = new ArrayList<Artist>(unique);

        return listContacts;
    }

    @Override
    public void OnClick(String AlbumID) {
        mListener.onArtistFragmentInteraction(AlbumID);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            disposable = new CompositeDisposable();
            Load_Artists();

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
    }

    public interface OnArtistFragmentInteractionListener {
        void onArtistFragmentInteraction(String id);
    }


}
