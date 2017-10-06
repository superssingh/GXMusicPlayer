package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.santoshkumarsingh.gxmusicplayer.Activities.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class DetailActivity extends AppCompatActivity implements ServiceCallback {

    private static MediaPlayerService playerService;
    @BindView(R.id.d_play_pause)
    ImageButton play;
    @BindView(R.id.d_next)
    ImageButton next;
    @BindView(R.id.d_previous)
    ImageButton previous;
    @BindView(R.id.d_repeatOne)
    ImageButton repeatBTN;
    @BindView(R.id.d_repeatRandom)
    ImageButton repeatRandom;
    @BindView(R.id.d_seekBar)
    SeekBar seekBar;
    @BindView(R.id.d_songThumbnail)
    ImageView d_thumbnail;
    @BindView(R.id.d_trackDuration)
    TextView duration;
    @BindView(R.id.d_trackArtist)
    TextView artist;
    @BindView(R.id.d_trackTitle)
    TextView title;
    @BindView(R.id.d_songCurrentTime)
    TextView currentTime;
    @BindView(R.id.d_trackAlbum)
    TextView album;
    @BindView(R.id.d_fab)
    FloatingActionButton detail_fab;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Toolbar toolbar;
    private Bitmap bitmap;
    private CompositeDisposable disposable;
    private StorageUtil storageUtil;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private Animation animation;
    private CountDownTimer timer;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            playerService.setCallback(DetailActivity.this);
            serviceBound = true;
            Toast.makeText(DetailActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
            UI_update(audioList, trackPosition, bitmap);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Toast.makeText(DetailActivity.this, "Service Unbound", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        disposable = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities();
        storageUtil = new StorageUtil(this);
        playerService = new MediaPlayerService();
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        d_thumbnail.setAnimation(animation);
        title.setAnimation(animation);
        artist.setAnimation(animation);
        album.setAnimation(animation);

        setClickedListeners();

        if (storageUtil.loadAudioIndex() != -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();
            ConnectMediaPlayer();
        } else {
            return;
        }

    }

    private void setClickedListeners() {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    play.setBackgroundResource(R.drawable.ic_play_circle_filled);
                } else {
                    playerService.resume();
                    play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
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
                play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
                trackPosition = playerService.getAudioIndex();
                storageUtil.storeAudioIndex(trackPosition);
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
                play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
                trackPosition = playerService.getAudioIndex();
                storageUtil.storeAudioIndex(trackPosition);

            }
        });

        repeatBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                }
                repeatBTN.setAnimation(animation);
                setRepeatButtonIcon(playerService.getRepeat());
            }
        });

        repeatRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                }

                playerService.setRepeat(2);
            }
        });

    }

    private void ConnectMediaPlayer() {
        disposable.add(observable()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {
                        bitmap = utilities.getTrackThumbnail(audioList.get(s).getURL());
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
                        playAudio(s);
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

                    Log.d("Observable- ", "" + playerService.ismAudioIsPlaying() + "/ " + playerService.getRepeat());
                } while (playerService == null);

                e.onComplete();
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
        play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
    }

    @Override
    public void doSomething(List<Audio> audioList, int position, Bitmap bitmap) {
        UI_update(audioList, position, bitmap);
    }

    private void setRepeatButtonIcon(int repeat) {
        switch (repeat) {
            case 0:
                repeatBTN.setBackgroundResource(R.drawable.ic_repeat_one);
                playerService.setRepeat(1);
                break;
            case 1:
                repeatBTN.setBackgroundResource(R.drawable.ic_shuffle);
                playerService.setRepeat(2);
                break;
            case 2:
                repeatBTN.setBackgroundResource(R.drawable.ic_repeat_all);
                playerService.setRepeat(0);
                break;
        }
    }

    private void setPlayPauseState(boolean playPauseState) {
        if (playPauseState) {
            play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
        } else {
            play.setBackgroundResource(R.drawable.ic_play_circle_filled);
        }
    }

    private void UI_update(List<Audio> audioList, int trackPosition, Bitmap bitmap) {
        this.audioList = audioList;
        setRepeatButtonIcon(playerService.getRepeat());
        setPlayPauseState(playerService.ismAudioIsPlaying());
        if (bitmap == null) {
            d_thumbnail.setImageResource(R.drawable.ic_headset);
        } else {
            d_thumbnail.setImageBitmap(bitmap);
        }

        title.setText(audioList.get(trackPosition).getTITLE());
        artist.setText(audioList.get(trackPosition).getARTIST());
        album.setText(audioList.get(trackPosition).getALBUM());
        int trackDuration = Integer.parseInt(audioList.get(trackPosition).getDURATION());
        duration.setText(decimalFormat.format(((float) trackDuration / 1000) / 60) + "");

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

    private void seekBarCycle() {
        int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
        seekBar.setProgress(i);
    }

    private void currentTimeCycle() {
        int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
        currentTime.setText(decimalFormat.format(((float) i / 1000) / 60) + "");
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

//    public void timerStart(long timeLengthMilli) {
//        timer = new CountDownTimer(timeLengthMilli, 1000) {
//
//            @Override
//            public void onTick(long milliTillFinish) {
//                milliLeft = milliTillFinish;
//                min = (milliTillFinish / (1000 * 60));
//                sec = ((milliTillFinish / 1000) - min * 60);
//                clock.setText(Long.toString(min) + ":" + Long.toString(sec));
//                Log.i("Tick", "Tock");
//            }
//        };
//    }


    private void update_currentTime() {
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        aLong = Long.valueOf(playerService.mediaPlayer.getCurrentPosition());
                    }
                })
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Long aLong) {
                        currentTime.setText(decimalFormat.format(((float) aLong / 1000) / 60) + "");
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
        unbindService(serviceConnection);
        disposable.dispose();
    }
}
