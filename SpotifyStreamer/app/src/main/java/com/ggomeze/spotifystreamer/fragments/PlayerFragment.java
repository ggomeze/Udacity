package com.ggomeze.spotifystreamer.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ggomeze on 12/08/15.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, MediaPlayer.OnPreparedListener{

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

    private ContentValues[] mReturnedTracks;
    private int mCurrentItem = 0;

    private MediaPlayer mMediaPlayer;

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

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);

        getLoaderManager().initLoader(FETCH_TRACKS_LOADER, null, this);

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPrevious();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNext();
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        return fragment;
    }

    public void moveToNext(){
        if(mCurrentItem == mReturnedTracks.length - 1)
            mCurrentItem = 0;
        else
            mCurrentItem++;
        updateTrackAndPlay();
    }

    public void moveToPrevious(){
        if(mCurrentItem == 0)
            mCurrentItem = mReturnedTracks.length - 1;
        else
            mCurrentItem--;
        updateTrackAndPlay();
    }

    public void play() {
        //TODO Update to pause button if playing
        mMediaPlayer.reset();
        try {
            String dataSource = mReturnedTracks[mCurrentItem].getAsString(TrackContract.TrackEntry.COLUMN_TRACK_URL);
            mMediaPlayer.setDataSource(dataSource);
            mMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException ilegalException) {
            Toast.makeText(getActivity(), "IlegalException Thrown", Toast.LENGTH_SHORT).show();
        } catch (IOException ioException) {
            Toast.makeText(getActivity(), "IOException  Thrown", Toast.LENGTH_SHORT).show();
        }

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
        projectionArray.add(ArtistContract.ArtistEntry.TABLE_NAME + "." + ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME);
        String[] projection = new String[projectionArray.size()];
        projection = projectionArray.toArray(projection);

        long artistId = ArtistContract.ArtistEntry.getArtistIdFromUri(mTrackUri);
        return new CursorLoader(getActivity(),
                TrackContract.TrackEntry.buildTracksFromAnArtist(artistId),
                projection,   //projection
                null,   //where clause
                null,   //values for where clause
                null    // sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "Got the cursor back");

        mReturnedTracks = new ContentValues[data.getCount()];
        long trackId = TrackContract.TrackEntry.getTrackIdFromArtistUri(mTrackUri);

        for(int i = 0; data.moveToNext() ; i++) {
            ContentValues values = new ContentValues(data.getColumnCount());
            long id = data.getInt(0);
            values.put(TrackContract.TrackEntry._ID, id);
            values.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, data.getString(1));
            values.put(TrackContract.TrackEntry.COLUMN_ARTIST_FOREIGN_KEY, data.getInt(2));
            values.put(TrackContract.TrackEntry.COLUMN_ALBUM_NAME, data.getString(3));
            values.put(TrackContract.TrackEntry.COLUMN_TRACK_URL, data.getString(4));
            values.put(TrackContract.TrackEntry.COLUMN_IMAGE_MED, data.getString(5));
            values.put(TrackContract.TrackEntry.COLUMN_TRACK_NAME, data.getString(6));
            values.put(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB, data.getString(7));
            values.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, data.getString(8));
            mReturnedTracks[i] = values;
            if (id==trackId) mCurrentItem=i;
        }

        updateTrackAndPlay();
    }

    private void updateTrackAndPlay() {
        ContentValues values = mReturnedTracks[mCurrentItem];
        String thumbnailUrl = values.getAsString(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB);
        mTrackName.setText(values.getAsString(TrackContract.TrackEntry.COLUMN_TRACK_NAME));
        mAlbumName.setText(values.getAsString(TrackContract.TrackEntry.COLUMN_ALBUM_NAME));
        mArtistName.setText(values.getAsString(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME));

        if (thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(R.drawable.artist_placeholder).into(mAlbumAvatar);
        } else {
            Picasso.with(getActivity()).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(mAlbumAvatar);
        }

        play();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }
}