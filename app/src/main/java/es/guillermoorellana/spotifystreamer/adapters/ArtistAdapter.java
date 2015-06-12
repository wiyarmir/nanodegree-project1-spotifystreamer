package es.guillermoorellana.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import es.guillermoorellana.spotifystreamer.R;
import es.guillermoorellana.spotifystreamer.models.Artist;

/**
 * Created by wiyarmir on 07/06/2015.
 */


public class ArtistAdapter extends BaseAdapter {
    private final Context context;
    int resource;
    List<Artist> items;

    public ArtistAdapter(Context context, int resource) {
        this.context = context;
        this.resource = resource;
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Artist getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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
                        .placeholder(R.drawable.no_album_art)
                        .resizeDimen(R.dimen.album_cover_side, R.dimen.album_cover_side)
                        .into(viewHolder.icon);
            } else {
                viewHolder.icon.setImageResource(R.drawable.no_album_art);
            }
        }

        return convertView;
    }

    public Context getContext() {
        return context;
    }

    public List<Artist> getValues() {
        return items;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Artist> artists) {
        items.addAll(artists);
    }

    static class ViewHolder {
        ImageView icon;
        TextView text1;
    }

}
