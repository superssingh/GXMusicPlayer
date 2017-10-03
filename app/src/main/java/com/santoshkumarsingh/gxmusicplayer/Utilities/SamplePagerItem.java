package com.santoshkumarsingh.gxmusicplayer.Utilities;

/**
 * Created by santoshsingh on 30/09/17.
 */

public class SamplePagerItem {
    private String Title;
    private int IndicatorColor, DividerColor;

    public SamplePagerItem(String tab, int green, int yellow) {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getIndicatorColor() {
        return IndicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        IndicatorColor = indicatorColor;
    }

    public int getDividerColor() {
        return DividerColor;
    }

    public void setDividerColor(int dividerColor) {
        DividerColor = dividerColor;
    }
}
