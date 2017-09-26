package com.santoshkumarsingh.gxmusicplayer.Interfaces;

import android.graphics.Bitmap;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 15/09/17.
 */

public interface ServiceCallback {
    void doSomething(int position, int duration, int currentTime, Bitmap bitmap);
}
