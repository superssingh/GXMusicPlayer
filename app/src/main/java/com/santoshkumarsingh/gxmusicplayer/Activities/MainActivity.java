package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.AudioAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Services.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ServiceCallback {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.santoshkumarsingh.gxmusicplayer.PlayNewAudio";
    private static MediaPlayerService playerService;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.play_pause)
    ImageButton play_pause;
    @BindView(R.id.previous)
    ImageButton previous;
    @BindView(R.id.next)
    ImageButton next;
    @BindView(R.id.songThumbnail)
    ImageView songThumbnail;
    @BindView(R.id.trackTitle)
    TextView songTitle;
    @BindView(R.id.trackArtist)
    TextView songArtist;
    @BindView(R.id.trackDuration)
    TextView songDuration;
    @BindView(R.id.seekBar)
    AppCompatSeekBar seekBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private Observable<String> stringObservable;
    private Observer<String> stringObserver;

    private Utilities utilities;
    private int trackPosition = 0, repeat = 0, mediaPlayerState = 0;
    private List<Audio> audioList;
    private LinearLayoutManager linearLayoutManager;
    private AudioAdapter audioAdapter;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Toolbar toolbar;
    private Handler handler;
    private Runnable runnable;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private Bitmap bitmap;
    private Disposable disposable;

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            playerService.setCallback(MainActivity.this);
            serviceBound = true;
            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Toast.makeText(MainActivity.this, "Service Unbound", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        audioList = new ArrayList<>();
        utilities = new Utilities();
        handler = new Handler();
        seekBar.setClickable(true);

        checkPermission();
        configRecycleView();
        NavigationDrawerSetup();

        StorageUtil storageUtil = new StorageUtil(MainActivity.this);
        if (storageUtil.loadAudioIndex() != -1) {
            trackPosition = storageUtil.loadAudioIndex();
        }


    }

    private void ConnectMediaPlayer() {
        observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        bitmap = (utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                                ? utilities.getTrackThumbnail(audioList.get(trackPosition).getURL())
                                : BitmapFactory.decodeResource(getResources(), R.drawable.audio_image));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull String s) {
                        Toast.makeText(MainActivity.this, "Connection : " + s, LENGTH_LONG).show();
                        UI_update(trackPosition);
                        playAudio(trackPosition);

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Service_bound_Error-", e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<String> observable() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull ObservableEmitter<String> e) throws Exception {
                do {
                    if (playerService == null) {
                        playerService = MediaPlayerService.getInstance(getApplicationContext());
                        playerService.setAudioList(audioList);
                    }
                    e.onNext(getString(R.string.SERVICE_CONNECTED));
                } while (playerService == null);

                e.onComplete();
            }
        });
    }

    //-------
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
                Log.e("SeekBar_Error: ", e.toString());
            }

            @Override
            public void onComplete() {

            }
        });

    }

    //-------

    @Override
    protected void onStart() {
        super.onStart();
        ConnectMediaPlayer();
    }

    private void NavigationDrawerSetup() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    //---------------------------
    private void configRecycleView() {
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        audioAdapter = new AudioAdapter(this);
        audioAdapter.addSongs(audioList);
        audioAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(audioAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        audioAdapter.setOnClickListener(new AudioAdapter.SongOnClickListener() {
            @Override
            public void OnClick(ImageButton optionButton, View view, Bitmap bitmap, String URL, int position) {
                trackPosition = position;
                playAudio(position);
                optionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeat == 0) {
                    repeat = 1;
                    playerService.setRepeat(1);
                    fab.setImageResource(R.drawable.ic_repeat_one);
                } else if (repeat == 1) {
                    repeat = 2;
                    playerService.setRepeat(2);
                    fab.setImageResource(R.drawable.ic_shuffle);
                } else {
                    repeat = 0;
                    playerService.setRepeat(0);
                    fab.setImageResource(R.drawable.ic_repeat_all);
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mediaPlayerState) {
                    case 1:
                        playerService.pause();
                        play_pause.setBackgroundResource(R.drawable.ic_play);
                        mediaPlayerState = 2;
                        break;
                    case 2:
                        playerService.resume();
                        play_pause.setBackgroundResource(R.drawable.ic_pause);
                        mediaPlayerState = 1;
                        break;

                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                }

                playerService.skipToPrevious();
                playerService.playMedia();
                play_pause.setBackgroundResource(R.drawable.ic_pause);
                trackPosition = playerService.getAudioIndex();

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                }

                playerService.skipToNext();
                playerService.playMedia();
                play_pause.setBackgroundResource(R.drawable.ic_pause);
                trackPosition = playerService.getAudioIndex();
            }
        });

    }

    private void playAudio(int audioIndex) {

        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }

        trackPosition = audioIndex;
        mediaPlayerState = 1;
        play_pause.setBackgroundResource(R.drawable.ic_pause);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    //-----------
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 24);
                return;
            }
        }

        loadAudioFiles();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 24:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadAudioFiles();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), LENGTH_LONG).show();
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void loadAudioFiles() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = this.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    Audio audio = new Audio(title, artist, url, album, duration);
                    audioList.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_exit) {
            if (serviceBound || playerService.mediaPlayer != null) {
                stopService(playerIntent);
                System.exit(0);
                playerService.onDestroy();
            }

            stopService(playerIntent);
            audioList.clear();
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }

    }

    public void seekBarCycle() {
        int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
        seekBar.setProgress(i);
    }

    @Override
    public void doSomething(int position, int duration, int currentTime, Bitmap bitmap) {
        songThumbnail.setImageBitmap(bitmap);
        songTitle.setText(audioList.get(position).getTITLE());
        songArtist.setText(audioList.get(position).getARTIST());
        songDuration.setText(decimalFormat.format(((float) duration / 1000) / 60) + "");

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

    public void UI_update(int trackPosition) {
        setRepeatButton(playerService.getRepeat());
        songThumbnail.setImageBitmap(bitmap);
        songTitle.setText(audioList.get(trackPosition).getTITLE());
        songArtist.setText(audioList.get(trackPosition).getARTIST());
        songDuration.setText(decimalFormat.format(((float)
                Integer.parseInt(audioList.get(trackPosition).getDURATION()) / 1000) / 60) + "");

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

    private void setRepeatButton(int repeat) {
        this.repeat = repeat;
        if (repeat == 1) {
            fab.setImageResource(R.drawable.ic_repeat_one);
        } else if (repeat == 2) {
            fab.setImageResource(R.drawable.ic_shuffle);
        } else {
            fab.setImageResource(R.drawable.ic_repeat_all);
        }
    }

}
