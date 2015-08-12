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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ggomeze on 12/08/15.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    static final int FETCH_TRACKS_LOADER = 0;

    private Uri mTrackUri;

    private TextView mTrackName;
    private TextView mArtistName;
    private TextView mAlbumName;
    private ImageView mAlbumAvatar;
    private ImageButton mPreviousButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;

    //Mandatory empty constructor for the activity to instantiate
    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent == null) return null;

        mTrackUri = intent.getData();
        View fragment = inflater.inflate(R.layout.fragment_player, container, false);

        //Setup the view objects
        mTrackName = (TextView)fragment.findViewById(R.id.track_name);
        mArtistName = (TextView)fragment.findViewById(R.id.artist_name);
        mAlbumName = (TextView)fragment.findViewById(R.id.album_name);
        mAlbumAvatar = (ImageView) fragment.findViewById(R.id.album_avatar);
        mPreviousButton = (ImageButton) fragment.findViewById(R.id.previous_button);
        mPlayButton = (ImageButton) fragment.findViewById(R.id.play_button);
        mNextButton = (ImageButton) fragment.findViewById(R.id.next_button);

        getLoaderManager().initLoader(FETCH_TRACKS_LOADER, null, this);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        //If there is no loader with that ID, create one
        // Create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        ArrayList<String> projectionArray = new ArrayList<String>(Arrays.asList(TrackContract.TrackEntry.TRACK_COLUMNS));
        projectionArray.add(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME);
        String[] projection = new String[projectionArray.size()];
        projection = projectionArray.toArray(projection);
        return new CursorLoader(getActivity(),
                mTrackUri,
                projection,   //projection
                null,   //where clause
                null,   //values for where clause
                null    // sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "Got the cursor back");

        if (data.moveToNext()) {
            String thumbnailUrl = data.getString(data.getColumnIndex(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB));
            mTrackName.setText(data.getString(data.getColumnIndex(TrackContract.TrackEntry.COLUMN_TRACK_NAME)));
            mAlbumName.setText(data.getString(data.getColumnIndex(TrackContract.TrackEntry.COLUMN_ALBUM_NAME)));
            mArtistName.setText(data.getString(data.getColumnIndex(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME)));

            if (thumbnailUrl.isEmpty()) {
                Picasso.with(getActivity()).load(R.drawable.artist_placeholder).into(mAlbumAvatar);
            } else {
                Picasso.with(getActivity()).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(mAlbumAvatar);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}