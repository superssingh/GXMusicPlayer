//package com.santoshkumarsingh.gxmusicplayer.Utilities;
//
//import android.media.AudioManager;
//
///**
// * Created by santoshsingh on 16/09/17.
// */
//
//public class Extras {
//
//
//    @Override
//    public void onAudioFocusChange(int focusChange) {
//        switch (focusChange) {
//            case AudioManager.AUDIOFOCUS_GAIN:
//                // resume playback
//                if (mediaPlayer == null) initMediaPlayer();
//                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
//                mediaPlayer.setVolume(1.0f, 1.0f);
//                mediaSession.setActive(true);
//                mAudioFocusGranted=true;
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS:
//                // Lost focus for an unbounded amount of time: stop playback and release media player
//                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
//                mediaPlayer.release();
//                mediaPlayer = null;
//                mAudioFocusGranted=false;
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                // Lost focus for a short time, but we have to stop
//                // playback. We don't release the media player because playback
//                // is likely to resume
//                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
//                mAudioFocusGranted=true;
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                // Lost focus for a short time, but it's ok to keep playing
//                // at an attenuated level
//                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
//                mAudioFocusGranted=true;
//                break;
//        }
//    }
//
//    //------------------------------------------
//
//
////    public void updateUI(int position) {
////        Bitmap bitmap= utilities.getTrackThumbnail(audioList.get(position).getURL()).equals(null)
////                ?utilities.getTrackThumbnail(audioList.get(position).getURL())
////                : BitmapFactory.decodeResource(getResources(), R.drawable.sound_thumb);
////
////        songThumbnail.setImageBitmap(bitmap);
////        songTitle.setText(audioList.get(position).getTITLE());
////        songArtist.setText(audioList.get(position).getARTIST());
////        songDuration.setText(decimalFormat.format(
////                ((float) Integer.parseInt(audioList.get(position).getDURATION())/ 1000) / 60) + "");
////        seekBar.setMax(Integer.parseInt(audioList.get(position).getDURATION()));
////
////        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
////            @Override
////            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
////                if (b) {
////                    if (playerService.mediaPlayer.isPlaying()) {
////                        playerService.mediaPlayer.seekTo(i);
////                    }
////                }
////            }
////
////            @Override
////            public void onStartTrackingTouch(SeekBar seekBar) {
////
////            }
////
////            @Override
////            public void onStopTrackingTouch(SeekBar seekBar) {
////
////            }
////        });
////
////        seekBarCycle();
////
////    }
//
//
//}
