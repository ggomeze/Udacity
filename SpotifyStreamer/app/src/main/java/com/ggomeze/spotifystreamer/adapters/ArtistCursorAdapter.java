package com.ggomeze.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by ggomeze on 05/07/15.
 */
public class ArtistCursorAdapter extends CursorAdapter {

    public ArtistCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.artist_item, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ParcelableArtist artist = convertCursorRowToParseableArtist(cursor);

        TextView artistName = (TextView) view.findViewById(R.id.artist_list_textview);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);

        artistName.setText(artist.name);
        String thumbnailUrl = artist.mThumbnailUrl;
        if (thumbnailUrl.isEmpty()) {
            Picasso.with(context).load(R.drawable.artist_placeholder).into(avatar);
        } else {
            Picasso.with(context).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(avatar);
        }
    }

    private ParcelableArtist convertCursorRowToParseableArtist(Cursor cursor) {
        Artist artist = new Artist();
        artist.id = cursor.getString(ArtistContract.ArtistEntry.COL_SPOTIFY_ARTIST_ID_INDEX);
        artist.name = cursor.getString(ArtistContract.ArtistEntry.COL_ARTIST_NAME_INDEX);
        ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
        parcelableArtist.mThumbnailUrl = cursor.getString(ArtistContract.ArtistEntry.COL_ARTIST_IMAGE_THUMB_INDEX);
        return parcelableArtist;
    }

}