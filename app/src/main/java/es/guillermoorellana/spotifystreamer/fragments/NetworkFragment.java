package es.guillermoorellana.spotifystreamer.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.adapters.TrackAdapter;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by guillermo on 18/06/2015.
 */
public class NetworkFragment extends Fragment {
    public static final String TAG = "NetworkFragment";

    private final SpotifyService spotify;
    private ArrayList<Artist> artistList;
    private ArrayList<Track> topTrackList;
    private OnTracksResultListener onTracksResult;
    private OnArtistsResultListener onArtistsResult;
    private String currentArtist;
    private String currentArtistId;


    public String getCurrentArtistName() {
        return currentArtist;
    }

    public List<Artist> getArtistList() {
        return artistList;
    }

    public List<Track> getTopTrackList() {
        return topTrackList;
    }

    public String getCurrentArtistId() {
        return currentArtistId;
    }

    public interface OnArtistsResultListener {
        void onNetworkSuccess();

        void onNetworkError(String message);
    }

    public interface OnTracksResultListener {
        void onNetworkSuccess();

        void onNetworkError(String message);
    }

    public void setOnArtistsResultListener(OnArtistsResultListener listener) {
        this.onArtistsResult = listener;
    }

    public void setOnTracksResultListener(OnTracksResultListener listener) {
        this.onTracksResult = listener;
    }

    public NetworkFragment() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
        artistList = new ArrayList<>();
        topTrackList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void searchArtists(final String artist) {
        currentArtist = artist;

        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                if (!artist.equals(currentArtist)) {
                    return; // late response
                }

                artistList.clear();

                artistList.addAll(artistsPager.artists.items);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onArtistsResult != null) {
                            onArtistsResult.onNetworkSuccess();
                        }
                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                if (!artist.equals(currentArtist)) {
                    return;
                }
                artistList.clear();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onArtistsResult != null) {
                            onArtistsResult.onNetworkError(error.getMessage());
                        }
                    }
                });
            }
        });
    }

    public void searchTopTracks(String artistId) {
        currentArtistId = artistId;

        Map<String, Object> paramMap = new ArrayMap<>();
        paramMap.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
        spotify.getArtistTopTrack(artistId, paramMap, new Callback<Tracks>() {
            @Override
            public void success(final Tracks tracks, Response response) {
                topTrackList.clear();
                topTrackList.addAll(tracks.tracks);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onTracksResult != null) {
                            onTracksResult.onNetworkSuccess();
                        }
                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                topTrackList.clear();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onTracksResult != null) {
                            onTracksResult.onNetworkError(error.getMessage());
                        }
                    }
                });
            }
        });
    }

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int ICON_BITMAP_INDEX = 1;

    public void fetch(final String artUrl, final FetchListener listener) {
        new AsyncTask<Void, Void, Bitmap[]>() {
            @Override
            protected Bitmap[] doInBackground(Void[] objects) {
                Bitmap[] bitmaps;
                try {
                    RequestCreator load = Picasso.with(getActivity()).load(artUrl);
                    bitmaps = new Bitmap[]{load.get(), load.resize(160, 160).get()};
                } catch (IOException e) {
                    return null;
                }
                return bitmaps;
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if (bitmaps == null) {
                    listener.onError(artUrl, new IllegalArgumentException("got null bitmaps"));
                } else {
                    listener.onFetched(artUrl,
                            bitmaps[BIG_BITMAP_INDEX], bitmaps[ICON_BITMAP_INDEX]);
                }
            }
        }.execute();
    }


    public static abstract class FetchListener {
        public abstract void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage);

        public void onError(String artUrl, Exception e) {
            Log.e(TAG, "AlbumArtFetchListener: error while downloading " + artUrl, e);
        }
    }
}
