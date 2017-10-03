package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Fragments.DetailFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.FavoriteFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.HomeFragment;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.FavoriteOnClickListener2;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
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
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

@SuppressWarnings("WeakerAccess")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ServiceCallback, TabLayout.OnTabSelectedListener, HomeFragment.OnFragmentInteractionListener, FavoriteFragment.OnFragmentInteractionListener, DetailFragment.OnFragmentInteractionListener {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.santoshkumarsingh.gxmusicplayer.PlayNewAudio";
    private static MediaPlayerService playerService;
    @BindView(R.id.fab)
    FloatingActionButton fab;
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
    @BindView(R.id.Tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.View_Pager)
    ViewPager pager;
    Animation animation;
    FavoriteOnClickListener2 favoriteOnClickListener;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Toolbar toolbar;
    private Bitmap bitmap;
    private CompositeDisposable disposable, disposable1;
    private StorageUtil storageUtil;
    private RealmConfiguration config;
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
            UI_update(trackPosition, bitmap);
            play_layout.setVisibility(View.VISIBLE);
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

        // Realm Initialization
        Realm.removeDefaultConfiguration();
        Realm.init(this);
        config = new RealmConfiguration.Builder()
                .name(getString(R.string.RealmDatabaseName))
                .schemaVersion(Integer.parseInt(getString(R.string.VERSION)))
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        ButterKnife.bind(this);
        NavigationDrawerSetup();
        disposable = new CompositeDisposable();
        disposable1 = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities();
        storageUtil = new StorageUtil(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        play_layout.setAnimation(animation);
        songTitle.setSelected(true);
        playerService = new MediaPlayerService();
        play_layout.setVisibility(View.GONE);
        InitializedView();

        if (storageUtil.loadAudioIndex() != -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();
            ConnectMediaPlayer();
        } else {
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void InitializedView() {
        //------Tab & View pager init...
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //------OnclickedListenters

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

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    playPause.setImageResource(R.drawable.ic_play_circle_filled);
                } else {
                    playerService.resume();
                    playPause.setImageResource(R.drawable.ic_pause_circle_filled);
                }
            }
        });

    }

    private void Load_Audio_Data() {
        disposable.add(getAudio()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {

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
                        ConnectMediaPlayer();
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
                    if (storageUtil.loadAudioIndex() != -1) {
                        audioList = storageUtil.loadAudio();
                    }
                } while (audioList != null);

                return audioList;
            }
        });
    }

//    private void showProgressbar() {
//        progressBar.setVisibility(View.VISIBLE);
//    }
//
//    private void hideProgressbar() {
//        progressBar.setVisibility(View.GONE);
//    }

    private void ConnectMediaPlayer() {
        disposable.add(observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {
                        bitmap = utilities.getTrackThumbnail(audioList.get(s).getURL()) != null
                                ? utilities.getTrackThumbnail(audioList.get(s).getURL()) : null;
//                                : BitmapFactory.decodeResource(getResources(), R.drawable.ic_headset));
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
//                        UI_update(s);
                        playAudio(s);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Service_bound_Error-", e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("Service_Bound- ", "Completed ");
                        if (playerService != null && playerService.mediaPlayer != null) {
                            Log.d("Service_Bound- ", "" + playerService.ismAudioIsPlaying() + "/ " + playerService.getRepeat());
                            setPlayPauseState(playerService.mediaPlayer.isPlaying());
                            setRepeatButtonIcon(playerService.getRepeat());
                        }
                        setRepeatButtonIcon(playerService.getRepeat());
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

                    Log.d("Observable- ", "" + playerService.ismAudioIsPlaying() + "/ " + playerService.getRepeat());
                } while (playerService == null);

                e.onComplete();
            }
        });
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
        playPause.setImageResource(R.drawable.ic_pause_circle_filled);

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

    @Override
    public void doSomething(int position, Bitmap bitmap) {
        UI_update(position, bitmap);
    }

    public void UI_update(int trackPosition, Bitmap bitmap) {
        setRepeatButtonIcon(playerService.getRepeat());
        setPlayPauseState(playerService.ismAudioIsPlaying());
        if (bitmap == null) {
            trackThumbnail.setImageResource(R.drawable.ic_headset);
        } else {
            trackThumbnail.setImageBitmap(bitmap);
        }

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
                Log.e("SeekBar_Error: ", e.toString());
            }

            @Override
            public void onComplete() {

            }
        });

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
    public void onHomeFragmentInteraction(List<Audio> audios, int position) {
        audioList = audios;
        trackPosition = position;
        ConnectMediaPlayer();
    }

    @Override
    public void onDetailFragmentInteraction(int position) {
        playAudio(position);
    }

    @Override
    public void onFavoriteFragmentInteraction(RealmResults<FavoriteAudio> audios, int position) {
        audioList = convertList(audios);
        playAudio(position);
    }

    private List<Audio> convertList(RealmResults<FavoriteAudio> audios) {
        List<Audio> audioList = new ArrayList<>();
        for (FavoriteAudio audio : audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    //*****Tab Layout***********
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private void setRepeatButtonIcon(int repeat) {
        switch (repeat) {
            case 0:
                fab.setImageResource(R.drawable.ic_repeat_all);
                break;
            case 1:
                fab.setImageResource(R.drawable.ic_repeat_one);
                break;
            case 2:
                fab.setImageResource(R.drawable.ic_shuffle);
                break;
        }
    }

    private void setPlayPauseState(boolean playPauseState) {
        if (playPauseState) {
            playPause.setImageResource(R.drawable.ic_pause_circle_filled);
        } else {
            playPause.setImageResource(R.drawable.ic_play_circle_filled);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        String[] tabs;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.Tabs);
        }

        @Override
        public Fragment getItem(int position) {
            HomeFragment homeFragment = new HomeFragment();
            FavoriteFragment favoriteFragment = new FavoriteFragment();
            DetailFragment detailFragment = new DetailFragment();

            switch (position) {
                case 0:
                    return homeFragment;
                case 1:
                    return favoriteFragment;
                case 2:
                    return detailFragment;
            }

            return homeFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}
