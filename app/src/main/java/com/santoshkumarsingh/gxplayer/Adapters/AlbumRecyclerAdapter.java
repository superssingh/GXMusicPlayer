package com.santoshkumarsingh.gxplayer.Adapters;

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
import com.santoshkumarsingh.gxplayer.Interfaces.ItemOnClickListener;
import com.santoshkumarsingh.gxplayer.Models.Album;
import com.santoshkumarsingh.gxplayer.R;
import com.santoshkumarsingh.gxplayer.Utilities.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.ViewHolder> {

    private Utilities utilities;
    private List<Album> albumList;
    private ItemOnClickListener itemOnClickListener;

    public AlbumRecyclerAdapter(ItemOnClickListener listener, List<Album> albums) {
        itemOnClickListener = listener;
        albumList = albums;
    }

    @Override
    public AlbumRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumRecyclerAdapter.ViewHolder holder, int position) {
        holder.Album_title.setText(albumList.get(position).getALBUM());
        final int i = position;
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(utilities.getImageIntoByteArray(albumList.get(position).getALBUM_ART()))
                .apply(RequestOptions.centerCropTransform().error(R.drawable.ic_album_24dp))
                .into(holder.albumImage);
        holder.albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemOnClickListener != null) {
                    itemOnClickListener.OnItemClick(albumList.get(i).getID());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumList.size();
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
            utilities = new Utilities(itemView.getContext());
        }
    }
}
