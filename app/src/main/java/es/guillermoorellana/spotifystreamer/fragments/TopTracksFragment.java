package es.guillermoorellana.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import es.guillermoorellana.spotifystreamer.MainActivity;
import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.adapters.TrackAdapter;

/**
 * Created by guillermo on 18/06/2015.
 */
public class TopTracksFragment extends ListFragment implements NetworkFragment.OnTracksResultListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new TrackAdapter(getActivity(), R.layout.listitem_track));
        setListShown(false);
        if (savedInstanceState != null) {
            addTracks();
        }
    }

    public void onListItemClick(ListView listView, View view, int position, long id) {

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

}
