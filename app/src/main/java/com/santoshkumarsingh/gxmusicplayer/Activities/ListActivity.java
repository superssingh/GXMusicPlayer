package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import be.rijckaert.tim.animatedvector.FloatingMusicActionButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
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
    @BindView(R.id.playfab)
    FloatingMusicActionButton playPauseFab;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
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
            UI_update(audioList, trackPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Toast.makeText(ListActivity.this, "Service Unbound", Toast.LENGTH_SHORT).show();
        }
    };
    private int category;
    private String keyword;
    private Toolbar toolbar;
    private String toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString(getString(R.string.Keyword));
        category = bundle.getInt(getString(R.string.category));

        toolbar = findViewById(R.id.customToolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.background_light));
        ButterKnife.bind(this);

        disposable = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities(getApplicationContext());
        storageUtil = new StorageUtil(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        play_layout.setAnimation(animation);
        songTitle.setSelected(true);
        playerService = new MediaPlayerService();

        if (storageUtil.loadAudioIndex() == -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex();
            Log.d("TrackPosition", audioList.get(trackPosition).getTITLE());
            Load_Audio_Data();
        }

    }

    private void setDataIntoAdapter(List<Audio> audios) {
        audioRecyclerAdapter = new AudioRecyclerAdapter(this, audios);
        audioRecyclerAdapter.notifyDataSetChanged();
        configRecycleView();
    }


    private void configRecycleView() {
        audioRecyclerAdapter = new AudioRecyclerAdapter(this, audioList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(audioRecyclerAdapter);

        playPauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    playPauseFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY);
                } else {
                    playerService.resume();
                    playPauseFab.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE);
                }
            }
        });

        play_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryState == 5) {
                    ListActivity.this.finish();
                } else {
                    Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                    startActivity(intent);
                }
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
        playPauseFab.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE);

    }

    @Override
    public void doSomething(List<Audio> audioList, int position) {
        UI_update(audioList, position);

    }

    public void UI_update(List<Audio> audio, int position) {
        this.audioList = audio;
        trackPosition = position;
        setPlayPauseState(playerService.ismAudioIsPlaying());

        Glide.with(ListActivity.this)
                .asBitmap()
                .load(utilities.getImageIntoByteArray(audio.get(position).getURL()))
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.ic_audiotrack))
                .transition(GenericTransitionOptions.with(R.anim.fade_in))
                .into(trackThumbnail);
        songTitle.setText(audio.get(position).getTITLE());
        songArtist.setText(audio.get(position).getARTIST());

        seekBar.setMax(Integer.parseInt(audio.get(position).getDURATION()));
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
            playPauseFab.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE);
        } else {
            playPauseFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
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
        if (serviceBound) {
            unbindService(serviceConnection);
        }

        disposable.dispose();
    }

    @Override
    public void OnItemClicked(List<Audio> audios, int position) {
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
                String isMP3 = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (isMP3.contains(".mp3") || isMP3.contains(".MP3")) {
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
                String isMP3 = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (isMP3.contains(".mp3") || isMP3.contains(".MP3")) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                        Audio audio = new Audio(id, title, artist, url, album, duration);
                        audios.add(audio);

                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        }

        return audios;
    }

    private void Load_Audio_Data() {
        disposable.add(getAudio()
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == 3) {
                            toolbarTitle = "Album Songs";
                            audioList = loadAlbumFiles(keyword);
                        } else if (integer == 4) {
                            toolbarTitle = "Artist Songs";
                            audioList = loadArtistFiles(keyword);
                        } else if (integer == 5) {
                            toolbarTitle = "Current Playlist";
                            audioList = storageUtil.loadAudio();
                            trackPosition = storageUtil.loadAudioIndex();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        toolbar.setTitle(toolbarTitle);
                        setDataIntoAdapter(audioList);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (storageUtil.loadAudioIndex() != -1) {
                            categoryState = storageUtil.loadCategoryIndex();
                            trackPosition = storageUtil.loadAudioIndex();
                            audioList = storageUtil.loadAudio();
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


    private List<Audio> loadSearchedFile(String searchSongName) {
        List<Audio> audios = new ArrayList<>();
        Toast.makeText(this, "Hiiii", Toast.LENGTH_LONG).show();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 and " + MediaStore.Audio.Media.DISPLAY_NAME + " = '" + searchSongName + "'";
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

                    Audio audio = new Audio(id, title, artist, url, album, duration);
                    audios.add(audio);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return audios;
    }


}
