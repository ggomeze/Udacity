package com.ggomeze.spotifystreamer.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.activities.DetailActivity;
import com.ggomeze.spotifystreamer.adapters.ArtistCursorAdapter;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;
import com.ggomeze.spotifystreamer.tasks.FetchArtistsTask;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistResultsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ArtistResultsFragment.class.getSimpleName();

    private static final String RESTURNED_ARTISTS = "returnedArtists";
    private static final int FETCH_ARTISTS_LOADER = 0;

    private ArrayList<ParcelableArtist> mReturnedArtists;
    private ArtistCursorAdapter mArtistCursorAdapter;
    private EditText mSearchText;

    //Mandatory empty constructor for the activity to instantiate
    public ArtistResultsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnedArtists = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_main, container, false);

        ListView mArtistList = (ListView) fragment.findViewById(R.id.list_view_artists);
        mSearchText = (EditText) fragment.findViewById(R.id.search);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchArtists();
                }
                return false;
            }
        });

        mArtistCursorAdapter = new ArtistCursorAdapter(getActivity(), null, 0);

        mArtistList.setAdapter(mArtistCursorAdapter);

        mArtistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) mArtistCursorAdapter.getItem(position);
                if (cursor != null) {
                    Intent artistDetail = new Intent(getActivity(), DetailActivity.class)
                            .setData(TrackContract.TrackEntry.buildTracksFromAnArtist(cursor.getLong(ArtistContract.ArtistEntry.COL_ARTIST_ID_INDEX))
                            );//artists/#/tracks
                    //TODO Remove this info and get it from database
                    artistDetail.putExtra(getString(R.string.album_intent_extra), cursor.getString(ArtistContract.ArtistEntry.COL_ARTIST_NAME_INDEX));
                    startActivity(artistDetail);
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(RESTURNED_ARTISTS)) {
            mReturnedArtists.addAll(savedInstanceState.<ParcelableArtist>getParcelableArrayList(RESTURNED_ARTISTS));
        }

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("returnedArtists", mReturnedArtists);
    }

    private void searchArtists() {
        String text = mSearchText.getText().toString();

        if (!(text.isEmpty())) {
            getLoaderManager().restartLoader(FETCH_ARTISTS_LOADER, null, this);
            new FetchArtistsTask(getActivity()).execute(text);
        }
    }

    //Callbacks to implement for loaders
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        //If there is no loader with that ID, create one
        // Create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String text = mSearchText.getText().toString();
        return new CursorLoader(getActivity(),
                ArtistContract.ArtistEntry.CONTENT_URI,
                ArtistContract.ArtistEntry.ARTIST_COLUMNS,   //projection
                ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME + " LIKE ?", //where clause
                new String[]{"%" + text + "%"}, //values for where clause
                null    // sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mArtistCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mArtistCursorAdapter.swapCursor(null);
    }
}
