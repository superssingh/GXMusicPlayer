package com.santoshkumarsingh.gxmusicplayer.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.util.Random;

/**
 * Created by santoshsingh on 31/08/17.
 */

public class Utilities {

    public Bitmap getTrackThumbnail(String URL){
        try {
            MediaMetadataRetriever metaRetriver;
            metaRetriver = new MediaMetadataRetriever();
            metaRetriver.setDataSource(URL);
            byte[] art = metaRetriver.getEmbeddedPicture();
            return BitmapFactory.decodeByteArray(art, 0, art.length);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     */
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     returns current duration in milliseconds
     * */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }


    public int randomSelection(int trackPosition, int size) {
        Random random = new Random();
        int i = random.nextInt(size);
        if (i == trackPosition && trackPosition == (size - 1)) {
            return i--;
        } else if (i != trackPosition) {
            return i;
        } else {
            return i++;
        }
    }
}
