package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.AudioRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.SongOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.santoshkumarsingh.gxmusicplayer.Activities.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class ListActivity extends AppCompatActivity implements ServiceCallback, SongOnClickListener {

    private static MediaPlayerService playerService;
    @BindView(R.id.List_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.Current_Play_Pause)
    ImageButton playPause;
    @BindView(R.id.track_Thumbnail)
    ImageView trackThumbnail;
    @BindView(R.id.trackTitle)
    TextView songTitle;
    @BindView(R.id.trackArtist)
    TextView songArtist;
    @BindView(R.id.current_seekBar)
    AppCompatSeekBar seekBar;
    @BindView(R.id.play_layout)
    FrameLayout play_layout;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Bitmap bitmap;
    private CompositeDisposable disposable;
    private StorageUtil storageUtil;
    private Animation animation;
    private int categoryState = 3;
    private AudioRecyclerAdapter audioRecyclerAdapter;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            playerService.setCallback(ListActivity.this);
            serviceBound = true;
            Toast.makeText(ListActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
            UI_update(audioList, trackPosition, bitmap);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Toast.makeText(ListActivity.this, "Service Unbound", Toast.LENGTH_SHORT).show();
        }
    };
    private int category;
    private String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString(getString(R.string.Keyword));
        category = bundle.getInt(getString(R.string.category));

        disposable = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities();
        storageUtil = new StorageUtil(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        play_layout.setAnimation(animation);
        songTitle.setSelected(true);
        playerService = new MediaPlayerService();

        Load_Audio_Data();
    }


    private void setDataIntoAdapter(List<Audio> audios) {
        audioList = audios;
        audioRecyclerAdapter = new AudioRecyclerAdapter(this, audios);
        audioRecyclerAdapter.notifyDataSetChanged();
        configRecycleView();
    }

    private void configRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(audioRecyclerAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    playPause.setImageResource(R.drawable.ic_play_circle_outline);
                } else {
                    playerService.resume();
                    playPause.setImageResource(R.drawable.ic_pause_circle_outline);
                }
            }
        });

        play_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void ConnectMediaPlayer() {
        disposable.add(observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {
                        bitmap = utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                                ? utilities.compressBitmap(utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()))
                                : utilities.decodeSampledBitmapFromResource(getResources(), R.drawable.audio_image, 50, 50);

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {

                    }
                })
                .subscribeWith(new DisposableObserver<Integer>() {

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer s) {
                        playAudio(s, categoryState);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Service_bound_Error-", e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("Service_Bound- ", "Completed ");
                    }
                }));
    }

    private Observable<Integer> observable() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<Integer> e) throws Exception {
                do {
                    if (playerService == null) {
                        playerService = MediaPlayerService.getInstance(getApplicationContext());
                        playerService.setAudioList(audioList);
                    }
                    e.onNext(trackPosition);
                } while (playerService == null);

                e.onComplete();
            }
        });
    }

    private void playAudio(int audioIndex, int categoryIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            storage.storeCategoryIndex(categoryIndex);

            playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            if (categoryIndex == storageUtil.loadCategoryIndex()) {
                //Store the new audioIndex to SharedPreferences
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudioIndex(audioIndex);

                //Service is active
                //Send a broadcast to the service -> PLAY_NEW_AUDIO
                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
            } else {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudio(audioList);
                storage.storeAudioIndex(audioIndex);
                storage.storeCategoryIndex(categoryIndex);

                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);

            }
        }

        trackPosition = audioIndex;
        categoryState = categoryIndex;
        playPause.setImageResource(R.drawable.ic_pause_circle_outline);

    }

    @Override
    public void doSomething(List<Audio> audioList, int position, Bitmap bitmap) {
        UI_update(audioList, position, bitmap);

    }

    public void UI_update(List<Audio> audioList, int trackPosition, Bitmap bitmap) {
        this.audioList = audioList;
        setPlayPauseState(playerService.ismAudioIsPlaying());

        trackThumbnail.setImageBitmap(bitmap);
        songTitle.setText(audioList.get(trackPosition).getTITLE());
        songArtist.setText(audioList.get(trackPosition).getARTIST());

        seekBar.setMax(Integer.parseInt(audioList.get(trackPosition).getDURATION()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    if (playerService.mediaPlayer.isPlaying()) {
                        playerService.mediaPlayer.seekTo(i);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        update_seekBar();
    }

    public void seekBarCycle() {
        int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
        seekBar.setProgress(i);
    }

    private void update_seekBar() {
        Observable.interval(1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull Long aLong) {
                if (playerService.mediaPlayer != null) {
                    seekBarCycle();
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Log.e("SeekBar_loop: ", e.toString());
            }

            @Override
            public void onComplete() {

            }
        });

    }

    private void setPlayPauseState(boolean playPauseState) {
        if (playPauseState) {
            playPause.setImageResource(R.drawable.ic_pause_circle_outline);
        } else {
            playPause.setImageResource(R.drawable.ic_play_circle_outline);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serviceBound) {
            unbindService(serviceConnection);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Load_Audio_Data();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
    }

    @Override
    public void OnItemClicked(List<Audio> audios, int position) {
        categoryState = 0;
        trackPosition = position;
        audioList = audios;
        storageUtil.storeAudio(audioList);
        playerService.setAudioList(audios);
        playAudio(position, categoryState);
    }

    private List<Audio> loadAlbumFiles(String albumId) {
        List<Audio> audios = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 and album_id = " + albumId;
        String sortOrder = "LOWER(" + MediaStore.Audio.Media.DISPLAY_NAME + ") ASC";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String genres = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY));

                    Audio audio = new Audio(id, title, artist, url, album, duration, genres);
                    audios.add(audio);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return audios;
    }

    private List<Audio> loadArtistFiles(String artistname) {
        List<Audio> audios = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 and artist = '" + artistname + "'";
        String sortOrder = "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String genres = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY));

                    Audio audio = new Audio(id, title, artist, url, album, duration, genres);
                    audios.add(audio);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return audios;
    }

    private void Load_Audio_Data() {
        disposable.add(getAudio()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                        if (integer == 3) {
                            getSupportActionBar().setTitle("Songs of album");
                            audioList = loadAlbumFiles(keyword);
                        } else if (integer == 4) {
                            getSupportActionBar().setTitle("Songs of artist");
                            audioList = loadArtistFiles(keyword);
                        }
                        setDataIntoAdapter(audioList);

                        if (storageUtil.loadAudioIndex() != -1) {
                            categoryState = storageUtil.loadCategoryIndex();
                            trackPosition = storageUtil.loadAudioIndex();
                            audioList = storageUtil.loadAudio();
                            bitmap = utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                                    ? utilities.compressBitmap(utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()))
                                    : utilities.decodeSampledBitmapFromResource(getResources(), R.drawable.audio_image, 50, 50);

                            playerIntent = new Intent(ListActivity.this, MediaPlayerService.class);
                            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        } else {
                            return;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer position) {
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error:: ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("OnComplete:: ", "Resume Completed");
                    }
                }));
    }

    private Observable<Integer> getAudio() {
        return Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return category;
            }
        });
    }


}
