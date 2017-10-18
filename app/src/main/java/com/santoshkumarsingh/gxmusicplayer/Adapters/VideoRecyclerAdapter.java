package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.santoshkumarsingh.gxmusicplayer.Interfaces.VideoOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Video;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder> {

    private Utilities utilities;
    private List<Video> videoList = new ArrayList<>();
    private VideoOnClickListener videoOnClickListener;
    private Bitmap bitmap;

    public VideoRecyclerAdapter(VideoOnClickListener videoOnClickListener, List<Video> videos) {
        this.videoOnClickListener = videoOnClickListener;
        this.videoList = videos;
    }

    @Override
    public VideoRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoRecyclerAdapter.ViewHolder holder, int position) {
        final Video video = videoList.get(position);
        final int videoPosition = position;
        holder.mTitle.setText(video.getTITLE());
        holder.mAlbum.setText(video.getALBUM());
        long duration = Long.parseLong(video.getDURATION());
        holder.duration.setText(utilities.milliSecondsToTimer(duration));

        bitmap = utilities.getTrackThumbnail(video.getURL()) != null
                ? utilities.compressBitmap(utilities.getTrackThumbnail(video.getURL()))
                : null;

        if (bitmap != null) {
            holder.thumbnail.setImageBitmap(bitmap);
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_ondemand_video_24dp);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoOnClickListener != null) {
                    videoOnClickListener.OnItemClicked(video.getURL());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public List<Video> getAudioList() {
        return videoList;
    }

    private Video getItem(int position) {
        return videoList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.V_Title)
        TextView mTitle;
        @BindView(R.id.V_Album)
        TextView mAlbum;
        @BindView(R.id.V_Thumbnail)
        ImageView thumbnail;
        @BindView(R.id.V_Duration)
        TextView duration;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            utilities = new Utilities(itemView.getContext());
        }

    }
}
