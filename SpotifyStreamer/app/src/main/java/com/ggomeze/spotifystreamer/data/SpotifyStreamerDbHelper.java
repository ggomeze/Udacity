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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ggomeze.spotifystreamer.data.TrackContract.TrackEntry;
import com.ggomeze.spotifystreamer.data.ArtistContract.ArtistEntry;

/**
 * Manages a local database for weather data.
 */
public class SpotifyStreamerDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "spotifystreamer.db";

    public SpotifyStreamerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //TODO It looks like relationship between tracks and artists, is has many to has many. We'd need so, a new table that link tracks and artists. For the time being, i'm going to consider a track has one artist
        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + ArtistEntry.TABLE_NAME + " (" +
                ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ArtistEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL, " +
                ArtistEntry.COLUMN_IMAGE_THUMB + " TEXT NOT NULL," +

                // To assure the application has just one artist entry,
                // it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ArtistEntry.COLUMN_ARTIST_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrackEntry.COLUMN_ARTIST_FOREIGN_KEY + " INTEGER, " +
                TrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_IMAGE_MED + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_IMAGE_THUMB + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_ID + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_TRACK_URL + " TEXT NOT NULL, " +

                // Set up the artist_id column as a foreign key to artists table.
                " FOREIGN KEY (" + TrackEntry.COLUMN_ARTIST_FOREIGN_KEY + ") REFERENCES " +
                ArtistEntry.TABLE_NAME + " (" + ArtistEntry._ID + ")  ON DELETE CASCADE, " +

                // To assure the application has just one track,
                // it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + TrackEntry.COLUMN_TRACK_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArtistEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
