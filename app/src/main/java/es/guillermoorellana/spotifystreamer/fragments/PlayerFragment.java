package es.guillermoorellana.spotifystreamer.fragments;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import es.guillermoorellana.spotifystreamer.R;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by guillermo on 19/06/2015.
 */
public class PlayerFragment extends DialogFragment implements MediaPlayer.OnPreparedListener {
    public static final String TAG = "dialogfragment";
    private MediaPlayer mediaPlayer;

    @InjectView(R.id.cover) ImageView albumCover;
    @InjectView(R.id.title) TextView trackTitle;
    @InjectView(R.id.artist) TextView trackArtist;
    @InjectView(R.id.album) TextView trackAlbum;
    @InjectView(R.id.button_next) ImageButton buttonNext;
    @InjectView(R.id.button_play) ImageButton buttonPlay;
    @InjectView(R.id.button_prev) ImageButton buttonPrev;
    @InjectView(R.id.elapsedtime) TextView elapsedTime;
    @InjectView(R.id.totaltime) TextView totalTime;

    private Track currentTrack = null;
    private boolean playing = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_layout, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        updateDisplayedTrack();
    }

    private void updateDisplayedTrack() {
        if (currentTrack != null) {
            trackTitle.setText(currentTrack.name);
            trackArtist.setText(currentTrack.artists.get(0).name);
            trackAlbum.setText(currentTrack.album.name);

            Picasso.with(getActivity())
                    .load(currentTrack.album.images.get(0).url)
                    .placeholder(R.drawable.no_album_art)
                    .into(albumCover);

            totalTime.setText(String.format("%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentTrack.duration_ms),
                    TimeUnit.MILLISECONDS.toSeconds(currentTrack.duration_ms) % 60));

            try {
                mediaPlayer.setDataSource(currentTrack.preview_url);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            buttonPlay.setEnabled(false);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        }
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        buttonPlay.setEnabled(true);
    }

    @OnClick(R.id.button_play)
    public void onClickButtonPlay(ImageView view) {
        playing = !playing;
        if (playing) {
            view.setImageResource(android.R.drawable.ic_media_pause);
            mediaPlayer.start();
        } else {
            view.setImageResource(android.R.drawable.ic_media_play);
            mediaPlayer.pause();
        }
    }

    @Override
    public void onPause() {
        mediaPlayer.pause();
        super.onPause();
    }
}
