package es.guillermoorellana.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.List;

import es.guillermoorellana.spotifystreamer.adapters.ArtistAdapter;
import es.guillermoorellana.spotifystreamer.models.Artist;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    public static final String KEY_ARTIST_ID = "artist_id";
    private static final String STATE_SEARCHBAR_TEXT = "inputText";
    private static final String STATE_LISTVIEW_DATA = "listData";
    private final int TRIGGER_SERACH = 1;
    private final long SEARCH_TRIGGER_DELAY_IN_MS = 1000;
    private ListView results;
    private ArtistAdapter adapter;
    private EditText input;
    private SpotifyApi api;
    private boolean requestOngoing = false;
    private String nextRequest = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SERACH) {
                performRequest(msg.getData().getString(STATE_SEARCHBAR_TEXT, ""));
            }
        }
    };

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

            }

            @Override
            public void afterTextChanged(final Editable searchString) {
                /*
                   Nice solution to avoid million requests
                   Found at http://stackoverflow.com/a/10224817/1322722
                 */
                handler.removeMessages(TRIGGER_SERACH);
                Message msg = new Message();
                msg.what = TRIGGER_SERACH;
                Bundle data = new Bundle();
                data.putString(STATE_SEARCHBAR_TEXT, searchString.toString());
                msg.setData(data);
                handler.sendMessageDelayed(msg, SEARCH_TRIGGER_DELAY_IN_MS);
            }
        });
    }

    private void performRequest(final CharSequence searchString) {

        if (requestOngoing) {
            nextRequest = searchString.toString();
            return;
        }
        String searchTerm;
        if (nextRequest != null) {
            searchTerm = nextRequest;
            nextRequest = null;
        } else {
            searchTerm = searchString.toString();
        }

        if (searchTerm.length() == 0) {
            adapter.clear();
            nextRequest = null;
            return;
        }

        requestOngoing = true;

        api.getService().searchArtists(searchTerm, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager artistsPager, Response response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        if (artistsPager.artists.items.size() > 0) {
                            adapter.addAll(Artist.fromObjectArray(artistsPager.artists.items));
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    String.format("Can't find the artist '%s', please " +
                                            "try something different!", searchString),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        if (nextRequest != null) {
                            performRequest(nextRequest);
                        }
                    }
                });
                requestOngoing = false;

            }


            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(
                        MainActivity.this,
                        String.format("Can't find the artist '%s', please check your " +
                                "connection!", searchString),
                        Toast.LENGTH_SHORT
                ).show();
                requestOngoing = false;
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
        requestOngoing = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putAll(getStateBundle());
        requestOngoing = true; // avoid making an extra network call when we restore
        super.onSaveInstanceState(outState);
    }

    private Bundle getStateBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_SEARCHBAR_TEXT, input.getText().toString());
        bundle.putSerializable(STATE_LISTVIEW_DATA, adapter.getValues().toArray());
        return bundle;
    }

    private void restoreStateFromBundle(Bundle savedInstanceState) {
        input.setText(savedInstanceState.getString(STATE_SEARCHBAR_TEXT, ""));
        Object[] obj = (Object[]) savedInstanceState.getSerializable(STATE_LISTVIEW_DATA);
        List<Artist> artistList = Artist.fromObjectArray(obj);
        adapter.clear();
        adapter.addAll(artistList);
    }
}
