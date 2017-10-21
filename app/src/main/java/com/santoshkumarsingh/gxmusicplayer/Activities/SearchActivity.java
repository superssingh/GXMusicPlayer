package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.santoshkumarsingh.gxmusicplayer.R;

public class SearchActivity extends AppCompatActivity {

    Animation animation;
    private Toolbar toolbar;
    private String keyword;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = findViewById(R.id.customToolbar);
        toolbar.setTitle("Search Browser");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.background_light));
        animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        toolbar.setAnimation(animation);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString(getString(R.string.Keyword));
        webView = findViewById(R.id.webView);
        String url = "https://www.google.com/search?q=" + keyword + " on youtube";

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }


}
