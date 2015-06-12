package es.guillermoorellana.spotifystreamer.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillermo on 12/06/2015.
 */
public class Artist implements Serializable {
    public String name;
    public List<Image> images;
    public String id;

    static Artist fromObjectArray(kaaes.spotify.webapi.android.models.Artist artist) {
        Artist result = new Artist();

        result.id = artist.id;
        result.name = artist.name;
        result.images = Image.fromSpotifyList(artist.images);

        return result;
    }

    public static List<Artist> fromObjectArray(List<kaaes.spotify.webapi.android.models.Artist>
                                                       artistList) {
        List<Artist> result = new ArrayList<>(artistList.size());
        for (kaaes.spotify.webapi.android.models.Artist externalArtist : artistList) {
            result.add(Artist.fromObjectArray(externalArtist));
        }
        return result;
    }

    public static List<Artist> fromObjectArray(Object[] array) {
        List<Artist> result = new ArrayList<>(array.length);
        for (Object item : array) {
            result.add((Artist) item);
        }
        return result;
    }
}
