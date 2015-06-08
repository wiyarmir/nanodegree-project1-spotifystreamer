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

import es.guillermoorellana.spotifystreamer.adapters.ArtistAdapter;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    public static final String KEY_ARTIST_ID = "artist_id";
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    adapter.clear();
                    return;
                }
                SpotifyService svc = api.getService();
                svc.searchArtists(s.toString(), new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, Response response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                adapter.addAll(artistsPager.artists.items);
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

}
