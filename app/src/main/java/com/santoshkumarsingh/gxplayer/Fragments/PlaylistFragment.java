package com.santoshkumarsingh.gxplayer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.santoshkumarsingh.gxplayer.Adapters.PlaylistsRecyclerAdapter;
import com.santoshkumarsingh.gxplayer.Interfaces.ItemOnClickListener;
import com.santoshkumarsingh.gxplayer.Models.Playlist;
import com.santoshkumarsingh.gxplayer.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistFragment extends Fragment implements ItemOnClickListener {

    @BindView(R.id.playlist_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.adView)
    AdView adView;
    PlaylistsRecyclerAdapter recyclerAdapter;
    private OnPlaylistFragmentInteractionListener mListener;
    private View view;

    public PlaylistFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlist, container, false);
        ButterKnife.bind(this, view);
        configRecycleView(getList());
        initAds();
        return view;
    }

    private void initAds() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(getActivity(), getString(R.string.AppID));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }


    private List<Playlist> getList() {
        List<Playlist> playlists = new ArrayList<>();
        int[] ICON = new int[]{R.drawable.heart, R.drawable.red, R.drawable.audio_placeholder,
                R.drawable.aqua, R.drawable.orange_placehonder, R.drawable.blue};

        int[] Title = new int[]{R.string.favorite, R.string.NewAudio, R.string.PartyAudio,
                R.string.RelaxedAudio, R.string.Motivational, R.string.Instrumental};

        for (int i = 0; i < ICON.length; i++) {
            Playlist playlist = new Playlist(ICON[i], Title[i]);
            playlists.add(playlist);
        }

        return playlists;
    }

    private void configRecycleView(List<Playlist> playlists) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        recyclerAdapter = new PlaylistsRecyclerAdapter(this, playlists);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaylistFragmentInteractionListener) {
            mListener = (OnPlaylistFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            configRecycleView(getList());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void OnItemClick(String ID) {
        mListener.onPlaylistFragmentInteraction(Integer.parseInt(ID));
    }

    public interface OnPlaylistFragmentInteractionListener {
        void onPlaylistFragmentInteraction(int id);
    }


}
