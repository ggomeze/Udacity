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

import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Students: This is NOT a complete test for the TrackContract --- just for the functions
    that we expect you to write.
 */
public class TestArtistContract extends AndroidTestCase {

    private static final String TEST_TRACK_NAME = "/Imagine all the people";
    private static final long TEST_ARTIST_ID = 12939L;  // December 20th, 2014

    public void testGetArtistIdFromUri() {
        Uri artistUri = ArtistContract.ArtistEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(TEST_ARTIST_ID)).
                appendPath(TrackContract.PATH_TRACKS).build();
        assertEquals(ArtistContract.ArtistEntry.getArtistIdFromUri(artistUri), TEST_ARTIST_ID);
    }

}