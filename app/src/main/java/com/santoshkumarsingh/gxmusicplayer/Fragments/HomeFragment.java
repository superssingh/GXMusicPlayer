package com.santoshkumarsingh.gxmusicplayer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.AudioRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.SongOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

public class HomeFragment extends Fragment implements SongOnClickListener {

    @BindView(R.id.Home_recyclerView)
    RecyclerView recyclerView;
    int trackposition = 0;
    private OnFragmentInteractionListener mListener;
    private List<Audio> audioList;
    private AudioRecyclerAdapter audioRecyclerAdapter;
    private StorageUtil storageUtil;
    private CompositeDisposable disposable;

    private View view;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(int position) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("KEY", position);
        homeFragment.setArguments(bundle);
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        audioList = new ArrayList<>();
        storageUtil = new StorageUtil(getContext());
        disposable = new CompositeDisposable();

        if (storageUtil.loadAudio() == null) {
            checkPermission();
        } else {
            if (savedInstanceState != null) {
                audioList = savedInstanceState.getParcelableArrayList(getString(R.string.All_Audio));
            } else {
                audioList = storageUtil.loadAudio();
                trackposition = storageUtil.loadAudioIndex();
            }

            setDataIntoAdapter(audioList);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (storageUtil.loadAudio() == null) {
            Load_AudioFiles();
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 24);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 24:
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
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Audio>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Audio> audios) {
                        setDataIntoAdapter(audios);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error:: ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("OnComplete:: ", "Completed");

                    }
                }));
    }

    private Observable<List<Audio>> getAudio() {
        return Observable.fromCallable(new Callable<List<Audio>>() {
            @Override
            public List<Audio> call() throws Exception {
                do {
                    loadAudioFiles();
                } while (audioList == null);

                return loadAudioFiles();
            }
        });
    }

    public List<Audio> loadAudioFiles() {
        List<Audio> audios = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String genres = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres._ID));

                    Audio audio = new Audio(id, title, artist, url, album, duration, genres);
                    audios.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return audios;
    }


    private void setDataIntoAdapter(List<Audio> audios) {
        audioList = audios;
        audioRecyclerAdapter = new AudioRecyclerAdapter(this, audios);
        audioRecyclerAdapter.notifyDataSetChanged();
        configRecycleView();
    }

    //---------------------------
    private void configRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(audioRecyclerAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (audioList != null) {
            savedInstanceState.putParcelableArrayList(getString(R.string.All_Audio), (ArrayList<? extends Parcelable>) audioRecyclerAdapter.getAudioList());
        }
    }

    @Override
    public void OnItemClicked(List<Audio> audios, int position) {
        mListener.onHomeFragmentInteraction(audios, position);
    }

    //------------------------
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

        }
    }

    public interface OnFragmentInteractionListener {
        void onHomeFragmentInteraction(List<Audio> audios, int position);
    }

}
