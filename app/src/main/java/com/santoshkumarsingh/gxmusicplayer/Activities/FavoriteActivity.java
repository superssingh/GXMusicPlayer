package com.santoshkumarsingh.gxmusicplayer.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.FavoriteRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.FavoriteOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class FavoriteActivity extends AppCompatActivity implements FavoriteOnClickListener {

    @BindView(R.id.fav_recyclerView)
    RecyclerView recyclerView;
    private Realm realm;
    private RealmConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        ButterKnife.bind(this);
        Realm.init(this);
        config = new RealmConfiguration.Builder()
                .name(getString(R.string.RealmDatabaseName))
                .schemaVersion(Integer.parseInt(getString(R.string.VERSION)))
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        getFavoriteList();
    }

    private void getFavoriteList() {
        realm = Realm.getDefaultInstance();
        RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class).findAll();
        if (favoriteAudios.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), R.string.NoDataFound, Toast.LENGTH_LONG).show();
        } else {
            configRecycleView(favoriteAudios);
        }

    }

    private void configRecycleView(RealmResults<FavoriteAudio> results) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
//        AutofitGridlayout layoutManager = new AutofitGridlayout(this, Integer.parseInt(getString(R.string.Image_Width)));
        FavoriteRecyclerAdapter recyclerAdapter = new FavoriteRecyclerAdapter(this, results);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void OnClick(ImageButton optionButton, View view, Bitmap bitmap, String URL, int position) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
