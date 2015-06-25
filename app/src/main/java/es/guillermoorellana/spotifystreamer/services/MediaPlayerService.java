package es.guillermoorellana.spotifystreamer.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.fragments.PlayerFragment;
import kaaes.spotify.webapi.android.models.Track;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class MediaPlayerService extends Service
        implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MAIN = "es.guillermoorellana.spotifystreamer.services.action.MAIN";
    private static final String ACTION_PLAY = "es.guillermoorellana.spotifystreamer.services.action.PLAY";
    private static final String ACTION_PAUSE = "es.guillermoorellana.spotifystreamer.services.action.PAUSE";
    private static final String ACTION_NEXT = "es.guillermoorellana.spotifystreamer.services.action.NEXT";
    private static final String ACTION_PREV = "es.guillermoorellana.spotifystreamer.services.action.PREV";
    private static final String ACTION_SEEK = "es.guillermoorellana.spotifystreamer.services.action.SEEK";
    private static final String ACTION_STOP = "es.guillermoorellana.spotifystreamer.services.action.STOP";

    public static final String ACTION_UPDATE = "es.guillermoorellana.spotifystreamer.services.action.UPDATE";

    public static final String EXTRA_INDEX = "es.guillermoorellana.spotifystreamer.services.extra.INDEX";
    public static final String EXTRA_PROGRESS = "es.guillermoorellana.spotifystreamer.services.extra.PROGRESS";

    public static final String TAG = "MPService";
    public static final String KEY_BUFFER_STATE = "bufferState";
    public static final String KEY_TRACK_LENGTH = "trackLenght";
    public static final String KEY_STATE = "status";
    public static final String KEY_ELAPSED_TIME = "elapsed";

    private static final int NOTIFICATION_ID = 0x55;
    private MediaSessionCompat mediaSession;

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }


    public enum State {
        READY,
        BUFFERING,
        PLAYING,
        PAUSED,
        STOPPED,
        FINISHED,
        ERROR
    }

    private State playerState;
    private MediaPlayer mediaPlayer;
    private MediaControllerCompat mediaController;
    private List<Track> playlist;
    private int currentIndex;

    private Handler mHandler = new Handler();
    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                Intent intent = new Intent(ACTION_UPDATE);
                intent.putExtra(PlayerFragment.KEY_TRACK_INDEX, currentIndex);
                intent.putExtra(MediaPlayerService.KEY_TRACK_LENGTH, mediaPlayer.getDuration());
                intent.putExtra(MediaPlayerService.KEY_STATE, playerState);
                intent.putExtra(MediaPlayerService.KEY_ELAPSED_TIME, mediaPlayer.getCurrentPosition() / 1000);
                sendBroadcast(intent);
                if (State.PLAYING.equals(playerState) && mediaPlayer.isPlaying()) {
                    mHandler.postDelayed(this, 500);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy...");
        mediaPlayer.release();
        mediaPlayer = null;
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(this);
        if (mediaSession != null) {
            mediaSession.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, int index) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_INDEX, index);
        context.startService(intent);
    }


    public static void startActionSeek(Context context, int progress) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_SEEK);
        intent.putExtra(EXTRA_PROGRESS, progress);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlayPause(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    public static void startActionPrev(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PREV);
        context.startService(intent);
    }

    public static void startActionNext(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_NEXT);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaSession == null) {
            initMediaSessions();
        }

        handleIntent(intent);

        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mediaController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mediaController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREV)) {
            mediaController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mediaController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mediaController.getTransportControls().stop();
        }
    }

    private void handleActionSeek(int progress) {
        if (State.PLAYING.equals(playerState)) {
            mediaPlayer.seekTo(progress * 1000);
        }
    }


    private void handleActionPrev() {
        goNewTrack(currentIndex - 1);
    }

    private void handleActionNext() {
        goNewTrack(currentIndex + 1);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private Notification buildNotificationForCurrentTrack(NotificationCompat.Action action) {
        Track track = playlist.get(currentIndex);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_art);

        return new NotificationCompat.Builder(this)
                .setContentTitle(track.name)
                .setTicker(String.format("%s - %s", track.name, track.artists.get(0).name))
                .setContentText(track.artists.get(0).name)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.MediaStyle())
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREV))
                .addAction(action)
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT))

                .build();
    }

    private void handleActionPlay(int index) {
        Log.d(TAG, "handleActionPlay");
        currentIndex = index;

        resetMediaPlayer();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Can't get Audio Focus");

        }

        playlist = MainActivity.getNetworkFragment().getTopTrackList();
        Track track = playlist.get(index);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        try {
            String preview_url = track.preview_url;
            Log.d(TAG, "Attempting to play " + preview_url);
            mediaPlayer.setDataSource(preview_url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void resetMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        playerState = State.STOPPED;
    }

    private void handleActionPlay() {
        mediaPlayer.start();
        mediaSession.setPlaybackState(buildPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0));
        playerState = State.PLAYING;
        startForeground(NOTIFICATION_ID, buildNotificationForCurrentTrack(null));
        mHandler.postDelayed(timer, 0);
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(MediaPlayerService.KEY_STATE, playerState);
        sendBroadcast(intent);
    }

    private void handleActionPause() {

        mediaPlayer.pause();
        playerState = State.PAUSED;
        stopForeground(true);
        mHandler.removeCallbacks(timer);
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(MediaPlayerService.KEY_STATE, playerState);
        sendBroadcast(intent);

    }

    private PlaybackStateCompat buildPlaybackState(int state, long position) {
        return new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, position, 1)
                .build();
    }


    private void initMediaSessions() {
        mediaPlayer = new MediaPlayer();

        mediaSession = new MediaSessionCompat(
                this,
                TAG,
                ComponentName.unflattenFromString("es.guillermoorellana.spotifystreamer.services.MediaPlayerService"),
                null);
        mediaController = mediaSession.getController();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.e("MediaPlayerService", "onPlay");
                updateNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.e("MediaPlayerService", "onPause");
                updateNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.e("MediaPlayerService", "onSkipToNext");
                handleActionNext();
                updateNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e("MediaPlayerService", "onSkipToPrevious");
                handleActionPrev();
                updateNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }


            @Override
            public void onStop() {
                super.onStop();
                Log.e("MediaPlayerService", "onStop");
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFICATION_ID);
                Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                stopService(intent);
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

        });
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "focus change:" + focusChange);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferState) {
        Log.d(TAG, "Buffering: " + bufferState);
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(MediaPlayerService.KEY_BUFFER_STATE, bufferState);
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Completion");
        goNewTrack(currentIndex + 1);
    }

    private void goNewTrack(int newindex) {
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        if (newindex < playlist.size()) {
            handleActionPlay(newindex);
        } else {
            stopForeground(true);
        }
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(PlayerFragment.KEY_TRACK_INDEX, currentIndex);
        sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.d(TAG, "ERROR!");
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "ERROR! Unknown");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "ERROR! Server died");
                break;
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        Log.d(TAG, "mediaplayer has info");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "mediaplayer is ready");
        updateNotification(null);
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(PlayerFragment.KEY_TRACK_INDEX, currentIndex);
        intent.putExtra(MediaPlayerService.KEY_TRACK_LENGTH, mediaPlayer.getDuration());
        intent.putExtra(MediaPlayerService.KEY_STATE, State.PLAYING);
        intent.putExtra(MediaPlayerService.KEY_ELAPSED_TIME, mediaPlayer.getCurrentPosition() / 1000);
        sendBroadcast(intent);
    }

    private void updateNotification(NotificationCompat.Action middleAction) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification notification = buildNotificationForCurrentTrack(middleAction);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
