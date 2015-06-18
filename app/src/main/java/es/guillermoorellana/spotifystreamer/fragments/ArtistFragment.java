package es.guillermoorellana.spotifystreamer.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import es.guillermoorellana.spotifystreamer.TopTrackActivity;
import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.adapters.ArtistAdapter;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment implements NetworkFragment.OnArtistsResultListener {

    public static final String KEY_ARTIST_ID = "artist_id";
    private ListView results;
    private ArtistAdapter adapter;
    private EditText input;
    private TextView empty;

    public ArtistFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_layout, container, false);
        results = (ListView) view.findViewById(R.id.results);
        adapter = new ArtistAdapter(getActivity(), R.layout.listitem_artist);

        results.setAdapter(adapter);

        input = (EditText) view.findViewById(R.id.input);

        empty = (TextView) view.findViewById(R.id.empty);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    adapter.clear();
                    return;
                }
                if (!s.toString().equals(MainActivity.getNetworkFragment()
                        .getCurrentArtistName
                                ())) {
                    MainActivity.getNetworkFragment().searchArtists(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getItemAtPosition(position);
                Intent i = new Intent(getActivity(), TopTrackActivity.class);
                i.putExtra(KEY_ARTIST_ID, artist.id);
                startActivity(i);
            }
        });
    }

    private void addArtists() {
        adapter.addAll(MainActivity.getNetworkFragment().getArtistList());
    }

    @Override
    public void onNetworkSuccess() {
        adapter.clear();
        addArtists();
        if (adapter.getCount() == 0) {
            results.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
            results.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkError(String message) {
        adapter.clear();
        Toast.makeText(
                getActivity(),
                "Can't find the artist you are looking for, please check your connection!",
                Toast.LENGTH_SHORT
        ).show();
    }
}

