package es.guillermoorellana.spotifystreamer.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.fragments.NetworkFragment;
import es.guillermoorellana.spotifystreamer.fragments.PlayerFragment;
import kaaes.spotify.webapi.android.models.Track;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class MediaPlayerService extends Service implements Playback.Callback {

    public static final String ACTION_MAIN = "es.guillermoorellana.spotifystreamer.services.action.MAIN";
    public static final String ACTION_PLAY = "es.guillermoorellana.spotifystreamer.services.action.PLAY";
    public static final String ACTION_PAUSE = "es.guillermoorellana.spotifystreamer.services.action.PAUSE";
    public static final String ACTION_NEXT = "es.guillermoorellana.spotifystreamer.services.action.NEXT";
    public static final String ACTION_PREV = "es.guillermoorellana.spotifystreamer.services.action.PREV";
    public static final String ACTION_SEEK = "es.guillermoorellana.spotifystreamer.services.action.SEEK";
    public static final String ACTION_STOP = "es.guillermoorellana.spotifystreamer.services.action.STOP";

    public static final String ACTION_UPDATE = "es.guillermoorellana.spotifystreamer.services.action.UPDATE";

    public static final String EXTRA_INDEX = "es.guillermoorellana.spotifystreamer.services.extra.INDEX";
    public static final String EXTRA_PROGRESS = "es.guillermoorellana.spotifystreamer.services.extra.PROGRESS";

    public static final String TAG = "MPService";
    public static final String KEY_BUFFER_STATE = "bufferState";
    public static final String KEY_TRACK_LENGTH = "trackLenght";
    public static final String KEY_STATE = "status";
    public static final String KEY_ELAPSED_TIME = "elapsed";
    private static final int STOP_DELAY = 30000;


    private static final int NOTIFICATION_ID = 0x55;
    private MediaSessionCompat mediaSession;
    private boolean serviceStarted;


    private MediaControllerCompat mediaController;
    private List<Track> playingQueue;
    private int currentIndexOnQueue;

    private Playback mPlayback;
    private NotificationManagerCompat notificationManager;
    private DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        playingQueue = new ArrayList<>();
        mPlayback = new Playback(this);
        mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();
        initMediaSessions();
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy...");

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.release();

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


    private void handleActionPrev() {
        currentIndexOnQueue--;
        goNewTrack();
    }

    private void handleActionNext() {
        currentIndexOnQueue++;
        goNewTrack();
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private Notification buildNotificationForCurrentTrack(NotificationCompat.Action action) {
        Track track = playingQueue.get(currentIndexOnQueue);

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

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder) {
        MainActivity.getNetworkFragment().fetch(bitmapUrl, new NetworkFragment.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                Log.d(TAG, "fetchBitmapFromURLAsync: set bitmap to " + artUrl);
                builder.setLargeIcon(bitmap);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        });
    }

    private void handleActionPlay() {
        if (!serviceStarted) {
            startService(new Intent(getApplicationContext(), MediaPlayerService.class));
            serviceStarted = true;
        }
        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }
//        updateMetadata();
        mPlayback.play(playingQueue.get(currentIndexOnQueue));
    }

    private void handleActionPause() {
        mPlayback.pause();
        stopForeground(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

    }

    private void handleActionStop() {
        mPlayback.stop(true);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        stopForeground(true);
        stopSelf();
        serviceStarted = false;
    }

    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
        Log.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }
        
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        
        int state = mPlayback.getState();
        
        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        stateBuilder.setState(state, position, 1.0f);

        mediaSession.setPlaybackState(stateBuilder.build());
        
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            updateNotification(null);
        }
    }

    
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        if (playingQueue == null || playingQueue.isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        if (currentIndexOnQueue > 0) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        if (currentIndexOnQueue < playingQueue.size() - 1) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return actions;
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
        mediaSession = new MediaSessionCompat(
                this,
                TAG,
                ComponentName.unflattenFromString("es.guillermoorellana.spotifystreamer.services.MediaPlayerService"),
                null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaController = mediaSession.getController();

        mediaSession.setCallback(new MediaSessionCallback());
    }

    private void goNewTrack() {
        if (currentIndexOnQueue < 0) {
            currentIndexOnQueue = 0;
        }
        if (currentIndexOnQueue < playingQueue.size()) {
            handleActionPlay();
        } else {
            handleActionStop();
        }
    }

    private void updateNotification(NotificationCompat.Action middleAction) {
        Notification notification = buildNotificationForCurrentTrack(middleAction);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onCompletion() {
        handleActionNext();
    }

    @Override
    public void onPlaybackStatusChanged(int state) {

    }

    @Override
    public void onError(String error) {

    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MediaPlayerService> mWeakReference;

        private DelayedStopHandler(MediaPlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPlayerService service = mWeakReference.get();
            if (service != null && service.mPlayback != null) {
                if (service.mPlayback.isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
                service.serviceStarted = false;
            }
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Log.e("MediaPlayerService", "onPlay");
            if (playingQueue == null || playingQueue.isEmpty()) {
                playingQueue = MainActivity.getNetworkFragment().getTopTrackList();
            }
            if (playingQueue != null && !playingQueue.isEmpty()) {
                handleActionPlay();
            }
            updateNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.e("MediaPlayerService", "onPause");
            handleActionPause();
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
            handleActionStop();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo((int) pos);
        }

    }

}
