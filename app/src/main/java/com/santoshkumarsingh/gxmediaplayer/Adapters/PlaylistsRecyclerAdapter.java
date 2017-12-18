package com.santoshkumarsingh.gxmediaplayer.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.santoshkumarsingh.gxmediaplayer.Interfaces.ItemOnClickListener;
import com.santoshkumarsingh.gxmediaplayer.Models.Playlist;
import com.santoshkumarsingh.gxmediaplayer.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class PlaylistsRecyclerAdapter extends RecyclerView.Adapter<PlaylistsRecyclerAdapter.ViewHolder> {

    private ItemOnClickListener itemOnClickListener;
    private List<Playlist> playlist;

    public PlaylistsRecyclerAdapter(ItemOnClickListener itemOnClickListener, List<Playlist> playlists) {
        this.itemOnClickListener = itemOnClickListener;
        playlist = playlists;
    }

    @Override
    public PlaylistsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistsRecyclerAdapter.ViewHolder holder, int position) {

        holder.Album_title.setText(playlist.get(position).getTitle());
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(playlist.get(position).getIcon())
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.ic_audiotrack))
                .into(holder.albumImage);
        holder.albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemOnClickListener != null) {
                    itemOnClickListener.OnItemClick(holder.getPosition() + "");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Animation animation;
        @BindView(R.id.A_imageView)
        ImageView albumImage;
        @BindView(R.id.A_Title)
        TextView Album_title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
        }
    }
}
