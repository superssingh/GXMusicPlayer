package com.santoshkumarsingh.gxmusicplayer.Interfaces;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;

import java.util.List;

/**
 * Created by santoshsingh on 30/09/17.
 */

public interface SongsListListener {
    void OnTabClicked(List<Audio> audios);
}
