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
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ggomeze on 05/07/15.
 */
public class TrackCursorAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView trackName;
        public final TextView albumName;
        public final ImageView avatar;

        public ViewHolder(View view) {
            trackName = (TextView) view.findViewById(R.id.track_name);
            albumName = (TextView) view.findViewById(R.id.album_name);
            avatar = (ImageView) view.findViewById(R.id.album_avatar);
        }
    }

    public TrackCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ParcelableTrack track = convertCursorRowToParseableTrack(cursor);

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.trackName.setText(track.name);
        viewHolder.albumName.setText(track.album.name);

        String thumbnailUrl = track.mThumbnailUrl;
        if (thumbnailUrl.isEmpty()) {
            Picasso.with(context).load(R.drawable.artist_placeholder).into(viewHolder.avatar);
        } else {
            Picasso.with(context).load(track.mThumbnailUrl).placeholder(R.drawable.artist_placeholder).into(viewHolder.avatar);
        }
    }

    private ParcelableTrack convertCursorRowToParseableTrack(Cursor cursor) {
        Track track = new Track();
        track.album = new Album();
        track.id = cursor.getString(TrackContract.TrackEntry.COL_TRACK_ID_INDEX);
        track.name = cursor.getString(TrackContract.TrackEntry.COL_TRACK_NAME_INDEX);
        track.album.name = cursor.getString(TrackContract.TrackEntry.COL_ALBUM_NAME_INDEX);
        track.preview_url = cursor.getString(TrackContract.TrackEntry.COL_TRACK_URL_INDEX);
        ParcelableTrack parcelableTrack = new ParcelableTrack(track);
        parcelableTrack.mThumbnailUrl = cursor.getString(TrackContract.TrackEntry.COL_TRACK_IMAGE_THUMB_INDEX);
        return parcelableTrack;
    }

}