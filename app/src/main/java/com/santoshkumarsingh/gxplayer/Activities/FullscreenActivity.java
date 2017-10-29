package com.santoshkumarsingh.gxplayer.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.santoshkumarsingh.gxplayer.Models.Video;
import com.santoshkumarsingh.gxplayer.R;
import com.santoshkumarsingh.gxplayer.Utilities.FullScreenVideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.VISIBLE;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements RewardedVideoAdListener {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    FullScreenVideoView fullScreenVideoView;
    private FrameLayout customControls;
    private String videoURL;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
    };
    private VideoView videoView;
    private RewardedVideoAd mRewardedVideoAd;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(VISIBLE);
            customControls.setVisibility(VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private CompositeDisposable disposable;
    private List<Video> videoList;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        Bundle bundle = getIntent().getExtras();
        videoURL = bundle.getString(getString(R.string.videoURL));
        position = bundle.getInt(getString(R.string.videoPosition));
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullVideoView);
        customControls = findViewById(R.id.custom_controls);
        customControls.setVisibility(View.GONE);

        fullScreenVideoView = new FullScreenVideoView(this);
        videoView = findViewById(R.id.fullVideoView);
        videoList = new ArrayList<>();
        disposable = new CompositeDisposable();
        Load_VideoFiles();
        initRewardedVideo();

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FullscreenActivity.this, "Hi", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

    }

    //-------get videos fromsdcard:
    private void Load_VideoFiles() {
        disposable = new CompositeDisposable();
        disposable.add(getAudio()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<List<Video>>() {
                    @Override
                    public void accept(List<Video> videos) throws Exception {
                        videoList = videos;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Video>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Video> videos) {
                        videoPlayer(videoURL);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.e("Error::Home ", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("OnComplete:: ", "Completed");
                    }
                }));
    }

    private Observable<List<Video>> getAudio() {
        return Observable.fromCallable(new Callable<List<Video>>() {
            @Override
            public List<Video> call() throws Exception {
                return loadVideo();
            }
        });
    }

    public List<Video> loadVideo() {
        List<Video> videos = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.ALBUM + "!=0";
        String sortOrder = "LOWER(" + MediaStore.Video.Media.DISPLAY_NAME + ") ASC";
        Cursor cursor = FullscreenActivity.this.getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));

                    Video video = new Video(title, url, album, duration);
                    videos.add(video);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        if (videos == null) {
            Toast.makeText(FullscreenActivity.this, R.string.file_not_found, Toast.LENGTH_LONG).show();
        }

        return videos;
    }

    private void initRewardedVideo() {
        MobileAds.initialize(this, getString(R.string.AppID));
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(FullscreenActivity.this);
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id), new AdRequest.Builder().build());
        }
    }

    private void showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    private void videoPlayer(String URL) {
        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        mediaController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = position == videoList.size() - 1 ? 0 : position + 1;
                setVideo(videoList.get(position).getURL());
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = position == 0 ? videoList.size() - 1 : position - 1;
                setVideo(videoList.get(position).getURL());
            }
        });

        videoView.setMediaController(mediaController);

        //Setting MediaController and URI, then starting the videoView
        setVideo(URL);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                position = position == videoList.size() - 1 ? 0 : position + 1;
                setVideo(videoList.get(position).getURL());
            }
        });


    }

    private void setVideo(String URL) {
        videoView.setVideoURI(Uri.parse(URL));
        videoView.requestFocus();
        videoView.start();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        showRewardedVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
            setMargins(customControls, 0, 0, -100, 0);
        } else {
            show();
            setMargins(customControls, 0, 0, 0, 0);
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;


        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }


    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }


}
