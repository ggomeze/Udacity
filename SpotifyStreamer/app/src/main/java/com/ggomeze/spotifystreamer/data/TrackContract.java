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
package com.ggomeze.spotifystreamer.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Defines table and column names for the weather database.
 */
public class TrackContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.ggomeze.spotifystreamer";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.ggomeze.spotifystreamer/tracks/ is a valid path for
    // tracks. content://com.ggomeze.spotifystreamer/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_TRACKS = "tracks";

    /* Inner class that defines the table contents of the artists table */
    public static final class TrackEntry implements BaseColumns {

        public static final String LOG_TAG = TrackEntry.class.getSimpleName();

        public static final String TABLE_NAME = "Tracks";

        // Column with the foreign key into the artist table.
        public static final String COLUMN_ARTIST_FOREIGN_KEY = "artist_id";
        // Track ID in Spotify
        public static final String COLUMN_TRACK_ID = "track_id";
        // Track name
        public static final String COLUMN_TRACK_NAME = "name";
        // Album name
        public static final String COLUMN_ALBUM_NAME = "album_name";
        // Track stream URL
        public static final String COLUMN_TRACK_URL = "url";
        // Track image Thumbnail url
        public static final String COLUMN_IMAGE_THUMB = "image_thumb";
        // Track medium image url
        public static final String COLUMN_IMAGE_MED = "image_med";

        //Content Provider
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;

        public static final String[] TRACK_COLUMNS = {
                TrackEntry.TABLE_NAME + "." + TrackEntry._ID,
                TrackEntry.COLUMN_TRACK_ID,
                TrackEntry.COLUMN_ARTIST_FOREIGN_KEY,
                TrackEntry.COLUMN_ALBUM_NAME,
                TrackEntry.COLUMN_TRACK_URL,
                TrackEntry.COLUMN_IMAGE_MED,
                TrackEntry.COLUMN_TRACK_NAME,
                TrackEntry.TABLE_NAME + "." + TrackEntry.COLUMN_IMAGE_THUMB
        };

        // These indices are tied to TRACK_COLUMNS.  If TRACK_COLUMNS changes, these must change.
        public static final int COL_ID_INDEX = 0;
        public static final int COL_TRACK_ID_INDEX = 1;
        public static final int COL_ARTIST_FOREIGN_KEY_INDEX = 2;
        public static final int COL_ALBUM_NAME_INDEX = 3;
        public static final int COL_TRACK_URL_INDEX = 4;
        public static final int COL_IMAGE_MED_INDEX = 5;
        public static final int COL_TRACK_NAME_INDEX = 6;
        public static final int COL_TRACK_IMAGE_THUMB_INDEX = 7;


        public static Uri buildTrackIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTracksFromTrackName(String trackName) {
            return CONTENT_URI.buildUpon().appendPath(trackName).build();
        }

        public static Uri buildArtistFromTrackId(long trackId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(trackId))
                    .appendPath(ArtistContract.PATH_ARTISTS).build();
        }

        public static Uri buildTracksFromAnArtist(long artistId) {
            return ArtistContract.ArtistEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(artistId)).
                    appendPath(PATH_TRACKS).build();
        }

        public static Uri buildTrackFromArtistAndTrack(long artistId, long trackId) {
            return ArtistContract.ArtistEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(artistId)).
                    appendPath(PATH_TRACKS).appendPath(Long.toString(trackId)).build();
        }

        // Get fields from Uri
        public static long getTrackIdFromUri(Uri uri) {
            long trackId = -1L;

            try {
                trackId = Long.parseLong(uri.getPathSegments().get(1));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Error parsing track ID");
            }

            return trackId;
        }
    }
}
