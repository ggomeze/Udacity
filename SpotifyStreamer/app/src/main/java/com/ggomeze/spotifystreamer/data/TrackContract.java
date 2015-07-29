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

import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database.
 */
public class TrackContract {

    /* Inner class that defines the table contents of the artists table */
    public static final class TrackEntry implements BaseColumns {

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

    }
}
