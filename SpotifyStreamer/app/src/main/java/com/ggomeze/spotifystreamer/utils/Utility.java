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

package com.ggomeze.spotifystreamer.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class Utility {

    public static long artistExist (Context context, String spotifyArtistId) {
        long artistRowId = -1L;
        // First, check if the artist with this artist_id exists in the db
        Cursor cursor = context.getContentResolver().query(
                ArtistContract.ArtistEntry.CONTENT_URI,
                null,   // projection
                ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",   //where clause
                new String[]{spotifyArtistId},   // Values for the "where" clause
                null    // sort order
        );
        if (cursor.moveToFirst()) {// If it exists, return the current ID
            artistRowId = cursor.getLong(cursor.getColumnIndex(ArtistContract.ArtistEntry._ID));
        }
        cursor.close();
        return artistRowId;
    }

    public static long trackExist (Context context, String trackId) {
        long trackRowId = -1L;
        // First, check if the artist with this artist_id exists in the db
        Cursor cursor = context.getContentResolver().query(
                TrackContract.TrackEntry.CONTENT_URI,
                null,   // projection
                TrackContract.TrackEntry.COLUMN_TRACK_ID + " = ?",   //where clause
                new String[]{trackId},   // Values for the "where" clause
                null    // sort order
        );
        if (cursor.moveToFirst()) {// If it exists, return the current ID
            trackRowId = cursor.getLong(cursor.getColumnIndex(TrackContract.TrackEntry._ID));
        }
        cursor.close();
        return trackRowId;
    }

    /**
     * Helper method to handle insertion of a new track in the database.
     *
     * @param aTrack Track to be inserted
     * @return the row ID of the added track.
     */
    public static long addTrack(Context context, Track aTrack) {
        long trackId = -1;
        // First, check if the track with this track_id exists in the db
        trackId = Utility.trackExist(context, aTrack.id);
        if (trackId < 0) {// Doesn't exist
            Uri insertedTrackUri = context.getContentResolver().insert(TrackContract.TrackEntry.CONTENT_URI,
                    new ParcelableTrack(aTrack).getTrackContentValues());
            trackId = ContentUris.parseId(insertedTrackUri);
        }
        return trackId;
    }

    /**
     * Helper method to handle insertion of a new artist in the database.
     *
     * @param spotifyArtistId The potify artist id.
     * @param artistName Artist/Group name like "Coldplay"
     * @param imageThumb URL of the image that identify the group/artist in Spotify
     * @return the row ID of the added location.
     */
    public static long addArtist(Context context, String spotifyArtistId, String artistName, String imageThumb) {
        long artistId = -1L;
        // First, check if the artist with this artist_id exists in the db
        artistId = Utility.artistExist(context, spotifyArtistId);
        if (artistId < 0) {// Doesn't exist
            ContentValues artistValues = new ContentValues();
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, artistName);
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID, spotifyArtistId);
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB, imageThumb);
            Uri insertedArtistUri = context.getContentResolver().insert(ArtistContract.ArtistEntry.CONTENT_URI, artistValues);
            artistId = ContentUris.parseId(insertedArtistUri);
        }
        return artistId;
    }

    /**
     * @param artists List of artists
     * @return an array of ContentValues with artists information
     */
    public static ContentValues[] getContentValuesFromArtistList(List<Artist> artists) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(artists.size());
        for (Artist artist : artists) {
            ParcelableArtist parcelableArtist = new ParcelableArtist(artist);
            ContentValues artistValues = new ContentValues();
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, artist.name);
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID, artist.id);
            artistValues.put(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB, parcelableArtist.getThumbnailUrl());
            cVVector.add(artistValues);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        return cVVector.toArray(cvArray);
    }

    /**
     * Convert list of tracks to a ContentValues[]. If an artistId is passed as parameter, it also links those tracks
     * to the specified artist Id
     * @param tracks List of tracks
     * @param artistId Optional field for linking tracks to artistId
     * @return an array of ContentValues with tracks information
     */
    public static ContentValues[] getContentValuesFromTrackList(List<Track> tracks, long artistId) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(tracks.size());
        for (Track track : tracks) {
            ParcelableTrack parcelableTrack = new ParcelableTrack(track);
            ContentValues trackValues = new ContentValues();
            trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_NAME, track.name);
            trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, track.id);
            trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_URL, track.preview_url);
            trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_MED, parcelableTrack.getThumbnailUrl());
            trackValues.put(TrackContract.TrackEntry.COLUMN_ALBUM_NAME, track.album.name);
            trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB, parcelableTrack.getThumbnailUrl());
            if (artistId > 0)
                trackValues.put(TrackContract.TrackEntry.COLUMN_ARTIST_FOREIGN_KEY, artistId);
            cVVector.add(trackValues);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        return cVVector.toArray(cvArray);
    }

    public static String convertMillisToText(int millis) {
        return String.format("0:%02d",
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

}