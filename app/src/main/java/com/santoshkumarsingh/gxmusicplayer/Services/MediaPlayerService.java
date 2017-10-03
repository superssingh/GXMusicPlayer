package com.santoshkumarsingh.gxmusicplayer.Services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.santoshkumarsingh.gxmusicplayer.Activities.MainActivity;
import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.ServiceCallback;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

import static android.content.ContentValues.TAG;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 07/09/17.
 */

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String ACTION_PLAY = "com.santoshkumarsingh.gxmusicplayer.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.santoshkumarsingh.gxmusicplayer.ACTION_PAUSE";
    private static final String ACTION_PREVIOUS = "com.santoshkumarsingh.gxmusicplayer.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.santoshkumarsingh.gxmusicplayer.ACTION_NEXT";
    private static final String ACTION_STOP = "com.santoshkumarsingh.gxmusicplayer.ACTION_STOP";
    private static final String CMD_NAME = "command";
    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    private static MediaPlayerService sInstance = null;
    private static String SERVICE_CMD = "com.santoshkumarsingh.gxmusicplayer.ACTION_SERVICE_COMMAND";
    //    private static String SERVICE_CMD = "com.sec.android.app.music.musicservicecommand";
    private final IBinder iBinder = new LocalBinder();
    // global variable
    public MediaPlayer mediaPlayer;
    RealmResults<FavoriteAudio> audios;
    private Context mContext;
    private AudioManager audioManager;
    //Used to pause/resume MediaPlayer
    private int resumePosition, repeat = 0;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    //List of available Audio files
    private List<Audio> audioList = new ArrayList<>();
    private int audioIndex = -1;
    private Audio activeAudio; //an object of the currently playing audio
    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private Utilities utilities;
    private Bitmap albumArt;
    private ServiceCallback serviceCallback;
    private boolean mAudioIsPlaying = false;
    private boolean mAudioFocusGranted = false;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private boolean mReceiverRegistered = false;
    //BroadcastReceiver for Becoming noisy (any other interruption)
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pause();
        }
    };

    //----------BroadcastReceiver to play new audio
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mediaPlayer.reset();
            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            mediaPlayer.stop();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    public MediaPlayerService() {
        super();
    }

    private MediaPlayerService(Context context) {
        mContext = context;
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @SuppressLint("LongLogTag")
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        playMedia();
                        mediaSession.setActive(true);
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        resume();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        mAudioFocusGranted = false;
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        mediaPlayer.start();
                        mAudioFocusGranted = true;
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                        mAudioFocusGranted = true;
                        break;
                    default:
                        //
                }
            }
        };

    }

    public static MediaPlayerService getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MediaPlayerService(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioList = new ArrayList<>();
        activeAudio = new Audio();
        utilities = new Utilities();
        mediaPlayer = new MediaPlayer();
        // Perform one-time setup procedures
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    private void initMediaPlayer() {
        resumePosition = 0;
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        if (activeAudio != null) {
            //Reset so that the MediaPlayer is not pointing to another data source
            mediaPlayer.reset();
            try {
                // Set the data source to the mediaFile location
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(activeAudio.getURL());
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
            mediaPlayer.prepareAsync();
        }
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public void playMedia() {
        if (mediaPlayer == null) {
            initMediaPlayer();
            mediaPlayer.setVolume(1.0f, 1.0f);
        }

        mediaPlayer.start();
        mAudioIsPlaying = true;
        buildNotification(PlaybackStatus.PAUSED);

    }

    public void play() {
        if (!mAudioIsPlaying) {
            if (mediaPlayer == null) {
                initMediaPlayer();
            }

            // 1. Acquire audio focus
            if (!mAudioFocusGranted && requestAudioFocus()) {
                // 2. Kill off any other play back sources
                forceMusicStop();
                // 3. Register broadcast receiver for player intents
                setupBroadcastReceiver();
            }
            // 4. Play music
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PAUSED);
            mAudioIsPlaying = true;
        }
    }

    public void stopMedia() {
        // 1. Stop play back
        if (mediaPlayer.isPlaying() || mediaPlayer != null) {
            mediaPlayer.stop();
            mAudioIsPlaying = false;
            // 2. Give up audio focus
            abandonAudioFocus();
        }

    }

    public void pause() {
        // 1. Suspend play back
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            mAudioIsPlaying = false;
            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            mAudioIsPlaying = true;
            buildNotification(PlaybackStatus.PAUSED);

        }
    }

    private void initMediaSession() {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        //noinspection EmptyMethod
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resume();
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        // replace with medias albumArt
        albumArt = (utilities.getTrackThumbnail(audioList.get(audioIndex).getURL()) != null ?
                utilities.getTrackThumbnail(activeAudio.getURL())
                : BitmapFactory.decodeResource(getResources(), R.drawable.ic_headset));
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getARTIST())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getALBUM())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTITLE())
                .build());

    }

    public void skipToNext() {
        audioIndex = (audioIndex == audioList.size() - 1 ? 0 : audioIndex + 1);
        activeAudio = audioList.get(audioIndex);
        resumePosition = 0;
        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        mediaPlayer.stop();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        //update notification
        updateMetaData();
        buildNotification(PlaybackStatus.PAUSED);
    }

    public void skipToPrevious() {
        audioIndex = (audioIndex == 0 ? audioList.size() - 1 : audioIndex - 1);
        activeAudio = audioList.get(audioIndex);
        resumePosition = 0;
        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        mediaPlayer.stop();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        //update notification
        updateMetaData();
        buildNotification(PlaybackStatus.PAUSED);
    }

    private void randomSelection() {
        audioIndex = utilities.randomSelection(audioIndex, audioList.size());
        activeAudio = audioList.get(audioIndex);
        resumePosition = 0;
        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        mediaPlayer.stop();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        //update notification
        updateMetaData();
        buildNotification(PlaybackStatus.PAUSED);
    }

    private void buildNotification(PlaybackStatus playbackStatus) {


        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;
        //Build a new notification according to the current state of the MediaPlayer
        switch (playbackStatus) {
            case PLAYING:
                notificationAction = android.R.drawable.ic_media_pause;
                //create the pause action
                play_pauseAction = playbackAction(1);
                break;
            case PAUSED:
                notificationAction = android.R.drawable.ic_media_play;
                //create the play action
                play_pauseAction = playbackAction(0);
                break;
        }
//        if (playbackStatus.equals(PlaybackStatus.PLAYING)) {
//            notificationAction = android.R.drawable.ic_media_pause;
//            //create the pause action
//            play_pauseAction = playbackAction(1);
//        } else {
//            notificationAction = android.R.drawable.ic_media_play;
//            //create the play action
//            play_pauseAction = playbackAction(0);
//        }

        albumArt = (utilities.getTrackThumbnail(audioList.get(audioIndex).getURL()) != null
                ? utilities.getTrackThumbnail(activeAudio.getURL())
                : BitmapFactory.decodeResource(getResources(), R.drawable.ic_audiotrack));
        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setColorized(true)
                // Set the large and small icons
                .setLargeIcon(albumArt)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getARTIST())
                .setContentTitle(activeAudio.getALBUM())
                .setContentInfo(activeAudio.getTITLE())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(getApplicationContext(), MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            initMediaSession();
            initMediaPlayer();
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pause();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resume();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    //--------------
    // Binder given to clients
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        switch (repeat) {
            case 0:
//                stopMedia();
                skipToNext();
//                playMedia();
//                mp.start();
                break;
            case 1:
                mp.start();
                break;
            case 2:
                randomSelection();
                mp.start();
                break;
        }
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
        if (serviceCallback != null) {
            serviceCallback.doSomething(audioIndex, albumArt);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }

    public int getAudioIndex() {
        return audioIndex;
    }

    public void setCallback(ServiceCallback callback) {
        serviceCallback = callback;
    }

    @SuppressLint("LongLogTag")
    private boolean requestAudioFocus() {
        if (!mAudioFocusGranted) {
            if (mContext == null) {
                mContext = getApplicationContext();
            }

            audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // Request audio focus for play back
                int result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mAudioFocusGranted = true;
                } else {
                    // FAILED
                    Log.e(TAG, "AUDIO FOCUS FAILED");
                }
            } else {
                mAudioFocusGranted = false;
            }
        }

        return mAudioFocusGranted;
    }

    @SuppressLint("LongLogTag")
    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int result = am.abandonAudioFocus(mOnAudioFocusChangeListener);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocusGranted = false;
        } else {
            Log.e(TAG, "AUDIO FOCUS ABANDONED");
        }
        mOnAudioFocusChangeListener = null;
    }

    private void setupBroadcastReceiver() {
        BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @SuppressLint("LongLogTag")
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra(CMD_NAME);
                Log.i(TAG, "mIntentReceiver.onReceive " + action + " / " + cmd);

                if (ACTION_PAUSE.equals(action) || (SERVICE_CMD.equals(action) && ACTION_PAUSE.equals(cmd))) {
                    play();
                }

                if (ACTION_PLAY.equals(action) || (SERVICE_CMD.equals(action) && ACTION_PLAY.equals(cmd))) {
                    pause();
                }
            }
        };

        // Do the right thing when something else tries to play
        if (!mReceiverRegistered) {
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(SERVICE_CMD);
            commandFilter.addAction(ACTION_PAUSE);
            commandFilter.addAction(ACTION_PLAY);
            mContext.registerReceiver(mIntentReceiver, commandFilter);
            mReceiverRegistered = true;
        }
    }

    private void forceMusicStop() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am.isMusicActive()) {
            Intent intentToStop = new Intent(SERVICE_CMD);
            intentToStop.putExtra(CMD_NAME, ACTION_STOP);
            mContext.sendBroadcast(intentToStop);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer == null) {
        } else {
            stopMedia();
            mediaPlayer.release();
            audioList.clear();
            activeAudio = null;
            removeAudioFocus();
            //Disable the PhoneStateListener
            if (phoneStateListener != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }

            //unregister BroadcastReceivers
            unregisterReceiver(becomingNoisyReceiver);
            unregisterReceiver(playNewAudio);
            removeNotification();

            //Forced Stop any mediaplayer
            forceMusicStop();
        }
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public boolean ismAudioIsPlaying() {
        return mAudioIsPlaying;
    }

    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

}