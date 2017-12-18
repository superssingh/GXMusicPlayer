package com.santoshkumarsingh.gxmediaplayer.Adapters.PlaylistsAdapters;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.santoshkumarsingh.gxmediaplayer.Database.RealmDB.InstrumentalAudio;
import com.santoshkumarsingh.gxmediaplayer.Interfaces.PlayListOnClickListener;
import com.santoshkumarsingh.gxmediaplayer.R;
import com.santoshkumarsingh.gxmediaplayer.Utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class InstrumentalRecyclerAdapter extends RecyclerView.Adapter<InstrumentalRecyclerAdapter.ViewHolder> {

    private RealmResults<InstrumentalAudio> audioList;
    private Utilities utilities;
    private PlayListOnClickListener onClickListener;

    public InstrumentalRecyclerAdapter(PlayListOnClickListener listener, RealmResults<InstrumentalAudio> audios) {
        onClickListener = listener;
        audioList = audios;
    }

    @Override
    public InstrumentalRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final InstrumentalRecyclerAdapter.ViewHolder holder, int position) {
        holder.audio = audioList.get(position);
        long duration = Long.parseLong(holder.audio.getDURATION());

        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(utilities.getImageIntoByteArray(holder.audio.getURL()))
                .apply(RequestOptions.centerCropTransform().error(R.drawable.pink))
                .into(holder.thumbnail);

        holder.mTitle.setText(holder.audio.getTITLE());
        holder.mArtist.setText(holder.audio.getARTIST());
        holder.duration.setText(utilities.milliSecondsToTimer(duration));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.OnInstrumentClick(audioList, holder.getAdapterPosition());
                }
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(holder.getAdapterPosition());
                audioList.size();
                if (audioList.size() == 0) {
                    Snackbar.make(view, R.string.list_empty, Snackbar.LENGTH_LONG).show();
                }

                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    private void delete(int position) {
        Realm realm = Realm.getDefaultInstance();
        audioList = realm.where(InstrumentalAudio.class).findAll();
        realm.beginTransaction();
        InstrumentalAudio song = audioList.get(position);
        song.deleteFromRealm();
        realm.commitTransaction();
        notifyDataSetChanged();
        realm.close();
        getItemCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.F_Title)
        TextView mTitle;
        @BindView(R.id.F_Artist)
        TextView mArtist;
        @BindView(R.id.song_duration)
        TextView duration;
        @BindView(R.id.F_delete)
        ImageButton delete;
        @BindView(R.id.F_imageView)
        ImageView thumbnail;

        private InstrumentalAudio audio;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            Animation animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
            utilities = new Utilities(itemView.getContext());
        }
    }
}
