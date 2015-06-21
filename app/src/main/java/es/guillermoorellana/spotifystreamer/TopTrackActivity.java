package es.guillermoorellana.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import es.guillermoorellana.spotifystreamer.fragments.ArtistFragment;
import es.guillermoorellana.spotifystreamer.fragments.PlayerFragment;
import es.guillermoorellana.spotifystreamer.fragments.TopTracksFragment;
import es.guillermoorellana.spotifystreamer.services.MediaPlayerService;


public class TopTrackActivity extends AppCompatActivity implements TopTracksFragment.Callback {

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

        TopTracksFragment topTracksFragment;
        if (savedInstanceState == null) {
            topTracksFragment = new TopTracksFragment();
            topTracksFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, topTracksFragment, TopTracksFragment.TAG)
                    .commit();
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
    public void onListItemClick(int track) {
        PlayerFragment playerFragment = (PlayerFragment) getSupportFragmentManager()
                .findFragmentByTag(PlayerFragment.TAG);

        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
        }

        Bundle args = new Bundle();
        args.putInt(PlayerFragment.KEY_TRACK_INDEX, track);
        playerFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.detail_container, playerFragment, PlayerFragment.TAG)
                .addToBackStack(null)
                .commit();
        MediaPlayerService.startActionPlay(this, track);
    }
}
