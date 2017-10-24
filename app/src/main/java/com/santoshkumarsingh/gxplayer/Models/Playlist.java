package com.santoshkumarsingh.gxplayer.Models;

/**
 * Created by santoshsingh on 24/10/17.
 */

public class Playlist {
    private int icon;
    private int title;

    public Playlist(int icon, int title) {
        this.icon = icon;
        this.title = title;
    }

    public Playlist() {
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }
}