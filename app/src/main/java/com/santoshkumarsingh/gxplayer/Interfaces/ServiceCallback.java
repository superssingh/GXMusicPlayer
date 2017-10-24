package com.santoshkumarsingh.gxplayer.Interfaces;

import com.santoshkumarsingh.gxplayer.Models.Audio;

import java.util.List;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 15/09/17.
 */

public interface ServiceCallback {
    void doSomething(List<Audio> audioList, int position);
}
