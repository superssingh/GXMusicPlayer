package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.VideoOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Video;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.io.File;
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

    public VideoRecyclerAdapter(VideoOnClickListener videoOnClickListener, List<Video> videos) {
        this.videoOnClickListener = videoOnClickListener;
        this.videoList = videos;
    }

    @Override
    public VideoRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoRecyclerAdapter.ViewHolder holder, int position) {
        final Video video = videoList.get(position);
        long duration = Long.parseLong(video.getDURATION());

        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(Uri.fromFile(new File(video.getURL())))
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.ic_ondemand_video_24dp))
                .into(holder.thumbnail);

        holder.mTitle.setText(video.getTITLE());
        holder.mAlbum.setText(video.getALBUM());
        holder.duration.setText(utilities.milliSecondsToTimer(duration));

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
