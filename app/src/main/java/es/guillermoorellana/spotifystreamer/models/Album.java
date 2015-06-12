package es.guillermoorellana.spotifystreamer.models;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;

/**
 * Created by guillermo on 12/06/2015.
 */
public class Album implements Serializable {

    public String name;
    public List<Image> images;

    public static Album fromSpotify(AlbumSimple album) {
        Album result = new Album();

        result.name = album.name;
        result.images = Image.fromSpotifyList(album.images);

        return result;
    }
}
