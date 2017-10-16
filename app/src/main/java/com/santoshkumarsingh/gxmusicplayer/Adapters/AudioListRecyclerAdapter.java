package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;
import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.RealmContentProvider;
import com.santoshkumarsingh.gxmusicplayer.Interfaces.SongOnClickListener;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
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
public class AudioListRecyclerAdapter extends RecyclerView.Adapter<AudioListRecyclerAdapter.ViewHolder> {

    Bitmap bitmap;
    private Utilities utilities;
    private List<Audio> audioList = new ArrayList<>();
    private SongOnClickListener songOnClickListener;

    public AudioListRecyclerAdapter(SongOnClickListener songOnClickListener, List<Audio> audioList) {
        this.songOnClickListener = songOnClickListener;
        this.audioList = audioList;
        utilities = new Utilities();
    }

    @Override
    public AudioListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AudioListRecyclerAdapter.ViewHolder holder, int position) {
        final Audio audio = audioList.get(position);
        final int audioPosition = position;
        holder.mTitle.setText(audio.getTITLE());
        holder.mArtist.setText(audio.getARTIST());
        bitmap = utilities.getTrackThumbnail(audio.getURL()) != null
                ? utilities.compressBitmap(utilities.getTrackThumbnail(audio.getURL()))
                : null;

        if (bitmap != null) {
            holder.thumbnail.setImageBitmap(bitmap);
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_audiotrack);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songOnClickListener != null) {
                    songOnClickListener.OnItemClicked(audioList, audioPosition);
                }
            }
        });

        holder.love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RealmContentProvider contentProvider = new RealmContentProvider();
                contentProvider.addFavorite(view.getContext(), audio);
                view.setBackgroundResource(R.drawable.ic_favorite_24dp);
            }
        });

    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    private Audio getItem(int position) {
        return audioList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.song_Title)
        TextView mTitle;
        @BindView(R.id.song_Artist)
        TextView mArtist;
        @BindView(R.id.imageView)
        ImageView thumbnail;
        @BindView(R.id.love)
        ShineButton love;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            love = new ShineButton(itemView.getContext());
        }

    }
}
