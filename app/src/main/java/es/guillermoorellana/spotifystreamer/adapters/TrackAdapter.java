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
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by wiyarmir on 08/06/2015.
 */
public class TrackAdapter extends BaseAdapter {


    private final int resource;
    private final Context context;
    private final ArrayList<Track> items;


    public TrackAdapter(Context context, int resource) {
        this.resource = resource;
        this.context = context;
        items = new ArrayList<>();
    }

    public void clear() {
        items.clear();
    }

    static class ViewHolder {
        @InjectView(android.R.id.icon) ImageView icon;
        @InjectView(android.R.id.text1) TextView text1;
        @InjectView(android.R.id.text2) TextView text2;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = items.get(position);

        if (track != null) {
            viewHolder.text1.setText(track.name);
            viewHolder.text2.setText(track.album.name);
            if (track.album.images.size() > 0) {
                Picasso.with(context)
                        .load(track.album.images.get(0).url)
                        .placeholder(R.drawable.no_album_art)
                        .resizeDimen(R.dimen.album_cover_side, R.dimen.album_cover_side)
                        .into(viewHolder.icon);
            } else {
                viewHolder.icon.setImageResource(R.drawable.no_album_art);
            }
        }

        return convertView;
    }

    public void addAll(List<Track> tracks) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            items.addAll(tracks);
        } else {
            for (Track track : tracks) {
                items.add(track);
            }
        }
    }
}
