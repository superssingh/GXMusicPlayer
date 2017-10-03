package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.santoshkumarsingh.gxmusicplayer.R;

import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }


}
