package com.santoshkumarsingh.gxmusicplayer.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 31/08/17.
 */

public class Utilities {
    Context context;

    public Utilities(Context context) {
        this.context = context;
    }

    // Best way to decode and compress image into bitmap
    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //-------
    public Bitmap getTrackThumbnail(String URL) {
        try {
            MediaMetadataRetriever metaRetriver;
            metaRetriver = new MediaMetadataRetriever();
            metaRetriver.setDataSource(URL);
            byte[] art = metaRetriver.getEmbeddedPicture();
            return compressBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }


    //random selection for Shuffle option
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

    public Bitmap compressBitmap(Bitmap bitmap) {

        // Display the original image on ImageView
        // Initialize a new ByteArrayStream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                /*
                    public boolean compress (Bitmap.CompressFormat format, int quality, OutputStream stream)
                        Write a compressed version of the bitmap to the specified outputstream.
                        If this returns true, the bitmap can be reconstructed by passing a
                        corresponding inputstream to BitmapFactory.decodeStream().

                        Note: not all Formats support all bitmap configs directly, so it is possible
                        that the returned bitmap from BitmapFactory could be in a different bitdepth,
                        and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque pixels).

                        Parameters
                        format : The format of the compressed image
                        quality : Hint to the compressor, 0-100. 0 meaning compress for small size,
                            100 meaning compress for max quality. Some formats,
                            like PNG which is lossless, will ignore the quality setting
                        stream: The outputstream to write the compressed data.

                        Returns
                            true if successfully compressed to the specified stream.
                */

                /*
                    Bitmap.CompressFormat
                        Specifies the known formats a bitmap can be compressed into.

                            Bitmap.CompressFormat  JPEG
                            Bitmap.CompressFormat  PNG
                            Bitmap.CompressFormat  WEBP
                */
        // Compress the bitmap with JPEG format and quality 50%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);

        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        return compressedBitmap;
        // Display the compressed bitmap in ImageView
//        iv_compressed.setImageBitmap(compressedBitmap);

    }

}
