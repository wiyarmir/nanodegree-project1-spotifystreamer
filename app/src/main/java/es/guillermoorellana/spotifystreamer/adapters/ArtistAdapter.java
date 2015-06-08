package es.guillermoorellana.spotifystreamer.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import es.guillermoorellana.spotifystreamer.R;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by wiyarmir on 07/06/2015.
 */


public class ArtistAdapter extends ArrayAdapter<Artist> {


    static class ViewHolder {
        ImageView icon;
        TextView text1;
    }

    int resource;

    public ArtistAdapter(Context context, int resource) {
        super(context, resource, android.R.id.text1);
        this.resource = resource;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Artist artist = getItem(position);

        if (artist != null) {
            viewHolder.text1.setText(artist.name);
            if (artist.images.size() > 0) {
                Picasso.with(getContext())
                        .load(artist.images.get(0).url)
                        .resizeDimen(R.dimen.album_cover_side, R.dimen.album_cover_side)
                        .into(viewHolder.icon);
            } else {
                viewHolder.icon.setImageBitmap(null);
            }
        }

        return convertView;
    }
}
