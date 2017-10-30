package com.santoshkumarsingh.gxplayer.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nightonke.boommenu.BoomMenuButton;
import com.santoshkumarsingh.gxplayer.Database.SharedPreferenceDB.StorageUtil;
import com.santoshkumarsingh.gxplayer.Models.Audio;
import com.santoshkumarsingh.gxplayer.R;
import com.santoshkumarsingh.gxplayer.Utilities.Utilities;

import java.util.List;

import be.rijckaert.tim.animatedvector.FloatingMusicActionButton;
import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;

public class DetailFragment extends Fragment {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
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
    @BindView(R.id.bassSeekbar)
    SeekBar bassSeekbar;
    @BindView(R.id.volumeSeekbar)
    SeekBar volumeSeekbar;
    @BindView(R.id.stopFrame)
    FrameLayout stopFrame;
    @BindView(R.id.d_BassBTN)
    FloatingActionButton BassBTN;

    FrameLayout bassFrame;
    View view;

    private int[] recorderIcons, recorderTitle, recordersubTitle;
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
    private int bassMaxStrength = 1000, valumeMaxStrength = 100;
    private int category = 5;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private BoomMenuButton bmb;


    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_detail, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(List<Audio> audioList, int position);
    }
}
