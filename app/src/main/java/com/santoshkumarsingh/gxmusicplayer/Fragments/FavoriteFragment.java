package com.santoshkumarsingh.gxmusicplayer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.santoshkumarsingh.gxmusicplayer.Adapters.FavoriteRecyclerAdapter;
import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.FavoriteOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class FavoriteFragment extends Fragment implements FavoriteOnClickListener {

    @BindView(R.id.fav_recyclerView)
    RecyclerView recyclerView;
    private OnFragmentInteractionListener mListener;
    private View view;
    private Realm realm = null;
    private FavoriteRecyclerAdapter recyclerAdapter;

    public FavoriteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        final DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        getFavoriteList();
        return view;
    }

    public void getFavoriteList() {
        realm = Realm.getDefaultInstance();
        RealmResults<FavoriteAudio> favoriteAudios = realm.where(FavoriteAudio.class).findAll();

        if (favoriteAudios.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.NoDataFound, Toast.LENGTH_LONG).show();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            configRecycleView(favoriteAudios);
        }
    }

    private void configRecycleView(RealmResults<FavoriteAudio> results) {
        recyclerAdapter = new FavoriteRecyclerAdapter(this, results);
        recyclerView.setAdapter(recyclerAdapter);
        Log.d("OnComplete:: ", "Completed");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (!realm.isClosed()) {
            realm.close();
        }
    }

    @Override
    public void OnClick(RealmResults<FavoriteAudio> audios, int position) {
        mListener.onFavoriteFragmentInteraction(audios, position);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFavoriteList();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFavoriteFragmentInteraction(RealmResults<FavoriteAudio> audios, int position);
    }

}
