package com.ggomeze.spotifystreamer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.ggomeze.spotifystreamer.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your ArtistContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {

    private static final long ARTIST_ID_IN_SPOTIFY_TEST = 12345;
    private static final long TRACK_ID_IN_SPOTIFY_TEST = 67890;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createTrackValues(long artistID) {
        ContentValues trackValues = new ContentValues();
        trackValues.put(TrackContract.TrackEntry.COLUMN_ARTIST_FOREIGN_KEY, artistID);
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, TRACK_ID_IN_SPOTIFY_TEST);
        trackValues.put(TrackContract.TrackEntry.COLUMN_ALBUM_NAME, "Album name");
        trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_MED, "http://image.url.med");
        trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB, "http://image.url.thumb");
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_NAME, "Track name");
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_URL, "http://track.url");
        return trackValues;
    }


    static ContentValues createArtistValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB, "http://artist.image.thumb");
        testValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, "Artist Name");
        testValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_ID, ARTIST_ID_IN_SPOTIFY_TEST);

        return testValues;
    }

    static long insertArtistValues(Context context, ContentValues artistValues) {
        SpotifyStreamerDbHelper dbHelper = new SpotifyStreamerDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        assertEquals(true, db.isOpen());

        long artistRowId = db.insert(ArtistContract.ArtistEntry.TABLE_NAME, null, artistValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert test artist", artistRowId != -1);

        db.close();

        return artistRowId;
    }

    static long insertTrackValues(Context context, ContentValues trackValues) {
        // insert our test records into the database
        SpotifyStreamerDbHelper dbHelper = new SpotifyStreamerDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        assertEquals(true, db.isOpen());

        long trackRowId = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, trackValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert test track", trackRowId != -1);

        db.close();

        return trackRowId;
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
}
