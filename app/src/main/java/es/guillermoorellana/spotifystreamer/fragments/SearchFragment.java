package es.guillermoorellana.spotifystreamer.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import es.guillermoorellana.spotifystreamer.R;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends Fragment {

    private final SpotifyApi api;
    private EditText input;
    private ListView results;

    public SearchFragment() {
        api = new SpotifyApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        results = (ListView) view.findViewById(R.id.results);
        results.setAdapter(new ArrayAdapter<Artist>(getActivity(), android.R.layout.simple_list_item_1));

        input = (EditText) view.findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SpotifyService svc = api.getService();
                svc.searchArtists(s.toString(), new Callback<ArtistsPager>() {
                    @Override
                    public void success(final ArtistsPager artistsPager, Response response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ArrayAdapter<Artist>) results.getAdapter()).clear();
                                ((ArrayAdapter<Artist>) results.getAdapter()).addAll(artistsPager.artists.items);
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

        return view;
    }
}
