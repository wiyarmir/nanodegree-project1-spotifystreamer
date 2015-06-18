package es.guillermoorellana.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import es.guillermoorellana.spotifystreamer.fragments.ArtistFragment;
import es.guillermoorellana.spotifystreamer.fragments.NetworkFragment;


public class MainActivity extends AppCompatActivity implements NetworkFragment.OnArtistsResultListener {

    private static NetworkFragment networkFragment;
    private ArtistFragment artistFragment;

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
}
