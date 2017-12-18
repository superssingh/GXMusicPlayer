package com.santoshkumarsingh.gxmediaplayer.Interfaces;

import com.santoshkumarsingh.gxmediaplayer.Models.Audio;

import java.util.List;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 15/09/17.
 */

public interface ServiceCallback {
    void doSomething(List<Audio> audioList, int position);
}
