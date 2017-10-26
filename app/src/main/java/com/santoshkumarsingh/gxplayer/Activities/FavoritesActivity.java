package com.santoshkumarsingh.gxplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
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

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.santoshkumarsingh.gxplayer.Adapters.AudioRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.FavoriteRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.InstrumentalRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.MotivationalRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.NewRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.PartyRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsAdapters.SoulRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.InstrumentalAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.MotivationalAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.NewAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.PartyAudio;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.SoulAudio;
import com.santoshkumarsingh.gxplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxplayer.Fragments.AlbumFragment;
import com.santoshkumarsingh.gxplayer.Interfaces.PlayListOnClickListener;
import com.santoshkumarsingh.gxplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxplayer.Models.Audio;
import com.santoshkumarsingh.gxplayer.R;
import com.santoshkumarsingh.gxplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxplayer.Utilities.Utilities;

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
import io.realm.Realm;
import io.realm.RealmResults;

import static com.santoshkumarsingh.gxplayer.Activities.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class FavoritesActivity extends AppCompatActivity implements ServiceCallback, PlayListOnClickListener {

    private static MediaPlayerService playerService;
    @BindView(R.id.fav_recyclerView)
    RecyclerView recyclerView;
    // all playlist adapters
    FavoriteRecyclerAdapter favoriteRecyclerAdapter;
    NewRecyclerAdapter newRecyclerAdapter;
    PartyRecyclerAdapter partyRecyclerAdapter;
    SoulRecyclerAdapter soulRecyclerAdapter;
    MotivationalRecyclerAdapter motivationalRecyclerAdapter;
    InstrumentalRecyclerAdapter instrumentalRecyclerAdapter;
    int[] Title = new int[]{
            R.string.favorite,
            R.string.NewAudio,
            R.string.PartyAudio,
            R.string.RelaxedAudio,
            R.string.Motivational,
            R.string.Instrumental
    };
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
    List<Audio> audioList;
    private AlbumFragment.OnFragmentInteractionListener mListener;
    private Realm realm = null;
    private Utilities utilities;
    private int trackPosition = 0;
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
            playerService.setCallback(FavoritesActivity.this);
            serviceBound = true;
            Log.d("Service Bound", "Yes");
            UI_update(audioList, trackPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d("Service Bound", "No");
        }
    };
    private int category;
    private int keyword;
    private Toolbar toolbar;
    private String toolbarTitle;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        toolbar = findViewById(R.id.customToolbar);
        ButterKnife.bind(this);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.background_light));
        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getInt(getString(R.string.Keyword));
        audioList = new ArrayList<>();
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(layoutManager);
        disposable = new CompositeDisposable();
        utilities = new Utilities(getApplicationContext());
        storageUtil = new StorageUtil(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        play_layout.setAnimation(animation);
        songTitle.setSelected(true);
        playerService = new MediaPlayerService();
        getList(keyword);

        if (storageUtil.loadAudioIndex() == -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex();
            Log.d("TrackPosition", audioList.get(trackPosition).getTITLE());

            Load_Audio_Data();
        }

        AddListeners();
        initAds();
    }

    private void Load_Audio_Data() {
        disposable.add(getAudio()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        getList(integer);
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
                            playerIntent = new Intent(FavoritesActivity.this, MediaPlayerService.class);
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
                return keyword;
            }
        });
    }


    private void initAds() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, getString(R.string.AppID));

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }


    private void getList(int keyword) {
        realm = Realm.getDefaultInstance();
        switch (keyword) {
            case 0:
                toolbar.setTitle(Title[keyword]);
                RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class).findAll();
                if (favoriteAudios.size() == 0) {
                    Log.d("Data 0", "0");
                    Toast.makeText(FavoritesActivity.this, getString(R.string.list_empty), Toast.LENGTH_LONG).show();
                } else {
                    favoriteRecyclerAdapter = new FavoriteRecyclerAdapter(this, favoriteAudios);
                    recyclerView.setAdapter(favoriteRecyclerAdapter);
                    Audio audio = new Audio();
                }
                break;
            case 1:
                toolbar.setTitle(Title[keyword]);
                RealmResults<NewAudio> newAudios = realm.where(NewAudio.class).findAll();
                if (newAudios.size() == 0) {
                    Toast.makeText(FavoritesActivity.this, getString(R.string.list_empty), Toast.LENGTH_LONG).show();
                } else {
                    newRecyclerAdapter = new NewRecyclerAdapter(this, newAudios);
                    recyclerView.setAdapter(newRecyclerAdapter);
                }

                break;
            case 2:
                toolbar.setTitle(Title[keyword]);
                RealmResults<PartyAudio> partyAudios = realm.where(PartyAudio.class).findAll();

                if (partyAudios.size() == 0) {
                    Toast.makeText(FavoritesActivity.this, R.string.list_empty, Toast.LENGTH_LONG).show();
                } else {
                    partyRecyclerAdapter = new PartyRecyclerAdapter(this, partyAudios);
                    recyclerView.setAdapter(partyRecyclerAdapter);
                }

                break;
            case 3:
                toolbar.setTitle(Title[keyword]);
                RealmResults<SoulAudio> soulAudios = realm.where(SoulAudio.class).findAll();

                if (soulAudios.size() == 0) {
                    Toast.makeText(FavoritesActivity.this, R.string.list_empty, Toast.LENGTH_LONG).show();
                } else {
                    soulRecyclerAdapter = new SoulRecyclerAdapter(this, soulAudios);
                    recyclerView.setAdapter(soulRecyclerAdapter);
                }

                break;
            case 4:
                toolbar.setTitle(Title[keyword]);
                RealmResults<MotivationalAudio> motivationalAudios = realm.where(MotivationalAudio.class).findAll();

                if (motivationalAudios.size() == 0) {
                    Toast.makeText(FavoritesActivity.this, R.string.list_empty, Toast.LENGTH_LONG).show();
                } else {
                    motivationalRecyclerAdapter = new MotivationalRecyclerAdapter(this, motivationalAudios);
                    recyclerView.setAdapter(motivationalRecyclerAdapter);
                }

                break;
            case 5:
                toolbar.setTitle(Title[keyword]);
                RealmResults<InstrumentalAudio> instrumentalAudios = realm.where(InstrumentalAudio.class).findAll();

                if (instrumentalAudios.size() == 0) {
                    Toast.makeText(FavoritesActivity.this, R.string.list_empty, Toast.LENGTH_LONG).show();
                } else {
                    instrumentalRecyclerAdapter = new InstrumentalRecyclerAdapter(this, instrumentalAudios);
                    recyclerView.setAdapter(instrumentalRecyclerAdapter);
                }

                break;
        }
    }

    @Override
    public void OnFavClick(RealmResults<FavoriteAudio> audios, int position) {
        category = 2;
        Audio audio = new Audio();
        audioList = audio.getFavAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    @Override
    public void OnNewClick(RealmResults<NewAudio> audios, int position) {
        category = 6;
        Audio audio = new Audio();
        audioList = audio.getNewAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    @Override
    public void OnPartyClick(RealmResults<PartyAudio> audios, int position) {
        category = 7;
        Audio audio = new Audio();
        audioList = audio.getPartyAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    @Override
    public void OnSoulClick(RealmResults<SoulAudio> audios, int position) {
        category = 8;
        Audio audio = new Audio();
        audioList = audio.getSoulAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    @Override
    public void OnMotivationClick(RealmResults<MotivationalAudio> audios, int position) {
        category = 9;
        Audio audio = new Audio();
        audioList = audio.getMotivAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    @Override
    public void OnInstrumentClick(RealmResults<InstrumentalAudio> audios, int position) {
        category = 10;
        Audio audio = new Audio();
        audioList = audio.getInstAudio(audios);
        trackPosition = position;
        playerService.setAudioList(audioList);
        playAudio(position, category);
    }

    private void AddListeners() {
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
                Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
                startActivity(intent);
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

        Bitmap bitmap = utilities.getTrackThumbnail(audioList.get(trackPosition).getURL()) != null
                ? utilities.getTrackThumbnail(audioList.get(trackPosition).getURL())
                : utilities.decodeSampledBitmapFromResource(getResources(), R.drawable.audio_placeholder, 150, 150);

        trackThumbnail.setImageBitmap(bitmap);
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

        if (!realm.isClosed()) {
            realm.close();
        }

        disposable.dispose();
    }
}
