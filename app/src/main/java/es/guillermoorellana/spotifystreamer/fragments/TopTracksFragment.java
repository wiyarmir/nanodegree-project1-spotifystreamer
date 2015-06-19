package es.guillermoorellana.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.adapters.TrackAdapter;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by guillermo on 18/06/2015.
 */
public class TopTracksFragment extends ListFragment implements NetworkFragment.OnTracksResultListener {
    public static final String TAG = "TopTracks";

    public interface Callback {
        void onListItemClick(Track track);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new TrackAdapter(getActivity(), R.layout.listitem_track));
        if (savedInstanceState != null) {
            addTracks();
        }

        Bundle arguments = getArguments();
        String artistId;
        if (arguments != null) {
            setListShown(false);

            artistId = arguments.getString(ArtistFragment.KEY_ARTIST_ID);

            // avoid double calls
            if (!artistId.equals(MainActivity.getNetworkFragment().getCurrentArtistId())) {
                MainActivity.getNetworkFragment().searchTopTracks(artistId);
            }
        } else {
            setEmptyText("");
        }
    }

    public void onListItemClick(ListView listView, View view, int position, long id) {
        Track track = (Track) listView.getAdapter().getItem(position);
        if (track != null) {
            ((Callback) getActivity()).onListItemClick(track);
        }
    }

    @Override
    public void onNetworkSuccess() {
        getTrackAdapter().clear();
        addTracks();
    }

    @Override
    public void onNetworkError(String message) {
        getTrackAdapter().clear();
        getActivity().finish();
    }

    private void addTracks() {
        getTrackAdapter().addAll(MainActivity.getNetworkFragment().getTopTrackList());
        setListShown(true);
        setEmptyText(getActivity().getString(R.string.tracklist_empty));
    }

    private TrackAdapter getTrackAdapter() {
        return (TrackAdapter) getListAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.getNetworkFragment().setOnTracksResultListener(this);
    }

    @Override
    public void onStop() {
        MainActivity.getNetworkFragment().setOnTracksResultListener(null);
        super.onStop();
    }

}
