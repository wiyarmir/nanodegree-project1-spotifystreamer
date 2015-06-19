package es.guillermoorellana.spotifystreamer.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MediaPlayerService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PLAY = "es.guillermoorellana.spotifystreamer.services.action.PLAY";
    private static final String ACTION_PAUSE = "es.guillermoorellana.spotifystreamer.services.action.PAUSE";
    
    private static final String EXTRA_URL = "es.guillermoorellana.spotifystreamer.services.extra.URL";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPlay(Context context, String url) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }
    
    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPause(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }
    
    public MediaPlayerService() {
        super("MediaPlayerService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_URL);
                handleActionPlay(param1);
            } else if (ACTION_PAUSE.equals(action)) {
                handleActionPause();
            }
        }
    }
    
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(String param1) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPause() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
