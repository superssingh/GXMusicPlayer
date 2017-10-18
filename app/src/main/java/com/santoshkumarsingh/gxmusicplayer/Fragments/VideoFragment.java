package com.santoshkumarsingh.gxmusicplayer.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.santoshkumarsingh.gxmusicplayer.Adapters.VideoRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.VideoOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Video;
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

public class VideoFragment extends Fragment implements VideoOnClickListener {

    @BindView(R.id.Video_recyclerView)
    RecyclerView recyclerView;
    private OnFragmentInteractionListener mListener;
    private List<Video> videos;
    private VideoRecyclerAdapter videoRecyclerAdapter;
    private CompositeDisposable disposable;

    private View view;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);
        videos = new ArrayList<>();
        Load_VideoFiles();
        return view;
    }

    private void Load_VideoFiles() {
        disposable = new CompositeDisposable();
        disposable.add(getAudio()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Video>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Video> videos) {
                        setDataIntoAdapter(videos);
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

    private Observable<List<Video>> getAudio() {
        return Observable.fromCallable(new Callable<List<Video>>() {
            @Override
            public List<Video> call() throws Exception {
                return loadVideo();
            }
        });
    }

    public List<Video> loadVideo() {
        List<Video> videos = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = "LOWER(" + MediaStore.Audio.Media.DISPLAY_NAME + ") ASC";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String isVideo = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (isVideo.contains(".mp4") || isVideo.contains(".3gp")) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String genres = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY));

                        Video video = new Video(id, title, artist, url, album, duration, genres);
                        videos.add(video);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }

        return videos;
    }


    private void setDataIntoAdapter(List<Video> videos) {
        this.videos = videos;
        videoRecyclerAdapter = new VideoRecyclerAdapter(this, videos);
        videoRecyclerAdapter.notifyDataSetChanged();
        configRecycleView();
    }

    private void configRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(videoRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void OnItemClicked(String videoURL) {
        mListener.onVideoFragmentInteraction(videoURL);
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Load_VideoFiles();
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

    public interface OnFragmentInteractionListener {
        void onVideoFragmentInteraction(String videoURL);
    }
}
