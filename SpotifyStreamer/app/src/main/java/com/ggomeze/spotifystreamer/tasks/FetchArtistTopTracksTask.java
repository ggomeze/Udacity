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

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.utils.Utility;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class FetchArtistTopTracksTask extends AsyncTask<Long, Void, List<Track>> {

    private final String LOG_TAG = FetchArtistTopTracksTask.class.getSimpleName();

    private WeakReference<Context> mContext;

    public FetchArtistTopTracksTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {

    }
    @Override
    protected List<Track> doInBackground(Long... artistsIDs) {
        Context context = mContext.get();
        if(artistsIDs.length == 0) {
            return null;
        }

        long mArtistId = artistsIDs[0];

        SpotifyService spotify = new SpotifyApi().getService();
        Map artistTopTracksParams = new HashMap<>();
        List<Track> returnedTracks = null;

        try {
            if (context != null) {
                artistTopTracksParams.put(context.getString(R.string.spotify_country_param), Utility.getCountryPreference(context));
                //Get spotifyId from database before calling Spotify
                Cursor cursor = context.getContentResolver().query(
                        ArtistContract.ArtistEntry.CONTENT_URI,
                        new String[]{ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID}, // leaving "columns" null just returns all the columns.
                        ArtistContract.ArtistEntry._ID + "= ?", // cols for "where" clause
                        new String[]{Long.toString(mArtistId)}, // values for "where" clause
                        null  // sort order
                );
                if(cursor.moveToNext()) {
                    returnedTracks = spotify.getArtistTopTrack(cursor.getString(0), artistTopTracksParams).tracks;
                    if (returnedTracks != null && returnedTracks.size() > 0) {
                        context.getContentResolver().bulkInsert(TrackContract.TrackEntry.buildTracksFromAnArtist(mArtistId),
                                Utility.getContentValuesFromTrackList(returnedTracks, mArtistId));
                    }
                }
            }
            return returnedTracks;
        } catch (Exception exception) {
            Log.e(LOG_TAG, context.getString(R.string.connection_error));
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<Track> returnedTracks) {
        Context context = mContext.get();
        if (returnedTracks != null) {
            if (returnedTracks.isEmpty()) {
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