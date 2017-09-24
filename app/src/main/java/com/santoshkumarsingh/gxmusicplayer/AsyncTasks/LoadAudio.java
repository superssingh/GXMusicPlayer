package com.santoshkumarsingh.gxmusicplayer.AsyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by santoshsingh on 22/09/17.
 */

public class LoadAudio {
    private Context context;
    private List<Audio> audios;

    public LoadAudio(Context context) {
        this.context = context;
        audios = new ArrayList<>();
    }

    public List<Audio> loadAudioFiles() {
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
