package es.guillermoorellana.spotifystreamer.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillermo on 12/06/2015.
 */
public class Track implements Serializable {
    public String name;
    public Album album;

    public static List<Track> fromSpotifyList(List<kaaes.spotify.webapi.android.models.Track> trackList) {
        List<Track> result = new ArrayList<>(trackList.size());
        for (kaaes.spotify.webapi.android.models.Track externalTrack : trackList) {
            result.add(Track.fromSpotifyList(externalTrack));
        }
        return result;
    }

    private static Track fromSpotifyList(kaaes.spotify.webapi.android.models.Track externalTrack) {
        Track result = new Track();

        result.name = externalTrack.name;
        result.album = Album.fromSpotify(externalTrack.album);

        return result;
    }
}
