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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long TRACK_ID_QUERY = 12345L;
    private static final long ARTIST_ID_QUERY = 67890L;

    // content://com.ggomeze.spotifystreamer/tracks"
    private static final Uri TEST_TRACKS_DIR = TrackContract.TrackEntry.CONTENT_URI;

    // content://com.ggomeze.spotifystreamer/tracks/#/artists"
    private static final Uri TEST_TRACKS_WITH_ARTISTS_DIR = TrackContract.TrackEntry.buildArtistFromTrackId(
            TRACK_ID_QUERY);

    // content://com.ggomeze.spotifystreamer/artist/#/tracks/#"
    private static final Uri TEST_ARTIST_TRACK_DIR = TrackContract.TrackEntry.buildTrackFromArtistAndTrack(
            ARTIST_ID_QUERY, TRACK_ID_QUERY);

    // content://com.ggomeze.spotifystreamer/artists"
    private static final Uri TEST_ARTIST_DIR =ArtistContract.ArtistEntry.CONTENT_URI;

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = TracksAndArtistProvider.buildUriMatcher();

        assertEquals("Error: The TRACKS URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_DIR), TracksAndArtistProvider.TRACKS);
        assertEquals("Error: The TRACK ARTISTS URI was matched incorrectly.",
                testMatcher.match(TEST_TRACKS_WITH_ARTISTS_DIR), TracksAndArtistProvider.TRACK_ARTISTS);
        assertEquals("Error: The TRACK ARTISTS URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_TRACK_DIR), TracksAndArtistProvider.ARTIST_TRACK_ID);
        assertEquals("Error: The ARTISTS URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_DIR), TracksAndArtistProvider.ARTISTS);
    }
}
