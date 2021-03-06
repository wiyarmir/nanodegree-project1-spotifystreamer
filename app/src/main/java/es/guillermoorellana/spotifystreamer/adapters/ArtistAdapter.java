package es.guillermoorellana.spotifystreamer.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import es.guillermoorellana.spotifystreamer.R;
import kaaes.spotify.webapi.android.models.Artist;

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
            viewHolder = new ViewHolder(convertView);
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            items.addAll(artists);
        } else {
            for (Artist artist : artists) {
                items.add(artist);
            }
        }
        items.addAll(artists);
    }

    static class ViewHolder {
        @InjectView(android.R.id.icon) ImageView icon;
        @InjectView(android.R.id.text1) TextView text1;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
