package es.guillermoorellana.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import es.guillermoorellana.spotifystreamer.R;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by wiyarmir on 08/06/2015.
 */
public class TrackAdapter extends ArrayAdapter<Track> {


    private final int resource;

    public TrackAdapter(Context context, int resource, List<Track> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text1;
        TextView text2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.text2 = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);

        if (track != null) {
            viewHolder.text1.setText(track.name);
            viewHolder.text2.setText(track.album.name);
            if (track.album.images.size() > 0) {
                Picasso.with(getContext())
                        .load(track.album.images.get(0).url)
                        .resizeDimen(R.dimen.album_cover_side, R.dimen.album_cover_side)
                        .into(viewHolder.icon);
            } else {
                viewHolder.icon.setImageBitmap(null);
            }
        }

        return convertView;
    }

}
