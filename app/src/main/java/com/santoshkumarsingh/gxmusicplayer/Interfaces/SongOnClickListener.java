package com.santoshkumarsingh.gxmusicplayer.Interfaces;

import android.graphics.Bitmap;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;

import java.util.List;

/**
 * Created by santoshsingh on 30/09/17.
 */

public interface SongOnClickListener {
    void OnItemClicked(List<Audio> audios, int position, Bitmap bitmap);
}
