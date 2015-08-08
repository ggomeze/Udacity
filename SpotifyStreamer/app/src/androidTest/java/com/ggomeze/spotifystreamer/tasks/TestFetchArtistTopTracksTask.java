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

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.ggomeze.spotifystreamer.data.ArtistContract;

public class TestFetchArtistTopTracksTask extends AndroidTestCase{
    static final String ADD_SPOTIFY_ARTIST_ID = "ag2jeic9sl92g4n1ndh";
    static final String ADD_ARTIST_NAME = "Michael Jackson";
    static final String ADD_IMAGE_THUMB = "http://artist.image.thumb";

    /*
        Students: uncomment testAddLocation after you have written the AddLocation function.
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(11)
    public void testAddArtist() {
        // start from a clean state
        getContext().getContentResolver().delete(ArtistContract.ArtistEntry.CONTENT_URI,
                ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",
                new String[]{ADD_SPOTIFY_ARTIST_ID});

        FetchArtistsTask fwt = new FetchArtistsTask(getContext(), null, null);
        long artistId = fwt.addArtist(ADD_SPOTIFY_ARTIST_ID, ADD_ARTIST_NAME, ADD_IMAGE_THUMB);

        // does addLocation return a valid record ID?
        assertFalse("Error: addArtist returned an invalid ID on insert",
                artistId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our artist?
            Cursor artistCursor = getContext().getContentResolver().query(
                    ArtistContract.ArtistEntry.CONTENT_URI,
                    new String[]{
                            ArtistContract.ArtistEntry._ID,
                            ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID,
                            ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME,
                            ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB
                    },
                    ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",
                    new String[]{ADD_SPOTIFY_ARTIST_ID},
                    null);

            // these match the indices of the projection
            if (artistCursor.moveToFirst()) {
                assertEquals("Error: the queried value of artistId does not match the returned value" +
                        "from addArtist", artistCursor.getLong(0), artistId);
                assertEquals("Error: the queried value of artist id is incorrect",
                        artistCursor.getString(1), ADD_SPOTIFY_ARTIST_ID);
                assertEquals("Error: the queried value of artist name is incorrect",
                        artistCursor.getString(2), ADD_ARTIST_NAME);
                assertEquals("Error: the queried value of artist image thumb is incorrect",
                        artistCursor.getString(3), ADD_IMAGE_THUMB);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    artistCursor.moveToNext());

            // add the location again
            long newArtistId = fwt.addArtist(ADD_SPOTIFY_ARTIST_ID, ADD_ARTIST_NAME, ADD_IMAGE_THUMB);

            assertEquals("Error: inserting an artist again should return the same ID",
                    artistId, newArtistId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(ArtistContract.ArtistEntry.CONTENT_URI,
                ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID + " = ?",
                new String[]{ADD_SPOTIFY_ARTIST_ID});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(ArtistContract.ArtistEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
