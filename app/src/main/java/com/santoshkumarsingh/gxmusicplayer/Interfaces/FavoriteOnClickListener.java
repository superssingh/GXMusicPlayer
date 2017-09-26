package com.santoshkumarsingh.gxmusicplayer.Interfaces;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by santoshsingh on 26/09/17.
 */

public interface FavoriteOnClickListener {
    void OnClick(ImageButton optionButton, View view, Bitmap bitmap, String URL, int position);
}