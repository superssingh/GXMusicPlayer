package com.santoshkumarsingh.gxmusicplayer.Services;

import android.graphics.Bitmap;

/**
 * Created by santoshsingh on 15/09/17.
 */

public interface ServiceCallback {
    void doSomething(int position, int duration, int currentTime, Bitmap bitmap);
}
