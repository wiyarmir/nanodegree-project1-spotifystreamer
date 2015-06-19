package es.guillermoorellana.spotifystreamer.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;
import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.adapters.ArtistAdapter;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment implements NetworkFragment.OnArtistsResultListener {

    public static final String KEY_ARTIST_ID = "artist_id";
    public static final String TAG = "ArtistFragment";

    public interface Callback {
        void onItemSelected(String artistId);
    }

    @InjectView(R.id.results) ListView results;
    @InjectView(R.id.input) EditText input;
    @InjectView(R.id.empty) TextView empty;

    private ArtistAdapter adapter;

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

        ButterKnife.inject(this, view);

        adapter = new ArtistAdapter(getActivity(), R.layout.listitem_artist);

        if (savedInstanceState != null) {
            addArtists(); //restore state
        }

        results.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnItemClick(R.id.results)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Artist artist = (Artist) parent.getItemAtPosition(position);
        ((Callback) getActivity()).onItemSelected(artist.id);
    }

    @OnTextChanged(R.id.input)
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}

