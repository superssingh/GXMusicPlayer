package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.santoshkumarsingh.gxmusicplayer.Interfaces.ArtistOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Artist;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class ArtistRecyclerAdapter extends RecyclerView.Adapter<ArtistRecyclerAdapter.ViewHolder> {

    Bitmap bitmap;
    private Utilities utilities;
    private List<Artist> albumList;
    private ArtistOnClickListener onClickListener;

    public ArtistRecyclerAdapter(ArtistOnClickListener listener, List<Artist> artists) {
        onClickListener = listener;
        albumList = artists;
    }

    @Override
    public ArtistRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ArtistRecyclerAdapter.ViewHolder holder, int position) {
        holder.Album_artrist.setText(albumList.get(position).getARTIST());
        final int i = position;

        bitmap = utilities.getTrackThumbnail(albumList.get(i).getALBUM_ART()) != null
                ? utilities.compressBitmap(utilities.getTrackThumbnail(albumList.get(i).getALBUM_ART()))
                : null;

        if (bitmap != null) {
            holder.albumImage.setImageBitmap(bitmap);
        } else {
            holder.albumImage.setImageResource(R.drawable.ic_audiotrack);
        }

        holder.albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.OnClick(albumList.get(i).getARTIST());
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
        @BindView(R.id.Artist_imageView)
        ImageView albumImage;
        @BindView(R.id.Artist)
        TextView Album_artrist;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            utilities = new Utilities(itemView.getContext());
            animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
        }
    }
}
