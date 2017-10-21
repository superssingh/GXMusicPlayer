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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.io.IOException;
import java.util.ArrayList;
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
    ImageButton equalizerBTN;
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
    @BindView(R.id.d_fab)
    FloatingActionButton detail_fab;
    @BindView(R.id.bassFrame)
    LinearLayout bassFrame;
    @BindView(R.id.bassSeekbar)
    SeekBar bassBoosterBTN;
    @BindView(R.id.d_Bassfab)
    FloatingActionButton bassFab;

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
    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;
    private boolean recording = false, bass = false;
    private int bassMaxStrength = 1000;
    private int category = 5;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
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
        bassBoosterBTN.setMax(bassMaxStrength);

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

        detail_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, ListActivity.class)
                        .putExtra(getString(R.string.category), category);
                startActivity(intent);

//                mFileName += "/Karaoke-" + audioList.get(trackPosition).getTITLE();
//                initRecorder();
//                showBassBoost();
            }
        });


        bassFab.setOnClickListener(new View.OnClickListener() {
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

        equalizerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!serviceBound) {
                    return;
                } else {
                    showPresets();
                }
            }
        });

        bassBoosterBTN.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
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

    private void setPlayPauseState(boolean playPauseState) {
        if (playPauseState) {
            play.setBackgroundResource(R.drawable.ic_pause_circle_filled);
        } else {
            play.setBackgroundResource(R.drawable.ic_play_circle_filled);
        }
    }

    private void UI_update(List<Audio> audioList, int trackPosition) {
        this.audioList = audioList;
        this.trackPosition = trackPosition;
        setRepeatButtonIcon(playerService.getRepeat());
        setPlayPauseState();
        bassBoosterBTN.setProgress(playerService.getBassLevel());
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
        lv.setBackground(getDrawable(R.drawable.orange_gradient));

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

    //Media Recorder implementation-------

    private void initRecorder() {
        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/gxrecording";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        LinearLayout ll = new LinearLayout(this);
        ll.setBackground(getResources().getDrawable(R.drawable.background1));
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        setContentView(ll);

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

    @Override
    protected void onStop() {
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();    //remove all items
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.list_menu) {

            return true;
        }

        return super.onOptionsItemSelected(item);
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

    class PlayButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
}
