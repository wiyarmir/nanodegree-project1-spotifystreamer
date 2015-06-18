package es.guillermoorellana.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import es.guillermoorellana.spotifystreamer.fragments.ArtistFragment;
import es.guillermoorellana.spotifystreamer.fragments.NetworkFragment;
import es.guillermoorellana.spotifystreamer.fragments.TopTracksFragment;
import kaaes.spotify.webapi.android.models.Track;


public class TopTrackActivity extends AppCompatActivity implements NetworkFragment.OnTracksResultListener {

    private TopTracksFragment topTracksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        String artistId = getIntent().getExtras().getString(ArtistFragment.KEY_ARTIST_ID, null);

        if (artistId == null) {
            Toast.makeText(this, "ERROR: No artist provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        topTracksFragment = (TopTracksFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_tracks);

        // avoid double calls
        if (!artistId.equals(MainActivity.getNetworkFragment().getCurrentArtistId())) {
            MainActivity.getNetworkFragment().searchTopTracks(artistId);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;


            default:
                break;

        }
        return true;
    }

    @Override
    public void onNetworkSuccess() {
        topTracksFragment.onNetworkSuccess();
    }

    @Override
    public void onNetworkError(String message) {
        topTracksFragment.onNetworkError(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.getNetworkFragment().setOnTracksResultListener(this);
    }

    @Override
    protected void onStop() {
        MainActivity.getNetworkFragment().setOnTracksResultListener(null);
        super.onStop();
    }

    public List<Track> getTopTracksList() {
        return MainActivity.getNetworkFragment().getTopTrackList();
    }
}
