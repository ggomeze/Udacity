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
public class TestTrackContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_TRACK_NAME = "/Imagine all the people";
    private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014

    /*
        Students: Uncomment this out to test your weather location function.
     */
    public void testBuildTrackArtist() {
        Uri songUri = TrackContract.TrackEntry.buildTracksFromTrackName(TEST_TRACK_NAME);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildTracksFromTrackName in " +
                        "TrackContract.",
                songUri);
        assertEquals("Error: Track name not properly appended to the end of the Uri",
                TEST_TRACK_NAME, songUri.getLastPathSegment());
        assertEquals("Error: Track name uri doesn't match our expected result",
                songUri.toString(),
                "content://com.ggomeze.spotifystreamer/tracks/%2FImagine%20all%20the%20people");
    }
}
