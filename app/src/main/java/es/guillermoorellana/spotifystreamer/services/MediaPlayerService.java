package es.guillermoorellana.spotifystreamer.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
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
 * <p>
 */
public class MediaPlayerService extends Service
        implements MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MAIN = "es.guillermoorellana.spotifystreamer.services.action.MAIN";
    private static final String ACTION_PLAY = "es.guillermoorellana.spotifystreamer.services.action.PLAY";
    private static final String ACTION_PAUSE = "es.guillermoorellana.spotifystreamer.services.action.PAUSE";
    private static final String ACTION_RESUME = "es.guillermoorellana.spotifystreamer.services.action.RESUME";
    private static final String ACTION_NEXT = "es.guillermoorellana.spotifystreamer.services.action.NEXT";
    private static final String ACTION_PREV = "es.guillermoorellana.spotifystreamer.services.action.PREV";
    private static final String ACTION_START_FOREGROUND = "es.guillermoorellana.spotifystreamer.services.action.START_FOREGROUND";
    private static final String ACTION_STOP_FOREGROUND = "es.guillermoorellana.spotifystreamer.services.action.STOP_FOREGROUND";

    public static final String ACTION_UPDATE = "es.guillermoorellana.spotifystreamer.services.action.UPDATE";

    private static final String EXTRA_INDEX = "es.guillermoorellana.spotifystreamer.services.extra.INDEX";
    public static final String TAG = "MPService";
    private static final int NOTIFICATION_ID = 0x55;
    private MediaPlayer mediaPlayer;
    private List<Track> playlist;
    private int currentIndex;

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy...");
        mediaPlayer.release();
        mediaPlayer = null;
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(this);
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
    public static void startForeground(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_START_FOREGROUND);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void stopForeground(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_STOP_FOREGROUND);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, int index) {
        startForeground(context);
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_INDEX, index);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPause(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResume(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_RESUME);
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
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_FOREGROUND.equals(action)) {
                handleActionForeground();
            } else if (ACTION_STOP_FOREGROUND.equals(action)) {
                handleActionStopForeground();
            } else if (ACTION_PLAY.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_INDEX, 0);
                handleActionPlay(param1);
            } else if (ACTION_PAUSE.equals(action)) {
                handleActionPause();
            } else if (ACTION_RESUME.equals(action)) {
                handleActionResume();
            } else if (ACTION_NEXT.equals(action)) {
                handleActionNext();
            } else if (ACTION_PREV.equals(action)) {
                handleActionPrev();
            }
        }

        return START_STICKY;
    }

    private void handleActionPrev() {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
    }

    private void handleActionNext() {
        onCompletion(null);
    }

    private void handleActionStopForeground() {
        stopForeground(false);
    }

    private void handleActionForeground() {
        Log.i(TAG, "Received Start Foreground Intent ");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, MediaPlayerService.class);
        previousIntent.setAction(ACTION_PREV);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, MediaPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_art);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Spotify Streamer")
                .setTicker("Spotify Streamer")
                .setContentText("Music")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void handleActionPlay(int index) {
        Log.d(TAG, "handleActionPlay");
        currentIndex = index;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();


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
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        try {
            String preview_url = playlist.get(index).preview_url;
            Log.d(TAG, "Attempting to play " + preview_url);
            mediaPlayer.setDataSource(preview_url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void handleActionResume() {
        mediaPlayer.start();
    }

    private void handleActionPause() {
        mediaPlayer.pause();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "MediaPlayer ready, starting...");
        mediaPlayer.start();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "focus change:" + focusChange);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int what) {
        Log.d(TAG, "Buffering: " + what);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Completion");
        goNextTrack();
    }

    private void goNextTrack() {
        if (currentIndex + 1 < playlist.size()) {
            handleActionPlay(currentIndex + 1);
        } else {
            handleActionStopForeground();
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

}
