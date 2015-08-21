package com.ggomeze.spotifystreamer.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.adapters.TrackCursorAdapter;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;
import com.ggomeze.spotifystreamer.tasks.FetchArtistTopTracksTask;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String ARTIST_TOP_TRACKS_URI = "artist_top_tracks_uri";
    public static final String TOP_TRACKS_FRAGMENT_TAG = "DFTAG";

    private static final String LOG_TAG = ArtistResultsFragment.class.getSimpleName();

    private static final String RESTURNED_TRACKS = "returnedTracks";
    private static final int FETCH_TRACKS_LOADER = 0;

    private ArrayList<ParcelableTrack> mReturnedTracks;
    private TrackCursorAdapter mTrackCursorAdapter;
    private Uri mArtistTopTracksUri;
    private long mArtistId = -1;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface TrackCallback {
        /**
         * ArtistCallback for when an item has been selected from this fragment.
         */
        public void onTrackSelected(Uri artistTopTracksUri);
    }

    //Mandatory empty constructor for the activity to instantiate
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnedTracks = new ArrayList<>();
        if (getArguments() != null) {
            mArtistTopTracksUri = getArguments().getParcelable(ARTIST_TOP_TRACKS_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mArtistTopTracksUri == null) {
            Intent intent = getActivity().getIntent();
            if (intent == null || intent.getData() == null) return null;

            mArtistTopTracksUri = intent.getData();
        }

        mArtistId = ArtistContract.ArtistEntry.getArtistIdFromUri(mArtistTopTracksUri);

        mTrackCursorAdapter = new TrackCursorAdapter(getActivity(), null, 0);

        View fragment = inflater.inflate(R.layout.fragment_detail, container, false);
        ListView artistList = (ListView)fragment.findViewById(R.id.artist_top_tracks);
        artistList.setAdapter(mTrackCursorAdapter);

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) mTrackCursorAdapter.getItem(position);
                if (cursor != null) {
                    ((TrackCallback) getActivity()).onTrackSelected(
                            TrackContract.TrackEntry.buildTrackFromArtistAndTrack(
                                    cursor.getLong(TrackContract.TrackEntry.COL_ARTIST_FOREIGN_KEY_INDEX),
                                    cursor.getLong(TrackContract.TrackEntry.COL_ID_INDEX)));//artists/#/tracks/#
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(RESTURNED_TRACKS)) {
            mReturnedTracks.addAll(savedInstanceState.<ParcelableTrack>getParcelableArrayList(RESTURNED_TRACKS));
        } else if (mArtistId > 0) {
            updateTopTracks();
        }

        return fragment;
    }

    private void updateTopTracks() {
        new FetchArtistTopTracksTask(getActivity()).execute(mArtistId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(RESTURNED_TRACKS, mReturnedTracks);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mArtistTopTracksUri != null)
            getLoaderManager().initLoader(FETCH_TRACKS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        //If there is no loader with that ID, create one
        // Create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(),
                mArtistTopTracksUri,
                TrackContract.TrackEntry.TRACK_COLUMNS,   //projection
                null,   //where clause
                null,   //values for where clause
                null    // sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTrackCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTrackCursorAdapter.swapCursor(null);
    }

    /**
     * Called from MainActivity on two pane mode, when a change in Preference has been detected
     */
    public void onCountryPreferenceChanged(String countryPreference) {
        //TODO Do not get preference from the task again. Send it as a param
        updateTopTracks();
    }
}


