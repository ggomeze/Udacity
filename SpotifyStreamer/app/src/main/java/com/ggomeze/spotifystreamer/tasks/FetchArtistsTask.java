package com.ggomeze.spotifystreamer.tasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.adapters.ArtistAdapter;
import com.ggomeze.spotifystreamer.adapters.TrackAdapter;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

    private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

    private WeakReference<ArtistAdapter> mArtistAdapter;
    private WeakReference<ArrayList<ParcelableArtist>> mReturnedArtists;
    private WeakReference<Context> mContext;

    public FetchArtistsTask(Context context, ArtistAdapter artistAdapter, ArrayList<ParcelableArtist> returnedArtists){
        mContext = new WeakReference<Context>(context);
        mArtistAdapter = new WeakReference<ArtistAdapter>(artistAdapter);
        mReturnedArtists = new WeakReference<ArrayList<ParcelableArtist>>(returnedArtists);
    }

    /**
     * Helper method to handle insertion of a new artist in the database.
     *
     * @param spotifyArtistId The potify artist id.
     * @param artistName Artist/Group name like "Coldplay"
     * @param imageThumb URL of the image that identify the group/artist in Spotify
     * @return the row ID of the added location.
     */
    long addArtist(String spotifyArtistId, String artistName, String imageThumb) {
        long artistId = -1;
        Context context = mContext.get();
        if (context != null) {
            // First, check if the artist with this artist_id exists in the db
            Cursor cursor = context.getContentResolver().query(
                    ArtistContract.ArtistEntry.CONTENT_URI,
                    null,   // projection
                    ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",   //where clause
                    new String[]{spotifyArtistId},   // Values for the "where" clause
                    null    // sort order
            );
            if (cursor.moveToFirst()) {// If it exists, return the current ID
                artistId = cursor.getLong(cursor.getColumnIndex(ArtistContract.ArtistEntry._ID));
                cursor.close();
            } else {// Otherwise, insert it using the content resolver and the base URI
                ContentValues artistValues = new ContentValues();
                artistValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, artistName);
                artistValues.put(ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID, spotifyArtistId);
                artistValues.put(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB, imageThumb);
                Uri insertedArtistUri = context.getContentResolver().insert(ArtistContract.ArtistEntry.CONTENT_URI, artistValues);
                artistId = ContentUris.parseId(insertedArtistUri);
            }
        }
        return artistId;
    }
    @Override
    protected void onPreExecute() {

    }
    @Override
    protected List<Artist> doInBackground(String... artists) {
        if(artists.length == 0) {
            return null;
        }
        String artistString = artists[0];
        SpotifyService spotify = new SpotifyApi().getService();
        try {
            ArtistsPager results = spotify.searchArtists(artistString);
            return results.artists.items;
        } catch (Exception exception) {
            Context context = mContext.get();
            if (context != null) Log.e(LOG_TAG, context.getString(R.string.connection_error));
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<Artist> returnedArtists) {
        ArtistAdapter artistAdapter = mArtistAdapter.get();
        ArrayList<ParcelableArtist> returnedArtistsArray = mReturnedArtists.get();

        if (artistAdapter == null || returnedArtistsArray == null) return;
        Context context = mContext.get();
        if (returnedArtists != null) {
            if (returnedArtists.size() > 0) {
                artistAdapter.clear();
                if (context != null) {
                    for (Artist artist : returnedArtists) {
                        ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
                        returnedArtistsArray.add(parcelableArtist);
                        addArtist(parcelableArtist.id, parcelableArtist.name, parcelableArtist.getThumbnailUrl());
                    }
                }
            } else {
                if (context != null) {
                    Toast.makeText(context, context.getString(R.string.no_artists_found), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}