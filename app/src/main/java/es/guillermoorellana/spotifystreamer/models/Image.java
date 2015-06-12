package es.guillermoorellana.spotifystreamer.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillermo on 12/06/2015.
 */
public class Image {
    public String url;

    public static List<Image> fromSpotifyList(List<kaaes.spotify.webapi.android.models.Image> imageList) {
        List<Image> result = new ArrayList<>(imageList.size());
        for (kaaes.spotify.webapi.android.models.Image externalImage : imageList) {
            result.add(Image.fromSpotifyList(externalImage));
        }
        return result;
    }

    private static Image fromSpotifyList(kaaes.spotify.webapi.android.models.Image externalImage) {
        Image result = new Image();

        result.url = externalImage.url;

        return result;
    }
}
