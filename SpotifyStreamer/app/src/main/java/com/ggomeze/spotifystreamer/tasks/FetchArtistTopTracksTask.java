/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ggomeze.spotifystreamer.tasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.adapters.TrackAdapter;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class FetchArtistTopTracksTask extends AsyncTask<String, Void, Tracks> {

    private final String LOG_TAG = FetchArtistTopTracksTask.class.getSimpleName();

    private WeakReference<TrackAdapter> mTrackAdapter;
    private WeakReference<ArrayList<ParcelableTrack>> mReturnedTracks;
    private WeakReference<Context> mContext;

    private String mSpotifyArtistId;

    public FetchArtistTopTracksTask(Context context, TrackAdapter trackAdapter, ArrayList<ParcelableTrack> returnedTracks) {
        mContext = new WeakReference<Context>(context);
        mTrackAdapter = new WeakReference<TrackAdapter>(trackAdapter);
        mReturnedTracks = new WeakReference<ArrayList<ParcelableTrack>>(returnedTracks);
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new track in the database.
     *
     * @param aTrack Track to be inserted
     * @return the row ID of the added track.
     */
    long addTrack(Track aTrack) {
        long trackId = -1;
        Context context = mContext.get();
        if (context != null) {
            // First, check if the artist with this artist_id exists in the db
            Cursor cursor = context.getContentResolver().query(
                    TrackContract.TrackEntry.CONTENT_URI,
                    null,   // projection
                    TrackContract.TrackEntry.COLUMN_TRACK_ID + " = ?",   //where clause
                    new String[]{aTrack.id},   // Values for the "where" clause
                    null    // sort order
            );
            if (cursor.moveToFirst()) {// If it exists, return the current ID
                trackId = cursor.getLong(cursor.getColumnIndex(TrackContract.TrackEntry._ID));
                cursor.close();
            } else {// Otherwise, insert it using the content resolver and the base URI
                Uri insertedArtistUri = context.getContentResolver().insert(TrackContract.TrackEntry.CONTENT_URI,
                        new ParcelableTrack(aTrack).getTrackContentValues());
                trackId = ContentUris.parseId(insertedArtistUri);
            }
        }
        return trackId;
    }

    @Override
    protected void onPreExecute() {

    }
    @Override
    protected Tracks doInBackground(String... artistsIDs) {
        Context context = mContext.get();
        if(artistsIDs.length == 0 || context == null) {
            return null;
        }
        mSpotifyArtistId = artistsIDs[0];

        SpotifyService spotify = new SpotifyApi().getService();
        Map artistTopTracksParams = new HashMap<String,String>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        artistTopTracksParams.put(context.getString(R.string.spotify_country_param), sharedPref.getString(context.getString(R.string.pref_country_key), "US"));
        try {
            return spotify.getArtistTopTrack(mSpotifyArtistId, artistTopTracksParams);
        } catch (Exception exception) {
            Log.e(LOG_TAG, context.getString(R.string.connection_error));
            return null;
        }
    }
    @Override
    protected void onPostExecute(Tracks returnedTracks) {
        TrackAdapter trackAdapter = mTrackAdapter.get();
        ArrayList<ParcelableTrack> returnedTracksArray = mReturnedTracks.get();

        if (trackAdapter == null || returnedTracksArray == null) return;
        Context context = mContext.get();

        if (returnedTracks != null) {
            if (returnedTracks.tracks.size() > 0) {
                trackAdapter.clear();
                if (context != null) {
                    Cursor cursor = context.getContentResolver().query(
                            ArtistContract.ArtistEntry.CONTENT_URI,
                            new String[]{ArtistContract.ArtistEntry._ID},   // projection
                            ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",
                            new String[]{mSpotifyArtistId},   // Values for the "where" clause
                            null    // sort order
                    );
                    if (cursor.moveToNext()){
                        long artistId = cursor.getLong(cursor.getColumnIndex(ArtistContract.ArtistEntry._ID));
                        cursor.close();
                        for (Track track : returnedTracks.tracks) {
                            ParcelableTrack parcelableTrack = new ParcelableTrack(track);
                            returnedTracksArray.add(parcelableTrack);
                            addTrack(track);
                        }
                    }
                }
            } else {
                if (context != null) {
                    Toast.makeText(context, context.getString(R.string.no_tracks_found), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }


}