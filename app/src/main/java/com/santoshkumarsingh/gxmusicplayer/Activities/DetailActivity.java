package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.OnBoomListener;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import be.rijckaert.tim.animatedvector.FloatingMusicActionButton;
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

import static com.santoshkumarsingh.gxmusicplayer.Activities.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class DetailActivity extends AppCompatActivity implements ServiceCallback {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static MediaPlayerService playerService;
    private static String mFileName = null;
    @BindView(R.id.d_play_pause)
    FloatingMusicActionButton play;
    @BindView(R.id.d_next)
    ImageButton next;
    @BindView(R.id.d_previous)
    ImageButton previous;
    @BindView(R.id.d_repeatOne)
    ImageButton repeatBTN;
    @BindView(R.id.d_equalizer)
    ImageButton d_equalizer;
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
    @BindView(R.id.d_CurrentTime)
    TextView currentTime;
    @BindView(R.id.d_trackAlbum)
    TextView album;
    @BindView(R.id.stopFab)
    ImageButton stopFab;
    @BindView(R.id.bassFrame)
    LinearLayout bassFrame;
    @BindView(R.id.bassSeekbar)
    SeekBar bassSeekbar;
    @BindView(R.id.stopFrame)
    FrameLayout stopFrame;
    @BindView(R.id.d_BassBTN)
    FloatingActionButton BassBTN;
    int[] recorderIcons, recorderTitle, recordersubTitle;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private CompositeDisposable disposable, disposable1, disposable2;
    private StorageUtil storageUtil;
    private Animation animation, animation1;
    private String[] presets;
    private AlertDialog dialog;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean recording = false, bass = false;
    private int bassMaxStrength = 1000;
    private int category = 5;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private BoomMenuButton bmb;
    private ArrayList<Pair> piecesAndButtons = new ArrayList<>();
    // Media PlayerService-------
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            playerService.setCallback(DetailActivity.this);
            serviceBound = true;
            Log.d("DetailActivity:", "Service Bound");
            UI_update(audioList, trackPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d("DetailActivity:", "Service Unbound");
        }
    };

    private RippleBackground rippleBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        bmb = findViewById(R.id.bmb);
        rippleBackground = findViewById(R.id.content);
        disposable = new CompositeDisposable();
        disposable1 = new CompositeDisposable();
        disposable2 = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities(getApplicationContext());
        storageUtil = new StorageUtil(this);
        playerService = new MediaPlayerService();
        bassFrame.setAnimation(animation);
        play.playAnimation();
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animation1 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        d_thumbnail.setAnimation(animation);
        title.setAnimation(animation);
        artist.setAnimation(animation);
        album.setAnimation(animation);
        bassSeekbar.setMax(bassMaxStrength);
        initRecorder();
        initBoomMemu();
        setClickedListeners();

        if (storageUtil.loadAudioIndex() != -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();
            ConnectMediaPlayer();
        } else {
            return;
        }
    }

    private void initBoomMemu() {

        assert bmb != null;

        recorderIcons = new int[]{R.drawable.ic_mic_black_24dp, R.drawable.ic_record_voice_over_black_24dp,
                R.drawable.ic_hearing__24dp, R.drawable.ic_playlist_play_24dp};
        recorderTitle = new int[]{R.string.record_without_song, R.string.record_with_song,
                R.string.listen_karaoke, R.string.current_playlist};
        recordersubTitle = new int[]{R.string.record_without_song_sub, R.string.record_with_song_sub,
                R.string.listen_karaoke_sub, R.string.current_playlist_sub};

        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder()
                    .normalImageDrawable(getResources().getDrawable(recorderIcons[i]))
                    .normalTextRes(recorderTitle[i])
                    .subNormalTextRes(recordersubTitle[i]);
            bmb.addBuilder(builder);
        }

        bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                if (index == 0) {
                    recording = true;
                    if (playerService.mediaPlayer.isPlaying()) {
                        playerService.pause();
                        setPlayPauseState();
                    }

                    rippleBackground.startRippleAnimation();
                    onRecord(recording);
                    BassBTN.setVisibility(View.GONE);
                    stopFab.setBackgroundResource(R.drawable.ic_mic_black_24dp);
                    stopFrame.setVisibility(View.VISIBLE);
                } else if (index == 1) {
                    recording = true;
                    if (!playerService.mediaPlayer.isPlaying()) {
                        playerService.resume();
                        setPlayPauseState();
                    }

                    rippleBackground.startRippleAnimation();
                    onRecord(recording);
                    BassBTN.setVisibility(View.GONE);
                    stopFab.setBackgroundResource(R.drawable.ic_mic_black_24dp);
                    stopFrame.setVisibility(View.VISIBLE);
                    Toast.makeText(DetailActivity.this, "Start Recording", Toast.LENGTH_LONG).show();

                } else if (index == 2) {
                    recording = false;
                    startPlaying();
                    stopFab.setBackgroundResource(R.drawable.ic_stop_24dp);
                    stopFrame.setVisibility(View.VISIBLE);
                    Toast.makeText(DetailActivity.this, "Start playing.", Toast.LENGTH_LONG).show();

                } else if (index == 3) {
                    Intent intent = new Intent(DetailActivity.this, ListActivity.class)
                            .putExtra(getString(R.string.category), category);
                    startActivity(intent);
                    Toast.makeText(DetailActivity.this, "Open", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });

    }

    private void setClickedListeners() {

        BassBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bass) {
                    bassFrame.setAnimation(animation1);
                    bassFrame.setVisibility(View.GONE);
                    bass = false;
                } else {
                    bassFrame.setAnimation(animation);
                    bassFrame.setVisibility(View.VISIBLE);
                    bass = true;
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.ismAudioIsPlaying()) {
                    playerService.pause();
                    setPlayPauseState();
                } else {
                    playerService.resume();
                    setPlayPauseState();
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
                switch (playerService.getRepeat()) {
                    case 0:
                        playerService.setRepeat(1);
                        break;
                    case 1:
                        playerService.setRepeat(2);
                        break;
                    case 2:
                        playerService.setRepeat(0);
                        break;
                }

                setRepeatButtonIcon(playerService.getRepeat());
            }
        });


        bassSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playerService.mediaPlayer != null) {
                    setBass((short) progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timer();
            }
        });

        stopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    stopFab.setBackgroundResource(R.drawable.ic_mic_black_24dp);
                    onRecord(false);
                    rippleBackground.stopRippleAnimation();
                    BassBTN.setVisibility(View.VISIBLE);
                    Snackbar.make(v, "Recording Stop", Snackbar.LENGTH_LONG).show();
                } else {
                    stopFab.setBackgroundResource(R.drawable.ic_stop_24dp);
                    onPlay(false);
                    BassBTN.setVisibility(View.VISIBLE);
                    Snackbar.make(v, "Record playing Stop", Snackbar.LENGTH_LONG).show();
                }

                stopFrame.setVisibility(View.GONE);
            }
        });

        d_equalizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPresets();
            }
        });

    }

    private void timer() {
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                bassFrame.setAnimation(animation1);
                bass = false;
            }

        }.start();
    }


    private void setPlayPauseState() {
        if (playerService.mediaPlayer != null) {
            if (!playerService.mediaPlayer.isPlaying()) {
                play.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE);
            } else {
                play.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY);
            }
        }
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
                        playAudio(integer);
                        setPlayPauseState();
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
    }

    @Override
    public void doSomething(List<Audio> audioList, int position) {
        UI_update(audioList, position);
    }

    private void setRepeatButtonIcon(int repeat) {
        switch (repeat) {
            case 0:
                repeatBTN.setBackgroundResource(R.drawable.ic_repeat_all);
                break;
            case 1:
                repeatBTN.setBackgroundResource(R.drawable.ic_repeat_one);
                break;
            case 2:
                repeatBTN.setBackgroundResource(R.drawable.ic_shuffle);
                break;
        }
    }

    private void UI_update(List<Audio> audioList, int trackPosition) {
        this.audioList = audioList;
        this.trackPosition = trackPosition;
        setRepeatButtonIcon(playerService.getRepeat());
        setPlayPauseState();
        bassSeekbar.setProgress(playerService.getBassLevel());
        Glide.with(DetailActivity.this)
                .asBitmap()
                .load(utilities.getImageIntoByteArray(audioList.get(trackPosition).getURL()))
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.ic_audiotrack))
                .transition(GenericTransitionOptions.with(R.anim.fade_in))
                .into(d_thumbnail);
        title.setText(audioList.get(trackPosition).getTITLE());
        artist.setText(audioList.get(trackPosition).getARTIST());
        album.setText(audioList.get(trackPosition).getALBUM());
        duration.setText(utilities.milliSecondsToTimer((long) Integer.parseInt(audioList.get(trackPosition).getDURATION())));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        disposable.dispose();
        disposable1.dispose();
        disposable2.dispose();
        audioList = null;
    }

    private void update_seekBar() {
        disposable1.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (playerService.mediaPlayer != null) {
                            int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(i);
                            currentTime.setText(utilities.milliSecondsToTimer((long) playerService.mediaPlayer.getCurrentPosition()));
                        } else {
                            seekBar.setProgress(0);
                            currentTime.setText("0:0");
                        }
                    }
                })
                .subscribeWith(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Long aLong) {

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("seekBar: ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    private void showPresets() {
        presets = getResources().getStringArray(R.array.PRESETS);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.preset_layout, null);
        builder.setView(convertView);
        builder.setTitle(R.string.Equalizer);
        ListView lv = convertView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DetailActivity.this, android.R.layout.simple_list_item_1, presets);
        lv.setAdapter(adapter);

        builder.setItems(presets, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setEqualizer((short) which);
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    private void setEqualizer(Short level) {
        playerService.setPresetLevel(level);
    }

    private void setBass(Short level) {
        playerService.setBassLevel(level);
    }

    //---------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void initRecorder() {
        Date date = new Date();
        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/karaoke" + date.getTime() + ".3gp";
        Log.i("filePath:", mFileName);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
