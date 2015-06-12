package es.guillermoorellana.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import es.guillermoorellana.spotifystreamer.adapters.ArtistAdapter;
import es.guillermoorellana.spotifystreamer.models.Artist;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    public static final String KEY_ARTIST_ID = "artist_id";
    private static final String STATE_SEARCHBAR_TEXT = "inputText";
    private ListView results;
    private ArtistAdapter adapter;
    private EditText input;
    private SpotifyApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new SpotifyApi();
        results = (ListView) findViewById(R.id.results);
        adapter = new ArtistAdapter(getApplicationContext(), R.layout.listitem_artist);
        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getItemAtPosition(position);
                Intent i = new Intent(MainActivity.this, ArtistTopActivity.class);
                i.putExtra(KEY_ARTIST_ID, artist.id);
                startActivity(i);
            }
        });
        results.setAdapter(adapter);

        input = (EditText) findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence searchString, int start, int before, int count) {
                if (searchString.length() == 0) {
                    adapter.clear();
                    return;
                }
                SpotifyService svc = api.getService();

                svc.searchArtists(searchString.toString(), new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, Response response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                if (artistsPager.artists.items.size() > 0) {
                                    adapter.addAll(Artist.fromSpotifyList(artistsPager.artists.items));
                                } else {
                                    Toast.makeText(
                                            MainActivity.this,
                                            String.format("Can't find the artist '%s', please try something different!", searchString),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        });
                    }


                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreStateFromBundle(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(getStateBundle());
        super.onSaveInstanceState(outState);
    }

    private Bundle getStateBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_SEARCHBAR_TEXT, input.getText().toString());
        return bundle;
    }

    private void restoreStateFromBundle(Bundle savedInstanceState) {
        input.setText(savedInstanceState.getString(STATE_SEARCHBAR_TEXT, ""));
    }
}
