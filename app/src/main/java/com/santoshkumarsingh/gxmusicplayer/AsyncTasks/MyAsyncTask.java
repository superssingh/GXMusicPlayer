package com.santoshkumarsingh.gxmusicplayer.AsyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;

import java.util.ArrayList;
import java.util.List;

public class MyAsyncTask extends AsyncTask<String, Void, List<Audio>> {

    List<Audio> audios = new ArrayList<>();
    private Context context;
    private AsyncResponse listener = null;

    public MyAsyncTask(Context context, AsyncResponse listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected List<Audio> doInBackground(String... urls) {
        try {
            while (isCancelled()) {
                return null;
            }

            return loadAudioFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled(List<Audio> audioList) {
        super.onCancelled(audioList);
    }

    @Override
    protected void onPostExecute(List<Audio> audioList) {
        listener.processFinish(audioList);
    }

    private List<Audio> loadAudioFiles() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    Audio audio = new Audio(title, artist, url, album, duration);
                    audios.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return audios;
    }
}