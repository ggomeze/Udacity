package com.ggomeze.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by ggomeze on 05/07/15.
 */
public class ArtistAdapter extends ArrayAdapter<ParcelableArtist> {

    private static class ViewHolder {
        private TextView artistName;
        private ImageView avatar;
    }

    public ArtistAdapter(Context context, int textViewResourceId, ArrayList<ParcelableArtist> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.artist_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.artistName = (TextView) convertView.findViewById(R.id.artist_list_textview);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ParcelableArtist artist = getItem(position);
        if (artist!= null) {
            viewHolder.artistName.setText(artist.name);
            List<Image> images = artist.images;
            String thumbnailUrl = artist.getThumbnailUrl();
            if (thumbnailUrl.isEmpty()) {
                Picasso.with(this.getContext()).load(R.drawable.artist_placeholder).into(viewHolder.avatar);
            } else {
                Picasso.with(this.getContext()).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(viewHolder.avatar);
            }
        }

        return convertView;
    }
}