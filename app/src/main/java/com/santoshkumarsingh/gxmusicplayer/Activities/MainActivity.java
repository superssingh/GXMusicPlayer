package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.AudioAdapter;
import com.santoshkumarsingh.gxmusicplayer.Classes.StorageUtil;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Services.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.santoshkumarsingh.gxmusicplayer.PlayNewAudio";

    @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
    @BindView(R.id.play_pause)
    ImageButton play_pause;
    @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
    @BindView(R.id.previous)
    ImageButton previous;
    @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
    @BindView(R.id.next)
    ImageButton next;
    @BindView(R.id.songThumbnail)
    ImageView songThumbnail;
    @BindView(R.id.trackTitle)
    TextView songTitle;
    @BindView(R.id.trackArtist)
    TextView songArtist;
    @BindView(R.id.trackDuration)
    TextView songDuration;
    @SuppressWarnings({"CanBeFinal", "WeakerAccess"})
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private int trackPosition = 0, repeat = 0;
    private List<Audio> audioList;
    private LinearLayoutManager linearLayoutManager;
    private AudioAdapter audioAdapter;

    private MediaPlayerService playerService;
    private boolean serviceBound = false;
    private Intent playerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        audioList = new ArrayList<>();
        playerService = new MediaPlayerService();

//        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
//        am.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        //************----------------------************
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //*******--------------------------------------------******
        checkPermission();
        configRecycleView();

        // end audio focus ---------
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeat == 0) {
                    repeat = 1;
                    playerService.setRepeat(1);
                    fab.setImageResource(R.drawable.ic_repeat_one);
                } else if (repeat == 1) {
                    repeat = 2;
                    playerService.setRepeat(2);
                    fab.setImageResource(R.drawable.ic_repeat_all);
                } else if (repeat==2){
                    repeat = 3;
                    playerService.setRepeat(3);
                    fab.setImageResource(R.drawable.ic_shuffle);
                }else{
                    repeat=0;
                    playerService.setRepeat(0);
                    fab.setImageResource(R.drawable.ic_foward_stop);
                }
            }
        });

        playerService.setAudioList(audioList);

        if (!serviceBound){
            StorageUtil storageUtil=new StorageUtil(getApplicationContext());
            if (storageUtil.loadAudioIndex()>=0) {
                playAudio(storageUtil.loadAudioIndex());
            }
        }

    }


    //---------------------------
    private void configRecycleView() {
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        audioAdapter = new AudioAdapter(this);
        audioAdapter.addSongs(audioList);
        audioAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(audioAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        audioAdapter.setOnClickListener(new AudioAdapter.SongOnClickListener() {
            @Override
            public void OnClick(ImageButton button, View view, Bitmap bitmap, String URL, int position) {
                trackPosition = position;
                playAudio(position);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackPosition == 0) {
                    play_pause.setBackgroundResource(R.drawable.ic_pause);
                    playAudio(0);
//                } else if () {
//                    Log.i("playing", "play");
////                    mediaPlayer.stop();
//
//                    play_pause.setBackgroundResource(R.drawable.ic_play);
                } else {
                    Log.i("pause", "pause");
//                    mediaPlayer.start();
                    play_pause.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = trackPosition == 0 ? audioList.size() - 1 : trackPosition - 1;
//                playSong(i);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = trackPosition == audioList.size() - 1 ? 0 : trackPosition + 1;
//                playSong(i);
            }
        });


    }

    //------

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            Toast.makeText(MainActivity.this, "Service Unbound", Toast.LENGTH_SHORT).show();
        }
    };


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

//            //Service is active
//            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
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


    //-----------
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 24);
                return;
            }
        }
        loadAudioFiles();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 24:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadAudioFiles();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), LENGTH_LONG).show();
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void loadAudioFiles() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = this.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    Audio audio = new Audio(title, artist, url, duration);
                    audioList.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

        if (id == R.id.nav_exit) {
            stopService(playerIntent);
            playerService.stopSelf();

            audioList.clear();
            System.exit(0);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
