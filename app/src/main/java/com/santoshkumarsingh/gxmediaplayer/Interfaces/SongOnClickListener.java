package com.santoshkumarsingh.gxmediaplayer.Interfaces;

import com.santoshkumarsingh.gxmediaplayer.Models.Audio;

import java.util.List;

/**
 * Created by santoshsingh on 30/09/17.
 */

public interface SongOnClickListener {
    void OnItemClicked(List<Audio> audios, int position);
}
