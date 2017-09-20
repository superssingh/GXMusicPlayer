package com.santoshkumarsingh.gxmusicplayer.Services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.santoshkumarsingh.gxmusicplayer.R;

/**
 * Created by santoshsingh on 20/09/17.
 */

public class Constants {
    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sound_thumb, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

    public interface ACTION {
        String MAIN_ACTION = "com.marothiatechs.customnotification.action.main";
        String INIT_ACTION = "com.marothiatechs.customnotification.action.init";
        String PREV_ACTION = "com.marothiatechs.customnotification.action.prev";
        String PLAY_ACTION = "com.marothiatechs.customnotification.action.play";
        String NEXT_ACTION = "com.marothiatechs.customnotification.action.next";
        String STARTFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
