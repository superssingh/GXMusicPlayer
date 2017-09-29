package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
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
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.AudioAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Utilities.LoadAudio;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.text.DecimalFormat;
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
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.widget.Toast.LENGTH_LONG;

@SuppressWarnings("WeakerAccess")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ServiceCallback, AudioAdapter.SongOnClickListener {

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
    @BindView(R.id.Progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.play_layout)
    FrameLayout play_layout;

    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private AudioAdapter audioAdapter;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Toolbar toolbar;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private Bitmap bitmap;
    private CompositeDisposable disposable, disposable1;
    private LoadAudio loadAudio;
    private RealmConfiguration config;
    private Animation animation;

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
    private StorageUtil storageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Realm Initialization
        Realm.init(this);
        config = new RealmConfiguration.Builder()
                .name(getString(R.string.RealmDatabaseName))
                .schemaVersion(Integer.parseInt(getString(R.string.VERSION)))
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        ButterKnife.bind(this);
        disposable = new CompositeDisposable();
        disposable1 = new CompositeDisposable();
        loadAudio = new LoadAudio(this);
        audioList = new ArrayList<>();
        utilities = new Utilities();
        seekBar.setClickable(true);
        NavigationDrawerSetup();

        storageUtil = new StorageUtil(MainActivity.this);
        trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();

        if (savedInstanceState == null) {
            audioList = null;
            if (storageUtil.loadAudioIndex() == -1) {
                checkPermission();
            } else {
                Load_Audio_Data();
            }
        } else {
            audioList = savedInstanceState.getParcelableArrayList(getString(R.string.AUDIO_STATE));
            setDataIntoAdapter(audioList);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (audioList == null && storageUtil.loadAudio() == null) {
//            checkPermission();
            Load_Audio_Data();
        } else {
            audioList = storageUtil.loadAudio();
            setDataIntoAdapter(audioList);
        }
    }

    private void Load_Audio_Data() {
        disposable.add(getAudio()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        showProgressbar();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {

                    }
                })
                .subscribeWith(new DisposableObserver<List<Audio>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Audio> audios) {
                        hideProgressbar();
                        setDataIntoAdapter(audios);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error:: ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("OnComplete:: ", "Completed");
                        ConnectMediaPlayer();

                    }
                }));
    }

    private Observable<List<Audio>> getAudio() {
        return Observable.fromCallable(new Callable<List<Audio>>() {
            @Override
            public List<Audio> call() throws Exception {
                return getAudioList();
            }
        });
    }

    private void showProgressbar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressbar() {
        progressBar.setVisibility(View.GONE);
    }

    private void ConnectMediaPlayer() {
        disposable1.add(observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        bitmap = (utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                                ? utilities.getTrackThumbnail(audioList.get(trackPosition).getURL())
                                : BitmapFactory.decodeResource(getResources(), R.drawable.ic_audiotrack));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull String s) {
                        UI_update(trackPosition);
                        playAudio(trackPosition);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Service_bound_Error-", e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("Service_Bound- ", "Completed ");
                        if (playerService != null && playerService.mediaPlayer != null) {
                            setPlayPauseState(playerService.ismAudioIsPlaying());
                        }

                    }
                }));
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
                    e.onNext("START");
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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArrayList(getString(R.string.AUDIO_STATE), new ArrayList<Parcelable>(audioAdapter.getAudioList()));
        outState.putInt(getString(R.string.SONG_POSITION), trackPosition);

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

    private void setDataIntoAdapter(List<Audio> audios) {
        audioList = audios;
        audioAdapter = new AudioAdapter(this, audios);
        audioAdapter.notifyDataSetChanged();
        configRecycleView();
    }

    //---------------------------
    private void configRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(audioAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (playerService.getRepeat()) {
                    case 0:
                        playerService.setRepeat(1);
                        fab.setImageResource(R.drawable.ic_repeat_one);
                        break;
                    case 1:
                        playerService.setRepeat(2);
                        fab.setImageResource(R.drawable.ic_shuffle);
                        break;
                    case 2:
                        playerService.setRepeat(0);
                        fab.setImageResource(R.drawable.ic_repeat_all);
                        break;
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    play_pause.setBackgroundResource(R.drawable.ic_play);
                } else {
                    playerService.resume();
                    play_pause.setBackgroundResource(R.drawable.ic_pause);
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

//
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

    private List<Audio> getAudioList() {
        return loadAudio.loadAudioFiles();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                    Load_Audio_Data();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), LENGTH_LONG).show();
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                Load_Audio_Data();
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

        if (id == R.id.nav_favorite) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_exit) {
            if (serviceBound || playerService.mediaPlayer != null) {
                stopService(playerIntent);
                playerService.onDestroy();
                System.exit(0);
            }

            audioList.clear();
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        setRepeatButtonIcon(playerService.getRepeat());
        setPlayPauseState(playerService.ismAudioIsPlaying());
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

    private void setRepeatButtonIcon(int repeat) {
        if (repeat == 1) {
            fab.setImageResource(R.drawable.ic_repeat_one);
        } else if (repeat == 2) {
            fab.setImageResource(R.drawable.ic_shuffle);
        } else {
            fab.setImageResource(R.drawable.ic_repeat_all);
        }
    }


    private void setPlayPauseState(boolean playPauseState) {
        if (playerService == null || playerService.mediaPlayer == null) {
            return;
        } else if (playerService.mediaPlayer != null) {
            if (playPauseState == false) {
                play_pause.setBackgroundResource(R.drawable.ic_play);
            } else {
                play_pause.setBackgroundResource(R.drawable.ic_pause);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }

        disposable.dispose();
        disposable1.dispose();
    }

    @Override
    public void OnItemClicked(int position) {
        playAudio(position);
    }
}
