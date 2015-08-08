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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TracksAndArtistProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SpotifyStreamerDbHelper mSpotifyStreamerDbHelper;

    static final int TRACKS = 100;
    static final int TRACK_ID = 101;
    static final int TRACK_NAME = 102;
    static final int TRACK_ARTISTS = 103;

    static final int ARTISTS = 200;
    static final int ARTIST_ID = 201;
    static final int ARTIST_NAME = 202;
    static final int ARTIST_TRACKS = 203;
    static final int ARTIST_TRACK_ID = 204; // We could also remove this one

    private static final SQLiteQueryBuilder tracksAndArtistQueryBuilder;
    private static final SQLiteQueryBuilder tracksQueryBuilder;
    private static final SQLiteQueryBuilder artistsQueryBuilder;

    static{
        tracksAndArtistQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        tracksAndArtistQueryBuilder.setTables(
                TrackContract.TrackEntry.TABLE_NAME + " INNER JOIN " +
                        ArtistContract.ArtistEntry.TABLE_NAME +
                        " ON " + TrackContract.TrackEntry.TABLE_NAME +
                        "." + TrackContract.TrackEntry.COLUMN_ARTIST_FOREIGN_KEY +
                        " = " + ArtistContract.ArtistEntry.TABLE_NAME +
                        "." + ArtistContract.ArtistEntry._ID);
    }

    static{
        tracksQueryBuilder = new SQLiteQueryBuilder();
        tracksQueryBuilder.setTables(
                TrackContract.TrackEntry.TABLE_NAME);
    }

    static{
        artistsQueryBuilder = new SQLiteQueryBuilder();
        artistsQueryBuilder.setTables(
                ArtistContract.ArtistEntry.TABLE_NAME);
    }

    //tracks.track_id = ?
    private static final String trackIdSelection =
            TrackContract.TrackEntry.TABLE_NAME +
                    "." + TrackContract.TrackEntry._ID + " = ? ";


    //artists.artist_id = ?
    private static final String artistIdSelection =
            ArtistContract.ArtistEntry.TABLE_NAME +
                    "." +ArtistContract.ArtistEntry._ID + " = ? ";

    //artists.artist_name = ?
    private static final String artistNameSelection =
            ArtistContract.ArtistEntry.TABLE_NAME +
                    "." +ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME + " = ? ";

    //artists.artist_id = ? AND track_id = ?
    private static final String artistIdAndTrackIdSelection =
            ArtistContract.ArtistEntry.TABLE_NAME +
                    "." + ArtistContract.ArtistEntry._ID + " = ? AND " +
                    TrackContract.TrackEntry._ID + " >= ? ";

    private Cursor getTracksBy(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return tracksQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getArtistsBy(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return artistsQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAllTracksByArtistName(Uri uri, String[] projection, String sortOrder) {
        String artistName = ArtistContract.ArtistEntry.getArtistNameFromUri(uri);
        String selection = artistNameSelection;
        String[] selectionArgs = new String[]{artistName};

        return tracksAndArtistQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTracksByArtistId(Uri uri, String[] projection, String sortOrder) {
        long artistId = ArtistContract.ArtistEntry.getArtistIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = artistIdSelection;
        selectionArgs = new String[]{Long.toString(artistId)};

        return tracksAndArtistQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getArtistFromTrack(Uri uri, String[] projection, String sortOrder) {
        long trackId = TrackContract.TrackEntry.getTrackIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = trackIdSelection;
        selectionArgs = new String[]{Long.toString(trackId)};

        return tracksAndArtistQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackFromArtist(Uri uri, String[] projection, String sortOrder) {
        long artistId = ArtistContract.ArtistEntry.getArtistIdFromUri(uri);
        long trackId = TrackContract.TrackEntry.getTrackIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (trackId == -1L) {//Get all tracks from artist
            selection = artistIdSelection;
            selectionArgs = new String[]{Long.toString(artistId)};
        } else { //Get only track with track id
            selectionArgs = new String[]{Long.toString(artistId), Long.toString(trackId)};
            selection = artistIdAndTrackIdSelection;
        }

        return tracksAndArtistQueryBuilder.query(mSpotifyStreamerDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = TrackContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, TrackContract.PATH_TRACKS, TRACKS);
        uriMatcher.addURI(authority, TrackContract.PATH_TRACKS + "/#", TRACK_ID);
        uriMatcher.addURI(authority, TrackContract.PATH_TRACKS + "/*", TRACK_NAME);
        uriMatcher.addURI(authority, TrackContract.PATH_TRACKS + "/#/" + ArtistContract.PATH_ARTISTS, TRACK_ARTISTS);
        uriMatcher.addURI(authority, ArtistContract.PATH_ARTISTS, ARTISTS);
        uriMatcher.addURI(authority, ArtistContract.PATH_ARTISTS + "/#", ARTIST_ID);
        uriMatcher.addURI(authority, ArtistContract.PATH_ARTISTS + "/*", ARTIST_NAME);
        uriMatcher.addURI(authority, ArtistContract.PATH_ARTISTS + "/#/" + TrackContract.PATH_TRACKS, ARTIST_TRACKS);
        uriMatcher.addURI(authority, ArtistContract.PATH_ARTISTS + "/#/" + TrackContract.PATH_TRACKS + "/#", ARTIST_TRACK_ID);

        return uriMatcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mSpotifyStreamerDbHelper = new SpotifyStreamerDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TRACK_ID:
            case ARTIST_TRACK_ID:
                return TrackContract.TrackEntry.CONTENT_ITEM_TYPE;
            case ARTIST_ID:
            case TRACK_ARTISTS://One artist per track
                return ArtistContract.ArtistEntry.CONTENT_ITEM_TYPE;
            case TRACK_NAME:
            case ARTIST_TRACKS:
            case TRACKS:
                return TrackContract.TrackEntry.CONTENT_TYPE;
            case ARTIST_NAME:
            case ARTISTS:
                return ArtistContract.ArtistEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "/tracks"
            case TRACKS:
                retCursor = getTracksBy(uri, projection, selection, selectionArgs, sortOrder);
                break;
            // "/artists"
            case ARTISTS:
                retCursor = getArtistsBy(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case TRACK_ARTISTS:
                retCursor = getArtistFromTrack(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mSpotifyStreamerDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TRACKS: {
                long _id = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TrackContract.TrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ARTISTS:
                long _id = db.insert(ArtistContract.ArtistEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ArtistContract.ArtistEntry.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mSpotifyStreamerDbHelper.getWritableDatabase();

        // Use the uriMatcher to match the TRACKS and ARTISTS URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        //Delete all rows if no selection
        if (null == selection) selection = "1";
        switch (match) {
            case TRACKS: {
                rowsDeleted = db.delete(TrackContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ARTISTS:
                rowsDeleted = db.delete(ArtistContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the actual rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Start by getting a writable database
        final SQLiteDatabase db = mSpotifyStreamerDbHelper.getWritableDatabase();

        // Use the uriMatcher to match the TRACKS and ARTISTS URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;
        switch (match) {
            case TRACK_ID:
                long trackId = TrackContract.TrackEntry.getTrackIdFromUri(uri);
                rowsUpdated = db.update(TrackContract.TrackEntry.TABLE_NAME, values, trackIdSelection, new String[]{Long.toString(trackId)});
                break;
            case ARTIST_ID:
                long artistId = ArtistContract.ArtistEntry.getArtistIdFromUri(uri);
                rowsUpdated = db.update(TrackContract.TrackEntry.TABLE_NAME, values, artistIdSelection, new String[]{Long.toString(artistId)});
                break;
            case TRACKS:
                rowsUpdated = db.update(TrackContract.TrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ARTISTS:
                rowsUpdated = db.update(ArtistContract.ArtistEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the actual rows updated
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mSpotifyStreamerDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRACKS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrackContract.TrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mSpotifyStreamerDbHelper.close();
        super.shutdown();
    }
}