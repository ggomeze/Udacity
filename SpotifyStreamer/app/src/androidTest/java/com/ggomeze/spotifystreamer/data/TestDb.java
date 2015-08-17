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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    private SQLiteDatabase db;

    // Since we want each test to start with a clean slate
    private void deleteTheDatabase() {
        mContext.deleteDatabase(SpotifyStreamerDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    @Override
    public void setUp() {

        deleteTheDatabase();
        db = new SpotifyStreamerDbHelper(
                this.mContext).getWritableDatabase();
    }

    /*
        This function gets called after each test is executed to close the database.
     */
    @Override
    public void tearDown() {
        db.close();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(TrackContract.TrackEntry.TABLE_NAME);
        tableNameHashSet.add(ArtistContract.ArtistEntry.TABLE_NAME);

        mContext.deleteDatabase(SpotifyStreamerDbHelper.DATABASE_NAME);

        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the tracks entry and artist entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + ArtistContract.ArtistEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for Artists table
        final HashSet<String> artistColumnHashSet = new HashSet<>();
        artistColumnHashSet.add(ArtistContract.ArtistEntry._ID);
        artistColumnHashSet.add(ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID);
        artistColumnHashSet.add(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME);
        artistColumnHashSet.add(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            artistColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required artists entry columns",
                artistColumnHashSet.isEmpty());

        c = db.rawQuery("PRAGMA table_info(" + TrackContract.TrackEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for Tracks table
        final HashSet<String> trackColumnHashSet = new HashSet<>();
        trackColumnHashSet.add(TrackContract.TrackEntry._ID);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_ARTIST_FOREIGN_KEY);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_TRACK_ID);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_ALBUM_NAME);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_IMAGE_MED);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_TRACK_NAME);
        trackColumnHashSet.add(TrackContract.TrackEntry.COLUMN_TRACK_URL);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            trackColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required tracks entry columns",
                trackColumnHashSet.isEmpty());

        c.close();
    }

    public void testArtistTable() {
        assertEquals(true, db.isOpen());

        ContentValues artistValues = TestUtilities.createArtistValues();
        long locationRowId = TestUtilities.insertArtistValues(this.mContext, artistValues);

        //Query the database
        Cursor cursor = db.query(ArtistContract.ArtistEntry.TABLE_NAME,
                null,
                ArtistContract.ArtistEntry._ID + "= ?",
                new String[]{String.valueOf(locationRowId)},
                null,
                null,
                null,
                null);

        //Validate in resulting Cursor with original
        if (cursor.moveToFirst()) {
            TestUtilities.validateCurrentRecord("Record is not valid", cursor, artistValues);
        } else {
            fail("No artist inserted in the database");
        }

        //Close cursor
        cursor.close();
    }

    public void testTrackTable() {
        assertEquals(true, db.isOpen());

        // First insert the artist (tested before), and then use the artistRowId to insert
        // the track. Make sure to cover as many failure cases as you can.
        long artistRowId = TestUtilities.insertArtistValues(this.mContext, TestUtilities.createArtistValues());
        assertTrue(artistRowId != -1);

        // Create ContentValues of what you want to insert
        ContentValues trackValues = TestUtilities.createTrackValues(artistRowId);

        // Insert ContentValues into database and get a row ID back
        long trackRowId = TestUtilities.insertTrackValues(this.mContext, trackValues);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(TrackContract.TrackEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                TrackContract.TrackEntry._ID + "= ?", // cols for "where" clause
                new String[]{String.valueOf(trackRowId)}, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null);

        // Move the cursor to a valid database row
        // Validate data in resulting Cursor with the original ContentValues
        if (cursor.moveToFirst()) {
            TestUtilities.validateCurrentRecord("Record is not valid", cursor, trackValues);
        } else {
            fail("No track inserted in the database");
        }

        //Close cursor
        cursor.close();
    }
}
