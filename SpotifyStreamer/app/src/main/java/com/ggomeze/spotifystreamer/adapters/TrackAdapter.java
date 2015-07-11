package com.ggomeze.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ggomeze on 05/07/15.
 */
public class TrackAdapter extends ArrayAdapter<ParcelableTrack> {

    private static class ViewHolder {
        private TextView trackName;
        private TextView albumName;
        private ImageView avatar;
    }

    public TrackAdapter(Context context, int textViewResourceId, List<ParcelableTrack> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.track_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.trackName = (TextView) convertView.findViewById(R.id.track_name);
            viewHolder.albumName = (TextView) convertView.findViewById(R.id.album_name);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.album_avatar);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ParcelableTrack track = getItem(position);
        if (track!= null) {
            viewHolder.trackName.setText(track.name);
            viewHolder.albumName.setText(track.album.name);
            String thumbnailUrl = track.getThumbnailUrl();
            if (thumbnailUrl.isEmpty()) {
                Picasso.with(this.getContext()).load(R.drawable.artist_placeholder).into(viewHolder.avatar);
            } else {
                Picasso.with(this.getContext()).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(viewHolder.avatar);
            }
        }

        return convertView;
    }
}