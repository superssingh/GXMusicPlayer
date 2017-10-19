package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.FullScreenVideoView;

public class VideoActivity extends AppCompatActivity {

    FullScreenVideoView fullScreenVideoView;
    VideoView videoView;
    private String videoURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        Bundle bundle = getIntent().getExtras();
        videoURL = bundle.getString(getString(R.string.videoURL));
        videoView = findViewById(R.id.fullVideoView);

        fullScreenVideoView = new FullScreenVideoView(this);
        videoPlayer(videoURL);

//        playerView = (SimpleExoPlayerView) findViewById(R.id.exoPlayerView);
//        initializePlayer(videoURL);
    }

    private void videoPlayer(String URL) {
        videoView = findViewById(R.id.fullVideoView);

        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(URL));
        videoView.requestFocus();
        videoView.start();
    }

//    private void initializePlayer(String url) {
//        player = ExoPlayerFactory.newSimpleInstance(
//                new DefaultRenderersFactory(this),
//                new DefaultTrackSelector(), new DefaultLoadControl());
//
//        playerView.setPlayer(player);
//
//        player.setPlayWhenReady(playWhenReady);
//        player.seekTo(currentWindow, playbackPosition);
//
//        Uri uri = Uri.parse(url);
//        MediaSource mediaSource = buildMediaSource(uri);
//        player.prepare(mediaSource, true, false);
//
//    }
//
//
//    private MediaSource buildMediaSource(Uri uri) {
//        return new ExtractorMediaSource(uri,
//                new DefaultHttpDataSourceFactory("ua"),
//                new DefaultExtractorsFactory(), null, null);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (Util.SDK_INT > 23) {
////            initializePlayer(videoURL);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        hideSystemUi();
//        if ((Util.SDK_INT <= 23 || player == null)) {
////            initializePlayer(videoURL);
//
//        }
//    }
//
//    @SuppressLint("InlinedApi")
//    private void hideSystemUi() {
//        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (Util.SDK_INT > 23) {
//            releasePlayer();
//        }
//    }
//
//    private void releasePlayer() {
//        if (player != null) {
//            playbackPosition = player.getCurrentPosition();
//            currentWindow = player.getCurrentWindowIndex();
//            playWhenReady = player.getPlayWhenReady();
//            player.release();
//            player = null;
//        }
//    }

//
//    private MediaSource buildMediaSource(Uri uri) {
//        // these are reused for both media sources we create below
//        DefaultExtractorsFactory extractorsFactory =
//                new DefaultExtractorsFactory();
//        DefaultHttpDataSourceFactory dataSourceFactory =
//                new DefaultHttpDataSourceFactory( "user-agent");
//
//        ExtractorMediaSource videoSource =
//                new ExtractorMediaSource(uri, dataSourceFactory,
//                        extractorsFactory, null, null);
//
//        Uri audioUri = Uri.parse(getString(R.string.media_url_mp3));
//        ExtractorMediaSource audioSource =
//                new ExtractorMediaSource(audioUri, dataSourceFactory,
//                        extractorsFactory, null, null);
//
//        return new ConcatenatingMediaSource(audioSource, videoSource);
//    }

}
