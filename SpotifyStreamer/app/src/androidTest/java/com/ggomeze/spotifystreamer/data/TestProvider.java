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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ggomeze.spotifystreamer.data.TrackContract.TrackEntry;
import com.ggomeze.spotifystreamer.data.ArtistContract.ArtistEntry;

import java.util.Arrays;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    private long mTestTrackId = 12345;
    private long mTestArtistId = 67890;
    private String mTestTrackName = "track name";
    private String mTestArtistName = "artist name";

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                TrackEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ArtistEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Tracks table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
//    public void deleteAllRecordsFromDB() {
//        SpotifyStreamerDbHelper dbHelper = new SpotifyStreamerDbHelper(mContext);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        db.delete(TrackEntry.TABLE_NAME, null, null);
//        db.delete(ArtistEntry.TABLE_NAME, null, null);
//        db.close();
//    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // TracksAndArtistProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                TracksAndArtistProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: TracksAndArtistProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + TrackContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TrackContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: TracksAndArtistProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.ggomeze.spotifystreamer/tracks/
        String type = mContext.getContentResolver().getType(TrackEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.ggomeze.spotifystreamer/tracks
        assertEquals("Error: the TrackEntry CONTENT_URI should return TrackEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

        // content://com.ggomeze.spotifystreamer/tracks/track%20name
        type = mContext.getContentResolver().getType(TrackEntry.buildTracksFromTrackName(mTestTrackName));
        // vnd.android.cursor.dir/com.ggomeze.spotifystreamer/tracks/track%20name
        assertEquals("Error: the TrackEntry CONTENT_URI should return TrackEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

        // content://com.ggomeze.spotifystreamer/tracks/12345
        type = mContext.getContentResolver().getType(
                TrackEntry.buildTrackIdUri(mTestTrackId));
        // vnd.android.cursor.item/com.ggomeze.spotifystreamer/tracks/12345
        assertEquals("Error: the TrackEntry CONTENT_URI with track_id should return TrackEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_ITEM_TYPE, type);

        // content://com.ggomeze.spotifystreamer/artists/
        type = mContext.getContentResolver().getType(ArtistEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.ggomeze.spotifystreamer/artists
        assertEquals("Error: the ArtistEntry CONTENT_URI should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        // content://com.ggomeze.spotifystreamer/tracks/12345/artists
        type = mContext.getContentResolver().getType(
                TrackEntry.buildArtistFromTrackId(mTestTrackId));
        // vnd.android.cursor.dir/com.ggomeze.spotifystreamer/tracks/12345/artists
        assertEquals("Error: the ArtistEntry CONTENT_URI with track_id should return ArtistEntry.CONTENT_ITEM_TYPE",
                ArtistEntry.CONTENT_ITEM_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicTrackQuery() {
        // insert our test records into the database
        SpotifyStreamerDbHelper dbHelper = new SpotifyStreamerDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createArtistValues();
        long artistRowId = TestUtilities.insertArtistValues(mContext, testValues);

        // Fantastic.  Now that we have an artist, add a track!
        ContentValues trackValues = TestUtilities.createTrackValues(artistRowId);

        long trackRowId = db.insert(TrackEntry.TABLE_NAME, null, trackValues);
        assertTrue("Unable to Insert TrackEntry into the Database", trackRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTrackQuery", trackCursor, trackValues);
    }

    /*
        This test insert some tracks from an artist and test the query to get those records afterwards
     */
    public void testGetTracksFromArtistQuery() {
        // First artist values
        ContentValues testValues = TestUtilities.createArtistValues();
        //Modify spotify_atist_id
        testValues.put(ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID, TestUtilities.ARTIST_ID_IN_SPOTIFY_TEST + "99");
        Uri firstArtistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, testValues);
        long firstArtistRowId = ContentUris.parseId(firstArtistUri);

        // Verify we got a row back.
        assertTrue(firstArtistRowId != -1);

        // Second artist values
        testValues = TestUtilities.createArtistValues();
        Uri secondArtistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, testValues);
        long secondArtistRowId = ContentUris.parseId(secondArtistUri);

        // Verify we got a row back.
        assertTrue(secondArtistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                ArtistEntry._ID + "= ?", // cols for "where" clause
                new String[]{Long.toString(secondArtistRowId)}, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testGetTracksFromArtistQuery. Error validating ArtistEntry.",
                cursor, testValues);

        // Now we can bulkInsert some tracks.  In fact, we only implement BulkInsert for track
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] firstBulkInsertContentValues = createBulkInsertTestTracksValues(firstArtistRowId, 20);
        ContentValues[] secondBulkInsertContentValues = createBulkInsertTestTracksValues(secondArtistRowId, 60);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver trackObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, trackObserver);

        mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, firstBulkInsertContentValues);
        int insertCount = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, secondBulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        trackObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(trackObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                TrackContract.TrackEntry.buildTracksFromAnArtist(secondArtistRowId),
                null,   // projection
                null,
                null,   // Values for the "where" clause
                null    // sort order
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                Arrays.copyOfRange(TrackEntry.TRACK_COLUMNS, 1, TrackEntry.TRACK_COLUMNS.length), // leaving "columns" null just returns all the columns.
                TrackEntry.COLUMN_ARTIST_FOREIGN_KEY + " = ?", // cols for "where" clause
                new String[]{Long.toString(secondArtistRowId)}, // values for "where" clause
                TrackEntry.COLUMN_TRACK_ID + " ASC"  // sort order == by TRACK_ID ASC (we are comparing a string though)
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testGetTracksFromArtistQuery.  Error validating TrackEntry " + i,
                    cursor, secondBulkInsertContentValues[i]);
        }
        cursor.close();
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your location queries are
        performing correctly.
     */
    public void testBasicArtistQueries() {
        // insert our test records into the database
        SpotifyStreamerDbHelper dbHelper = new SpotifyStreamerDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues artistValues = TestUtilities.createArtistValues();
        long artistRowId = TestUtilities.insertArtistValues(mContext, artistValues);
        assertTrue("Unable to Insert ArtistEntry into the Database", artistRowId != -1);

        // Test the basic content provider query
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicArtistQueries, artist query", artistCursor, artistValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Artist Query did not properly set NotificationUri",
                    artistCursor.getNotificationUri(), ArtistEntry.CONTENT_URI);
        }
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */
    public void testUpdateArtist() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createArtistValues();

        Uri artistUri = mContext.getContentResolver().
                insert(ArtistEntry.CONTENT_URI, values);
        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);
        Log.d(LOG_TAG, "New row id: " + artistRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ArtistEntry._ID, artistRowId);
        updatedValues.put(ArtistEntry.COLUMN_ARTIST_NAME, "Michael Jackson");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor artistCursor = mContext.getContentResolver().query(ArtistEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        artistCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                ArtistEntry.CONTENT_URI, updatedValues, ArtistEntry._ID + "= ?",
                new String[] { Long.toString(artistRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        artistCursor.unregisterContentObserver(tco);
        artistCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,   // projection
                ArtistEntry._ID + " = ?",
                new String[]{Long.toString(artistRowId)},   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating artist entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues artistValues = TestUtilities.createArtistValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, tco);
        Uri artistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, artistValues);
        assertTrue(artistUri != null);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry.",
                cursor, artistValues);

        // Fantastic.  Now that we have an artist, add a track!
        ContentValues trackValues = TestUtilities.createTrackValues(artistRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, tco);

        Uri trackInsertUri = mContext.getContentResolver()
                .insert(TrackEntry.CONTENT_URI, trackValues);
        assertTrue(trackInsertUri != null);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrackEntry insert.",
                trackCursor, trackValues);

        // Add the artist values in with the track data so that we can make
        // sure that the join worked and we actually get all the values back
        trackValues.putAll(artistValues);

        long trackId = TrackEntry.getTrackIdFromUri(trackInsertUri);
        assertTrue(trackId > 0);
        Log.v(LOG_TAG, "This is the trackId: " + trackId);

        Uri getArtistFromTrackIdUri = TrackEntry.buildArtistFromTrackId(trackId);
        Log.v(LOG_TAG, "This is the uri: " + getArtistFromTrackIdUri);

        // Get the joined Track and Artist data
        trackCursor = mContext.getContentResolver().query(
                getArtistFromTrackIdUri,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Tracks and Artists Data.",
                trackCursor, trackValues);

    }

    // Make sure we can still delete after adding/updating stuff
    //
    // It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver artistObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, artistObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver trackObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, trackObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        artistObserver.waitForNotificationOrFail();
        trackObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(artistObserver);
        mContext.getContentResolver().unregisterContentObserver(trackObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertTestTracksValues(long artistId, int trackOffset) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++ ) {
            ContentValues trackValues = new ContentValues();
            trackValues.put(TrackEntry.COLUMN_ARTIST_FOREIGN_KEY, artistId);
            trackValues.put(TrackEntry.COLUMN_ALBUM_NAME, "Album name");
            trackValues.put(TrackEntry.COLUMN_IMAGE_MED, "http://track.image.med");
            trackValues.put(TrackEntry.COLUMN_IMAGE_THUMB, "http://track.image.med");
            trackValues.put(TrackEntry.COLUMN_TRACK_ID, (trackOffset + i));
            trackValues.put(TrackEntry.COLUMN_TRACK_NAME, "Track name");
            trackValues.put(TrackEntry.COLUMN_TRACK_URL, "http://track.stream.url");
            returnContentValues[i] = trackValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        // first, let's create an artist value
        ContentValues testValues = TestUtilities.createArtistValues();
        Uri artistUri = mContext.getContentResolver().insert(ArtistEntry.CONTENT_URI, testValues);
        long artistRowId = ContentUris.parseId(artistUri);

        // Verify we got a row back.
        assertTrue(artistRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating ArtistEntry.",
                cursor, testValues);

        // Now we can bulkInsert some tracks.  In fact, we only implement BulkInsert for track
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertTestTracksValues(artistRowId, 80);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver trackObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, trackObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        trackObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(trackObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        cursor = mContext.getContentResolver().query(
                TrackContract.TrackEntry.buildTracksFromAnArtist(artistRowId),
                null,   // projection
                null,
                null,   // Values for the "where" clause
                null    // sort order
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                TrackEntry.COLUMN_TRACK_ID + " ASC"  // sort order == by TRACK_ID ASC (we are comparing a string though)
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TrackEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
