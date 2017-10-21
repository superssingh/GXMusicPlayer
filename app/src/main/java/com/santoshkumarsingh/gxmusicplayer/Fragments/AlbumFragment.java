package com.santoshkumarsingh.gxmusicplayer.Fragments;

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
import android.widget.Toast;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.santoshkumarsingh.gxmusicplayer.Adapters.AlbumRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.AlbumOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Album;
import com.santoshkumarsingh.gxmusicplayer.R;

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
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AlbumFragment extends Fragment implements AlbumOnClickListener {

    @BindView(R.id.album_recyclerView)
    RecyclerView recyclerView;
    AlbumRecyclerAdapter recyclerAdapter;
    List<Album> albumList;
    CompositeDisposable disposable;
    private OnFragmentInteractionListener mListener;
    private View view;

    public AlbumFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, container, false);
        ButterKnife.bind(this, view);
        albumList = new ArrayList<>();
        disposable = new CompositeDisposable();
        Load_Albums();
        return view;
    }

    private void Load_Albums() {
        disposable.add(getAudio()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Album>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Album> audios) {
                        albumList = audios;
                        configRecycleView(albumList);
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

    private Observable<List<Album>> getAudio() {
        return Observable.fromCallable(new Callable<List<Album>>() {
            @Override
            public List<Album> call() throws Exception {
                return loadAlbums();
            }
        });
    }

    private void configRecycleView(List<Album> albumList) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerAdapter = new AlbumRecyclerAdapter(this, albumList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public List<Album> loadAlbums() {
        List<Album> albumList = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = "lower(" + MediaStore.Audio.Albums.ALBUM + ") ASC";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, sortOrder);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String isMP3 = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (isMP3.contains(".mp3") || isMP3.contains(".MP3")) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String art = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                        Album album = new Album(id, name, artist, art);
                        albumList.add(album);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }

        return removeDuplicates(albumList);
    }

    private List<Album> removeDuplicates(List<Album> albumList) {

        List<Album> listContacts = new ArrayList<Album>();
        //LinkedHashSet preserves the order of the original list
        Set<Album> unique = new LinkedHashSet<Album>(albumList);
        listContacts = new ArrayList<Album>(unique);

        return listContacts;
    }

    @Override
    public void OnClick(String AlbumID) {
        Toast.makeText(getContext(), "OK " + AlbumID, Toast.LENGTH_LONG).show();
        mListener.onFragmentInteraction(AlbumID);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);

    }
}
