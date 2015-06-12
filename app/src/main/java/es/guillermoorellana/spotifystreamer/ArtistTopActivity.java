package es.guillermoorellana.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

import es.guillermoorellana.spotifystreamer.adapters.TrackAdapter;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ArtistTopActivity extends AppCompatActivity {

    private static String artistId;
    private static SpotifyService spotiservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistId = getIntent().getExtras().getString(MainActivity.KEY_ARTIST_ID, null);
        if (artistId == null) {
            Toast.makeText(this, "ERROR: No artist provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        spotiservice = new SpotifyApi().getService();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SongListFragment fragment = new SongListFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }

            default: {
                break;
            }
        }
        return true;
    }

    public static class SongListFragment extends ListFragment {

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Map<String, Object> paramMap = new ArrayMap<>();
            paramMap.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
            spotiservice.getArtistTopTrack(artistId, paramMap, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, Response response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tracks.tracks.size() > 0) {
                                setListAdapter(
                                        new TrackAdapter(
                                                getActivity(),
                                                R.layout.listitem_track,
                                                tracks.tracks
                                        )
                                );
                            } else {
                                Toast.makeText(getActivity(), "Can't find any top tracks for this" +
                                        " artist. Sorry!", Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    getActivity().finish();
                }
            });
        }

        public void onListItemClick(ListView listView, View view, int position, long id) {

        }
    }


}
