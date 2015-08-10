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

    public static final String LOG_TAG = ArtistResultsFragment.class.getSimpleName();

    static final String RESTURNED_TRACKS = "returnedTracks";
    static final int FETCH_TRACKS_LOADER = 0;

    private ArrayList<ParcelableTrack> mReturnedTracks;
    private TrackCursorAdapter mTrackCursorAdapter;
    private Uri mArtistTopTracksUri;
    private long mArtistId = -1;

    //Mandatory empty constructor for the activity to instantiate
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnedTracks = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent == null) return null;

        mArtistTopTracksUri = intent.getData();
        mArtistId = ArtistContract.ArtistEntry.getArtistIdFromUri(mArtistTopTracksUri);

        mTrackCursorAdapter = new TrackCursorAdapter(getActivity(), null, 0);

        View fragment = inflater.inflate(R.layout.fragment_detail, container, false);
        ListView artistList = (ListView)fragment.findViewById(R.id.artist_top_tracks);
        artistList.setAdapter(mTrackCursorAdapter);
        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO Play the track
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(RESTURNED_TRACKS)) {
            mReturnedTracks.addAll(savedInstanceState.<ParcelableTrack>getParcelableArrayList(RESTURNED_TRACKS));
        } else if (mArtistId > 0) {
            new FetchArtistTopTracksTask(getActivity()).execute(mArtistId);
        }

        return fragment;
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
}


