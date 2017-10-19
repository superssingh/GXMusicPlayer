package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import com.santoshkumarsingh.gxmusicplayer.Fragments.AlbumFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.ArtistFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.FavoriteFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.HomeFragment;
import com.santoshkumarsingh.gxmusicplayer.Fragments.VideoFragment;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

@SuppressWarnings("WeakerAccess")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener, HomeFragment.OnFragmentInteractionListener, FavoriteFragment.OnFragmentInteractionListener, AlbumFragment.OnFragmentInteractionListener, ArtistFragment.OnArtistFragmentInteractionListener, ServiceCallback, VideoFragment.OnFragmentInteractionListener {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.santoshkumarsingh.gxmusicplayer.PlayNewAudio";
    private static MediaPlayerService playerService;
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
    private Utilities utilities;
    private int trackPosition = 0, categoryState = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Toolbar toolbar;
    private CompositeDisposable disposable, disposable1, disposable2, disposable3;
    private StorageUtil storageUtil;
    private RealmConfiguration config;
    private Bitmap bitmap;
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
            UI_update(audioList, trackPosition, bitmap);
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
        toolbar = findViewById(R.id.customToolbar);
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
        disposable2 = new CompositeDisposable();
        disposable3 = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities(getApplicationContext());
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
            categoryState = storageUtil.loadCategoryIndex() == -1 ? 0 : storageUtil.loadCategoryIndex();
            ConnectMediaPlayer();
        } else {
            return;
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

    private void InitializedView() {
        initTabLayout();

        //------OnClickedListenters
        play_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.isPlayerStop()) {
                    playAudio(trackPosition, categoryState);
                } else {
                    if (playerService.ismAudioIsPlaying()) {
                        playerService.pause();
                        playPause.setImageResource(R.drawable.ic_play_circle_outline);
                    } else {
                        playerService.resume();
                        playPause.setImageResource(R.drawable.ic_pause_circle_outline);
                    }
                }
            }
        });

    }

    private void Load_Audio_Data() {
        disposable1.add(getAudio()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (storageUtil.loadAudioIndex() != -1) {
                            audioList = storageUtil.loadAudio();
                            trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();
                            categoryState = storageUtil.loadCategoryIndex() == -1 ? 0 : storageUtil.loadCategoryIndex();
                            bitmap = utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                                    ? utilities.getTrackThumbnail(audioList.get(trackPosition).getURL())
                                    : utilities.decodeSampledBitmapFromResource(getResources(), R.drawable.audio_image, 50, 50);
                            playerIntent = new Intent(MainActivity.this, MediaPlayerService.class);
                            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer position) {
                        trackThumbnail.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error:MainActivity1", e.getMessage());
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
                return trackPosition;
            }
        });
    }

    private void ConnectMediaPlayer() {
        disposable.add(observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {


                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        playAudio(integer, categoryState);
                    }
                })
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer s) {

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
                        playerService = MediaPlayerService.getInstance(MainActivity.this);
                        playerService.setAudioList(audioList);

                    }
                    e.onNext(trackPosition);
                } while (audioList == null);

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
                playerService.setAudioList(audioList);

                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
            }
        }

        trackPosition = audioIndex;
        categoryState = categoryIndex;

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
    public void doSomething(List<Audio> audioList, int position, Bitmap bitmap) {
        UI_update(audioList, position, bitmap);
    }

    public void UI_update(List<Audio> audio, int position, Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                    ? utilities.getTrackThumbnail(audioList.get(trackPosition).getURL())
                    : utilities.decodeSampledBitmapFromResource(getResources(), R.drawable.audio_image, 50, 50);
        }

        audioList = audio;
        trackPosition = position;
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


    private void update_seekBar() {
        disposable2.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (serviceBound) {
                            int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(i);
                        }
                    }
                })
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Long aLong) {
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("seekBar ", "error:" + e);
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        } else {
            return;
        }

        disposable.dispose();
        disposable1.dispose();
        disposable2.dispose();
        disposable3.dispose();
    }

    @Override
    public void onHomeFragmentInteraction(List<Audio> audios, int position) {
        categoryState = 1;
        trackPosition = position;
        audioList = audios;
        play_layout.setVisibility(View.VISIBLE);
        playerService.setAudioList(audioList);
        playAudio(position, categoryState);
    }

    @Override
    public void onFavoriteFragmentInteraction(RealmResults<FavoriteAudio> audios, int position) {
        categoryState = 2;
        trackPosition = position;
        audioList = convertList(audios);
        play_layout.setVisibility(View.VISIBLE);
        playerService.setAudioList(audioList);
        playAudio(position, categoryState);
    }

    @Override
    public void onFragmentInteraction(String id) {
        categoryState = 3;
        Intent intent = new Intent(MainActivity.this, ListActivity.class)
                .putExtra(getString(R.string.Keyword), id)
                .putExtra(getString(R.string.category), categoryState);
        startActivity(intent);
    }

    @Override
    public void onArtistFragmentInteraction(String id) {
        categoryState = 4;
        Intent intent = new Intent(MainActivity.this, ListActivity.class)
                .putExtra(getString(R.string.Keyword), id)
                .putExtra(getString(R.string.category), categoryState);
        startActivity(intent);

    }

    private List<Audio> convertList(RealmResults<FavoriteAudio> audios) {
        List<Audio> audioList = new ArrayList<>();
        for (FavoriteAudio audio : audios) {
            Audio a = new Audio(audio);
            audioList.add(a);
        }

        return audioList;
    }

    private void setPlayPauseState(boolean playPauseState) {
        if (playPauseState) {
            playPause.setImageResource(R.drawable.ic_pause_circle_outline);
        } else {
            playPause.setImageResource(R.drawable.ic_play_circle_outline);
        }
    }

    //*****Tab Layout------------

    private void initTabLayout() {
        //------Tab & View pager init...
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);
        int[] icons = {
                R.drawable.ic_library_music_24dp,
                R.drawable.ic_favorite_24dp,
                R.drawable.ic_album_24dp,
                R.drawable.ic_artist_black_24dp,
                R.drawable.ic_video_library_24dp
        };

        for (int i = 0; i < icons.length; i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }


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

    @Override
    public void onVideoFragmentInteraction(String videoURL) {
        if (playerService.ismAudioIsPlaying()) {
            playerService.pause();
        }

        play_layout.setVisibility(View.GONE);

        Intent intent = new Intent(MainActivity.this, FullscreenActivity.class)
                .putExtra(getString(R.string.videoURL), videoURL);
        startActivity(intent);

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
            AlbumFragment albumFragment = new AlbumFragment();
            ArtistFragment artistFragment = new ArtistFragment();
            VideoFragment videoFragment = new VideoFragment();

            switch (position) {
                case 0:
                    return homeFragment;
                case 1:
                    return favoriteFragment;
                case 2:
                    return albumFragment;
                case 3:
                    return artistFragment;
                case 4:
                    return videoFragment;
            }

            return homeFragment;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return 5;
        }

    }

}
