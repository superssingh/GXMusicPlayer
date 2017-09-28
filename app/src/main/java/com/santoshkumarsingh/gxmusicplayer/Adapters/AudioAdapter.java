package com.santoshkumarsingh.gxmusicplayer.Adapters;

import android.graphics.Bitmap;
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
import com.santoshkumarsingh.gxmusicplayer.Database.RealmDB.RealmContentProvider;
import com.santoshkumarsingh.gxmusicplayer.Models.Audio;
import com.santoshkumarsingh.gxmusicplayer.R;
import com.santoshkumarsingh.gxmusicplayer.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

    RealmResults<FavoriteAudio> favoriteAudios;
    private Utilities utilities;
    private List<Audio> audioList = new ArrayList<>();
    private SongOnClickListener songOnClickListener;

    public AudioAdapter(SongOnClickListener songOnClickListener, List<Audio> audioList) {
        this.songOnClickListener = songOnClickListener;
        this.audioList = audioList;
        utilities = new Utilities();
    }

    @Override
    public AudioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AudioAdapter.ViewHolder holder, int position) {
        final Audio audio = audioList.get(position);
        final int audioPosition = position;
        holder.mTitle.setText(audio.getTITLE());
        holder.mArtist.setText(audio.getARTIST());

        final Bitmap trackImage = utilities.getTrackThumbnail(audio.getURL());
        if (trackImage != null) {
            holder.thumbnail.setImageBitmap(trackImage);
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_audiotrack);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songOnClickListener != null) {
                    songOnClickListener.OnItemClicked(audioPosition);
                }
            }
        });

        holder.favorite.setOnClickListener(new View.OnClickListener() {
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

    public void addSongs(List<Audio> audioList) {
        this.audioList = null;
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public interface SongOnClickListener {
        void OnItemClicked(int position);
    }

    public interface FavoriteAudioListener {
        void onFavoriteClicked(Audio audio);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.song_Title)
        TextView mTitle;
        @BindView(R.id.song_Artist)
        TextView mArtist;
        @BindView(R.id.option)
        ImageButton favorite;
        @BindView(R.id.imageView)
        ImageView thumbnail;
        Animation animation;

        public ViewHolder(View itemView) {
            super(itemView);
            animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.fade_in);
            itemView.setAnimation(animation);
            ButterKnife.bind(this, itemView);
        }

    }

}
