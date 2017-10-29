package com.santoshkumarsingh.gxplayer.Database.SharedPreferenceDB;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.santoshkumarsingh.gxplayer.Models.Audio;
import com.santoshkumarsingh.gxplayer.Models.Video;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 08/09/17.
 */

public class StorageUtil {

    private final String STORAGE = "com.santoshkumarsingh.gxmusicplayer.STORAGE";
    private final Context context;
    private SharedPreferences preferences;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(List<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -a if no data found
    }

    public void storeAudioPlayerStopped(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioCurrentPosition", index);
        editor.apply();
    }

    public int loadAudioPlayerStopped() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioCurrentPosition", 0);//return -a if no data found
    }

    public int loadCategoryIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("categoryIndex", -1);//return -a if no data found
    }

    public void storeCategoryIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("categoryIndex", index);
        editor.apply();
    }


    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public void storeVideo(List<Video> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("videoArrayList", json);
        editor.apply();
    }

    public ArrayList<Video> loadVideo() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("videoArrayList", null);
        Type type = new TypeToken<ArrayList<Video>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeVideoIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("videoIndex", index);
        editor.apply();
    }

    public int loadVideoIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("videoIndex", -1);//return -a if no data found
    }

    public void clearCachedVideoPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


}

