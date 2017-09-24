package com.santoshkumarsingh.gxmusicplayer.AsyncTasks;

import com.santoshkumarsingh.gxmusicplayer.Models.Audio;

import java.util.List;

public interface AsyncResponse {
    void processFinish(List<Audio> audioList);
}
