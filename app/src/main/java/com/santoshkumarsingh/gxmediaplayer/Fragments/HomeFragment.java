package com.santoshkumarsingh.gxmediaplayer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.santoshkumarsingh.gxmediaplayer.Adapters.AudioRecyclerAdapter;
import com.santoshkumarsingh.gxmediaplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmediaplayer.Interfaces.SongOnClickListener;
import com.santoshkumarsingh.gxmediaplayer.Models.Audio;
import com.santoshkumarsingh.gxmediaplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

public class HomeFragment extends Fragment implements SongOnClickListener {

    @BindView(R.id.Home_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private OnFragmentInteractionListener mListener;
    private List<Audio> audioList;
    private AudioRecyclerAdapter audioRecyclerAdapter;
    private CompositeDisposable disposable;
    private StorageUtil storageUtil;
    private View view;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        audioList = new ArrayList<>();
        disposable = new CompositeDisposable();
        storageUtil = new StorageUtil(getActivity());

        if (storageUtil.loadAudioIndex() == -1) {
            checkPermission();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Load_AudioFiles();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 23);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 23:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Load_AudioFiles();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.permission_denied), LENGTH_LONG).show();
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                Load_AudioFiles();
        }
    }

    private void Load_AudioFiles() {
        disposable.add(getAudio()
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        progressBar.setVisibility(View.VISIBLE);

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        audioList = loadAudios(getContext());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setDataIntoAdapter(audioList);
                    }
                })
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer integer) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error::Home ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("OnComplete:: ", "Completed");

                    }
                }));
    }

    private Observable<Integer> getAudio() {
        return Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 0;
            }
        });
    }

    public List<Audio> loadAudios(Context context) {
        List<Audio> audios = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = "LOWER(" + MediaStore.Audio.Media.DISPLAY_NAME + ") ASC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    Audio audio = new Audio(id, title, artist, url, album, duration);
                    audios.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        if (audios == null) {
            Toast.makeText(getActivity(), R.string.file_not_found, Toast.LENGTH_LONG).show();
        }


        return audios;
    }


    private void setDataIntoAdapter(List<Audio> audios) {
        audioRecyclerAdapter = new AudioRecyclerAdapter(this, audios);
        audioRecyclerAdapter.notifyDataSetChanged();
        configRecycleView();
    }

    //---------------------------
    private void configRecycleView() {
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(audioRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void OnItemClicked(List<Audio> audios, int position) {
        mListener.onHomeFragmentInteraction(audios, position);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            disposable = new CompositeDisposable();
            Load_AudioFiles();
        }
    }


    public interface OnFragmentInteractionListener {
        void onHomeFragmentInteraction(List<Audio> audios, int position);
    }
}
