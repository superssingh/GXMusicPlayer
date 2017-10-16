package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.io.File;
import java.io.IOException;
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

import static com.santoshkumarsingh.gxmusicplayer.Activities.MainActivity.Broadcast_PLAY_NEW_AUDIO;

public class DetailActivity extends AppCompatActivity implements ServiceCallback {

    //--------Recorder
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static MediaPlayerService playerService;
    private static String mFileName = null;
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
    @BindView(R.id.d_CurrentTime)
    TextView currentTime;
    @BindView(R.id.d_trackAlbum)
    TextView album;
    @BindView(R.id.presetText)
    TextView presetText;
    @BindView(R.id.d_fab)
    FloatingActionButton detail_fab;
    //    @BindView(R.id.bmb)
//    BoomButton bmb;
    private Utilities utilities;
    private int trackPosition = 0;
    private List<Audio> audioList;
    private boolean serviceBound = false;
    private Intent playerIntent;
    private Bitmap bitmap;
    private CompositeDisposable disposable, disposable1, disposable2;
    private StorageUtil storageUtil;
    private Animation animation;
    private MediaRecorder mediaRecorder;
    private File audioFilePath;
    //for Equalizer-----
    private Equalizer equalizer;
    private int equalizerPresets;
    private String[] presets;
    private short val;
    private AlertDialog dialog;
    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    // service

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            playerService.setCallback(DetailActivity.this);
            serviceBound = true;
            Log.d("DetailActivity:", "Service Bound");
            UI_update(audioList, trackPosition, bitmap);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Log.d("DetailActivity:", "Service Unbound");
        }
    };

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("Playing");
        ButterKnife.bind(this);
        disposable = new CompositeDisposable();
        disposable1 = new CompositeDisposable();
        disposable2 = new CompositeDisposable();
        audioList = new ArrayList<>();
        utilities = new Utilities();
        storageUtil = new StorageUtil(this);
        playerService = new MediaPlayerService();
        mediaRecorder = new MediaRecorder();
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        d_thumbnail.setAnimation(animation);
        title.setAnimation(animation);
        artist.setAnimation(animation);
        album.setAnimation(animation);
        setClickedListeners();
        initRecorder();

        if (storageUtil.loadAudioIndex() != -1) {
            audioList = storageUtil.loadAudio();
            trackPosition = storageUtil.loadAudioIndex() == -1 ? 0 : storageUtil.loadAudioIndex();
            ConnectMediaPlayer();
        } else {
            return;
        }
    }

    private void setClickedListeners() {

        detail_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording == true) {
                    onRecord(false);
                    recording = false;
                } else {
                    onRecord(true);
                    recording = true;
                }
            }
        });

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

        repeatRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                } else {
                    showPresets();
                }
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

    private void setEqualizer() {
        disposable.add(equalizerObservale()
                .subscribeOn(Schedulers.newThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer s) throws Exception {
                        EqualizerSetup(s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Integer>() {

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Integer s) {
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Equalizer_Error-", e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("Equlizer- ", "Completed ");
                    }
                }));
    }

    private Observable<Integer> equalizerObservale() {
        return Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return equalizerPresets;
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
            d_thumbnail.setImageResource(R.drawable.audio_image);
        } else {
            d_thumbnail.setImageBitmap(bitmap);
        }

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
    }

    private void EqualizerSetup(int value) {
        equalizer = new Equalizer(0, playerService.mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(true);
        equalizer.usePreset((short) value);
        val = equalizer.getCurrentPreset();
        presetText.setText(presets[value]);
        Log.d("currentPreset: ", equalizer.getCurrentPreset() + "");
    }

    private void update_seekBar() {
        disposable1.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        int i = playerService.mediaPlayer == null ? 0 : playerService.mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(i);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        currentTime.setText(utilities.milliSecondsToTimer((long) playerService.mediaPlayer.getCurrentPosition()));
                    }
                })
                .subscribe()
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
                equalizerPresets = which;
                setEqualizer();
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }


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
        Log.d("Recording", "ON");
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Log.d("Recording", "OFF");
    }

    private void initRecorder() {
        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.mp3";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

    }

    class RecordButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }


}
