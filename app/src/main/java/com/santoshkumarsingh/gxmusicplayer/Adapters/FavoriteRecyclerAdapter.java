package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.graphics.Bitmap;
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

import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.FavoriteAudio;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.FavoriteOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<FavoriteRecyclerAdapter.ViewHolder> {

    private RealmResults<FavoriteAudio> favoriteAudios;
    private Utilities utilities;
    private FavoriteOnClickListener onClickListener;

    public FavoriteRecyclerAdapter(FavoriteOnClickListener listener, RealmResults<FavoriteAudio> audios) {
        onClickListener = listener;
        favoriteAudios = audios;
        utilities = new Utilities();
    }

    @Override
    public FavoriteRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FavoriteRecyclerAdapter.ViewHolder holder, int position) {
        holder.favoriteAudio = favoriteAudios.get(position);
        holder.mTitle.setText(holder.favoriteAudio.getTITLE());
        holder.mArtist.setText(holder.favoriteAudio.getARTIST());

        final Bitmap trackImage = utilities.getTrackThumbnail(holder.favoriteAudio.getURL());
        if (trackImage != null) {
            holder.thumbnail.setImageBitmap(trackImage);
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_audiotrack);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onClickListener != null) {
                    onClickListener.OnClick(holder.delete, holder.itemView, trackImage, holder.favoriteAudio.getURL(), holder.getAdapterPosition());
                }
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(holder.getAdapterPosition());
                favoriteAudios.size();
                if (favoriteAudios.size() == 0) {
                    Snackbar.make(view, R.string.Favorite_empty_list, Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return favoriteAudios.size();
    }

    private void delete(int position) {
        Realm realm = Realm.getDefaultInstance();
        favoriteAudios = realm.where(FavoriteAudio.class).findAll();
        realm.beginTransaction();
        FavoriteAudio fm = favoriteAudios.get(position);
        fm.deleteFromRealm();
        realm.commitTransaction();
        notifyDataSetChanged();
        realm.close();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Animation animation;
        @BindView(R.id.F_Title)
        TextView mTitle;
        @BindView(R.id.F_Artist)
        TextView mArtist;
        @BindView(R.id.F_delete)
        ImageButton delete;
        @BindView(R.id.F_imageView)
        ImageView thumbnail;
        private FavoriteAudio favoriteAudio;

        public ViewHolder(View itemView) {
            super(itemView);
            animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
            ButterKnife.bind(this, itemView);
        }
    }
}
