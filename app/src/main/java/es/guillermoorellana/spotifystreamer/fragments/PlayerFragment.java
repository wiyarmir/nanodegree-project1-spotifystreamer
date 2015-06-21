package es.guillermoorellana.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.services.MediaPlayerService;
import es.guillermoorellana.spotifystreamer.services.MediaPlayerService.State;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by guillermo on 19/06/2015.
 */
public class PlayerFragment extends DialogFragment {
    public static final String TAG = "dialogfragment";
    public static final String KEY_TRACK_INDEX = "trackIndex";

    @InjectView(R.id.cover) ImageView albumCover;
    @InjectView(R.id.title) TextView trackTitle;
    @InjectView(R.id.artist) TextView trackArtist;
    @InjectView(R.id.album) TextView trackAlbum;
    @InjectView(R.id.button_next) ImageButton buttonNext;
    @InjectView(R.id.button_play) ImageButton buttonPlay;
    @InjectView(R.id.button_prev) ImageButton buttonPrev;
    @InjectView(R.id.elapsedtime) TextView elapsedTime;
    @InjectView(R.id.totaltime) TextView totalTime;
    @InjectView(R.id.seekBar) SeekBar progressBar;

    private boolean playing = false;
    private UpdateReceiver receiver;


    private void updateElapsed(int seconds) {
        progressBar.setProgress(seconds);
        if (elapsedTime != null) {
            elapsedTime.setText(String.format("%d:%02d",
                    TimeUnit.SECONDS.toMinutes(seconds),
                    seconds % 60));
        }
    }

    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MediaPlayerService.ACTION_UPDATE.equals(intent.getAction())) {
                Log.d(TAG, "received update broadcast");
                if (intent.hasExtra(KEY_TRACK_INDEX)) {
                    updateDisplayedTrack(intent.getIntExtra(KEY_TRACK_INDEX, 0));
                }
                if (intent.hasExtra(MediaPlayerService.KEY_STATE)) {
                    State state = (State) intent.getSerializableExtra(MediaPlayerService.KEY_STATE);
                    playing = State.PLAYING.equals(state);
                }
                if (intent.hasExtra(MediaPlayerService.KEY_TRACK_LENGTH)) {
                    updateTotalLenght(intent.getIntExtra(MediaPlayerService.KEY_TRACK_LENGTH, 0));
                }
                if (intent.hasExtra(MediaPlayerService.KEY_ELAPSED_TIME)) {
                    updateElapsed(intent.getIntExtra(MediaPlayerService.KEY_ELAPSED_TIME, 0));
                }
                updatePlayButton();
            }
        }
    }

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
        ButterKnife.inject(this, dialog);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaPlayerService.ACTION_UPDATE);
        receiver = new UpdateReceiver();
        getActivity().registerReceiver(receiver, filter);

        Bundle args = getArguments();
        int currentIndex = args.getInt(KEY_TRACK_INDEX);

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaPlayerService.startActionSeek(getActivity(), seekBar.getProgress());
            }
        });
    }

    private void updateDisplayedTrack(int currentIndex) {
        List<Track> topTrackList = MainActivity.getNetworkFragment().getTopTrackList();
        Track currentTrack = topTrackList.get(currentIndex);
        if (currentTrack != null) {
            trackTitle.setText(currentTrack.name);
            trackArtist.setText(currentTrack.artists.get(0).name);
            trackAlbum.setText(currentTrack.album.name);

            Picasso.with(getActivity())
                    .load(currentTrack.album.images.get(0).url)
                    .placeholder(R.drawable.no_album_art)
                    .into(albumCover);

            updateTotalLenght(currentTrack.duration_ms);
        }
    }

    private void updateTotalLenght(long duration_ms) {
        totalTime.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration_ms),
                TimeUnit.MILLISECONDS.toSeconds(duration_ms) % 60));
        progressBar.setMax((int) duration_ms / 1000);
    }

    @OnClick(R.id.button_play)
    public void onClickButtonPlayPause() {
        playing = !playing;
        updatePlayButton();
        MediaPlayerService.startActionPlayPause(getActivity());
    }

    private void updatePlayButton() {
        if (playing) {
            buttonPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            buttonPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @OnClick(R.id.button_next)
    public void onClickButtonNext() {
        MediaPlayerService.startActionNext(getActivity());
    }

    @OnClick(R.id.button_prev)
    public void onClickButtonPrev() {
        MediaPlayerService.startActionPrev(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }
}
