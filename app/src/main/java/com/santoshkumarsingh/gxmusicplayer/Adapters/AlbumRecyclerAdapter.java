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

import com.santoshkumarsingh.gxmusicplayer.Interfaces.AlbumOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Album;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.ViewHolder> {

    Bitmap bitmap;
    private Utilities utilities;
    private List<Album> albumList;
    private AlbumOnClickListener onClickListener;

    public AlbumRecyclerAdapter(AlbumOnClickListener listener, List<Album> albums) {
        onClickListener = listener;
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
                    onClickListener.OnClick(albumList.get(i).getID());
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
        @BindView(R.id.A_Artist)
        TextView Album_artrist;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
            utilities = new Utilities(itemView.getContext());
        }
    }
}
