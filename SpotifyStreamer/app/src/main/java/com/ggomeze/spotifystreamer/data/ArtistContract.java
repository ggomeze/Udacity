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
public class ArtistContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    private static final String CONTENT_AUTHORITY = "com.ggomeze.spotifystreamer";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.ggomeze.spotifystreamer/tracks/ is a valid path for
    // tracks. content://com.ggomeze.spotifystreamer/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_ARTISTS = "artists";

    /* Inner class that defines the table contents of the artists table */
    public static final class ArtistEntry implements BaseColumns {

        public static final String LOG_TAG = ArtistEntry.class.getSimpleName();

        public static final String TABLE_NAME = "Artists";

        // Artist ID in Spotify
        public static final String COLUMN_SPOTIFY_ARTIST_ID = "spotify_artist_id";
        // Artist Name in Spotify
        public static final String COLUMN_ARTIST_NAME = "artist_name";
        // Artist image Thumbnail url
        public static final String COLUMN_IMAGE_THUMB = "image_thumb";

        //Content Provider
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTISTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;

        public static Uri buildArtistIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTracksFromAnArtistName(String artistName) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_ARTIST_NAME, artistName).build();
        }

        public static final String[] ARTIST_COLUMNS = {
                ArtistEntry.TABLE_NAME + "." + ArtistContract.ArtistEntry._ID,
                ArtistEntry.COLUMN_ARTIST_NAME,
                ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID,
                ArtistEntry.TABLE_NAME + "." + ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB
        };

        // These indices are tied to ARTIST_COLUMNS.  If ARTIST_COLUMNS changes, these must change.
        public static final int COL_ARTIST_ID_INDEX = 0;
        public static final int COL_ARTIST_NAME_INDEX = 1;
        public static final int COL_SPOTIFY_ARTIST_ID_INDEX = 2;
        public static final int COL_ARTIST_IMAGE_THUMB_INDEX = 3;

        // Get artistId from Uris (/artists/#, /artists/#/tracks)
        public static long getArtistIdFromUri(Uri uri) {
            long artistId = -1L;

            try {
                artistId = Long.parseLong(uri.getPathSegments().get(1));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Error parsing artist ID");
            }

            return artistId;
        }

        public static String getArtistNameFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_ARTIST_NAME);
        }
    }
}
