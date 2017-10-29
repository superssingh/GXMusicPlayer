package com.santoshkumarsingh.gxplayer.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.OnBoomListener;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.santoshkumarsingh.gxplayer.Database.RealmDB.RealmContentProvider;
import com.santoshkumarsingh.gxplayer.Interfaces.SongOnClickListener;
import com.santoshkumarsingh.gxplayer.Models.Audio;
import com.santoshkumarsingh.gxplayer.R;
import com.santoshkumarsingh.gxplayer.Utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by santoshsingh (santoshkumarsingh.com) on 17/08/17.
 */

@SuppressWarnings("ObjectEqualsNull")
public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.ViewHolder> {

    private Utilities utilities;
    private List<Audio> audioList = new ArrayList<>();
    private SongOnClickListener songOnClickListener;

    public AudioRecyclerAdapter(SongOnClickListener songOnClickListener, List<Audio> audioList) {
        this.songOnClickListener = songOnClickListener;
        this.audioList = audioList;
    }

    @Override
    public AudioRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AudioRecyclerAdapter.ViewHolder holder, int position) {
        final Audio audio = audioList.get(position);
        final int audioPosition = position;
        long duration = Long.parseLong(audio.getDURATION());

        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(utilities.getImageIntoByteArray(audio.getURL()))
                .apply(RequestOptions.centerCropTransform().error(R.drawable.audio_placeholder))
                .into(holder.thumbnail);

        holder.mTitle.setText(audio.getTITLE());
        holder.mArtist.setText(audio.getARTIST());
        holder.duration.setText(utilities.milliSecondsToTimer(duration));

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
            }
        });


        int[] recorderIcons = new int[]{R.drawable.red, R.drawable.audio_placeholder,
                R.drawable.aqua, R.drawable.orange_placehonder, R.drawable.blue};
        int[] recorderTitle = new int[]{R.string.NewAudio, R.string.PartyAudio,
                R.string.RelaxedAudio, R.string.Motivational, R.string.Instrumental};
        int[] recordersubTitle = new int[]{R.string.NewAudioSub, R.string.PartyAudioSub,
                R.string.RelaxedAudioSub, R.string.MotivationalSub, R.string.InstrumentalSub};
        int[] bmbColor = new int[]{
                R.color.colorAccent, R.color.green, R.color.aqua,
                R.color.orange, R.color.blue
        };

        for (int i = 0; i < holder.bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder()
                    .normalImageDrawable(holder.itemView.getResources().getDrawable(recorderIcons[i]))
                    .normalTextRes(recorderTitle[i])
                    .subNormalTextRes(recordersubTitle[i])
                    .normalColorRes(bmbColor[i]);

            holder.bmb.addBuilder(builder);
        }


        holder.bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                RealmContentProvider contentProvider = new RealmContentProvider();
                switch (index) {
                    case 0:
                        contentProvider.addNewSong(holder.itemView.getContext(), audio);
                        break;
                    case 1:
                        contentProvider.addPartySong(holder.itemView.getContext(), audio);
                        break;
                    case 2:
                        contentProvider.addRelaxedSong(holder.itemView.getContext(), audio);
                        break;
                    case 3:
                        contentProvider.addMotivationSong(holder.itemView.getContext(), audio);
                        break;
                    case 4:
                        contentProvider.addInstpirationalSong(holder.itemView.getContext(), audio);
                        break;
                }
            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

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

    public void setFilter(List<Audio> newList) {
        audioList = new ArrayList<>();
        audioList.addAll(newList);
        notifyDataSetChanged();
    }

    private void initBoomMemu() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.song_Title)
        TextView mTitle;
        @BindView(R.id.song_Artist)
        TextView mArtist;
        @BindView(R.id.imageView)
        ImageView thumbnail;
        @BindView(R.id.song_duration)
        TextView duration;
        ShineButton love;
        BoomMenuButton bmb;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            utilities = new Utilities(itemView.getContext());
            love = itemView.findViewById(R.id.love);
            bmb = itemView.findViewById(R.id.bmb1);
        }

    }

}
