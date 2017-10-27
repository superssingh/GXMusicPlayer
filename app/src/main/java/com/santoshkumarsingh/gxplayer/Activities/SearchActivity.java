package com.santoshkumarsingh.gxplayer.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.santoshkumarsingh.gxplayer.R;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String keyword;
    private WebView webView;
    private AdView mAdView;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = findViewById(R.id.customToolbar);
        toolbar.setTitle("Search Browser");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.background_light));
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString(getString(R.string.Keyword));
        webView = findViewById(R.id.webView);
        initAds();
        String url = "https://www.google.com/search?q=" + keyword + " on youtube";
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    private void initAds() {
        MobileAds.initialize(this, getString(R.string.AppID));
        mAdView = findViewById(R.id.adView);
        adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

}
