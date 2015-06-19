package es.guillermoorellana.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import es.guillermoorellana.spotifystreamer.fragments.ArtistFragment;
import es.guillermoorellana.spotifystreamer.fragments.NetworkFragment;
import es.guillermoorellana.spotifystreamer.fragments.PlayerFragment;
import es.guillermoorellana.spotifystreamer.fragments.TopTracksFragment;


public class MainActivity extends AppCompatActivity
        implements NetworkFragment.OnArtistsResultListener,
        ArtistFragment.Callback,
        TopTracksFragment.Callback {

    private static NetworkFragment networkFragment;
    private ArtistFragment artistFragment;
    private boolean twoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkFragment = (NetworkFragment) getSupportFragmentManager()
                .findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(networkFragment, NetworkFragment.TAG)
                    .commit();
        }

        if (findViewById(R.id.detail_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.detail_container, new TopTracksFragment(),
                                TopTracksFragment.TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }

        artistFragment = (ArtistFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_artists);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkFragment.setOnArtistsResultListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkFragment.setOnArtistsResultListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static NetworkFragment getNetworkFragment() {
        return networkFragment;
    }

    @Override
    public void onNetworkSuccess() {
        artistFragment.onNetworkSuccess();
    }

    @Override
    public void onNetworkError(String message) {
        artistFragment.onNetworkError(message);
    }

    @Override
    public void onItemSelected(String artistId) {
        if (!twoPane) {
            Intent i = new Intent(this, TopTrackActivity.class);
            i.putExtra(ArtistFragment.KEY_ARTIST_ID, artistId);
            startActivity(i);
        } else {
            Bundle args = new Bundle();
            args.putString(ArtistFragment.KEY_ARTIST_ID, artistId);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, ArtistFragment.TAG)
                    .commit();

        }
    }

    @Override
    public void onListItemClick(int track) {
        // if this is called to MainActivity, means that we are in big screen layout. Show as dialog
        FragmentManager fm = getSupportFragmentManager();
        PlayerFragment player;

        player = (PlayerFragment) fm.findFragmentByTag(PlayerFragment.TAG);
        if (player == null) {
            player = new PlayerFragment();
        }

        Bundle args = new Bundle();
        args.putInt(PlayerFragment.KEY_TRACK_INDEX, track);
        player.setArguments(args);
        player.show(fm, PlayerFragment.TAG);
    }
}
