package com.ggomeze.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.data.TrackContract;
import com.ggomeze.spotifystreamer.listeners.MediaPlayerListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ggomeze on 12/08/15.
 */
public class PlayerFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String PLAYER_FRAGMENT_TAG = "player_fragment_track";
    public static final String TOP_TRACKS_URI = "top_tracks_uri";

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final int FETCH_TRACKS_LOADER = 0;

    private int MILLISECONDS = 100;

    private static final String CURRENT_ITEM = "current_item";
    private static final String CURRENT_POSITION = "current_position";
    private static final String PLAYER_ON_PAUSED = "player_on_paused";
    private static final String PLAYER_COMPLETED = "player_completed";
    private static final String BUTTON_PLAYER_ON_PAUSED = "button_player_on_paused";

    private Uri mTrackUri;
    private boolean mOnTouch;

    private TextView mTrackName;
    private TextView mArtistName;
    private TextView mAlbumName;
    private TextView mStartTime;
    private TextView mEndTime;
    private ImageView mAlbumAvatar;
    private ImageButton mPreviousButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private SeekBar mSeekBar;

    private ContentValues[] mReturnedTracks;
    private int mCurrentPlayingItem = -1;

    private MediaPlayer mMediaPlayer;
    private boolean bPlayerOnPaused = false; //On pause state player
    private boolean bButtonPauseDisplayed = false; //Button pause displayed
    private boolean bPlayerCompleted = false; //Track was completely played
    private int mCurrentPosition;

    private Handler mHandler;

    private Runnable mSeekBarUpdater = new Runnable() {
        @Override
        public void run() {
            updateSeekBarWithPlayerPosition(); //this function can change value of mInterval.
            mHandler.postDelayed(mSeekBarUpdater, MILLISECONDS);
        }
    };

    private void startUpdatingSeekBarTask() {
        mSeekBarUpdater.run();
    }

    private void stopUpdatingSeekBarTask() {
        mHandler.removeCallbacks(mSeekBarUpdater);
    }

    //Mandatory empty constructor for the activity to instantiate
    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrackUri = getArguments().getParcelable(TOP_TRACKS_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mTrackUri == null) {
            Intent intent = getActivity().getIntent();
            if (intent == null || intent.getData() == null) return null;

            mTrackUri = intent.getData();
        }

        View fragment = inflater.inflate(R.layout.fragment_player, container, false);

        //Setup the view objects
        mTrackName = (TextView)fragment.findViewById(R.id.track_name);
        mArtistName = (TextView)fragment.findViewById(R.id.artist_name);
        mAlbumName = (TextView)fragment.findViewById(R.id.album_name);
        mStartTime = (TextView)fragment.findViewById(R.id.start_time);
        mEndTime = (TextView)fragment.findViewById(R.id.end_time);
        mAlbumAvatar = (ImageView) fragment.findViewById(R.id.album_avatar);
        mPreviousButton = (ImageButton) fragment.findViewById(R.id.previous_button);
        mPlayButton = (ImageButton) fragment.findViewById(R.id.play_button);
        mNextButton = (ImageButton) fragment.findViewById(R.id.next_button);
        mSeekBar = (SeekBar) fragment.findViewById(R.id.slider);

        updateSeekBarWithPosition(0);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        MediaPlayerListener listener = new MediaPlayerListener(this);
        mMediaPlayer.setOnPreparedListener(listener);
        mMediaPlayer.setOnCompletionListener(listener);
        mSeekBar.setOnSeekBarChangeListener(listener);
        mHandler = new Handler();

        if (savedInstanceState != null) {
            mCurrentPlayingItem = savedInstanceState.getInt(CURRENT_ITEM);
            updatePlayerWithPosition(savedInstanceState.getInt(CURRENT_POSITION));
            bPlayerOnPaused = savedInstanceState.getBoolean(PLAYER_ON_PAUSED);
            setPlayerButtonOnPause(savedInstanceState.getBoolean(BUTTON_PLAYER_ON_PAUSED));
            bPlayerCompleted = savedInstanceState.getBoolean(PLAYER_COMPLETED);
        }

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
                clickedOnMiddleButton();
            }
        });

        return fragment;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        getLoaderManager().destroyLoader(FETCH_TRACKS_LOADER);
        stopUpdatingSeekBarTask();
    }

    private void moveToNext(){
        if(mCurrentPlayingItem == mReturnedTracks.length - 1)
            mCurrentPlayingItem = 0;
        else
            mCurrentPlayingItem++;
        updateSeekBarWithPosition(0);
        updateTrackOnScreen();
        preparePlayerForPlaying(true);
    }

    private void moveToPrevious(){
        if(mCurrentPlayingItem == 0)
            mCurrentPlayingItem = mReturnedTracks.length - 1;
        else
            mCurrentPlayingItem--;
        updateSeekBarWithPosition(0);
        updateTrackOnScreen();
        preparePlayerForPlaying(true);
    }

    private void clickedOnMiddleButton() {
        mPlayButton.setImageResource(bButtonPauseDisplayed ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);

        if(mMediaPlayer.isPlaying()) {//Playing: do a pause.
            setPlayerButtonOnPause(false);
            bPlayerOnPaused = true;
            mMediaPlayer.pause();
        } else if (bPlayerOnPaused){ //Paused: Resume.
            setPlayerButtonOnPause(true);
            startReadyPlayer();
        } else if (bButtonPauseDisplayed) { //idle, initialized, preparing, prepared and button on paused ("playing")
            setPlayerButtonOnPause(false);
            mMediaPlayer.reset();
        } else {//idle, initialized, preparing, prepared and button on play
            setPlayerButtonOnPause(true);
            preparePlayerForPlaying(true);
        }
    }

    public void setPlayerButtonOnPause(boolean onPause) {
        bButtonPauseDisplayed = onPause;
        if(onPause) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            stopUpdatingSeekBarTask();
        }
    }

    private void preparePlayerForPlaying(boolean forPlaying) {
        setPlayerButtonOnPause(forPlaying);
        mMediaPlayer.reset();
        try {
            String dataSource = mReturnedTracks[mCurrentPlayingItem].getAsString(TrackContract.TrackEntry.COLUMN_TRACK_URL);
            mMediaPlayer.setDataSource(dataSource);
            mMediaPlayer.prepareAsync();
        } catch (IllegalArgumentException ilegalException) {
            Toast.makeText(getActivity(), "IlegalException Thrown", Toast.LENGTH_SHORT).show();
        } catch (IOException ioException) {
            Toast.makeText(getActivity(), "IOException  Thrown", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReadyPlayer(){
        mMediaPlayer.seekTo(mCurrentPosition);
        bPlayerOnPaused = false;
        bPlayerCompleted = false;
        mMediaPlayer.start();
        startUpdatingSeekBarTask();
    }

    public void prepared() {
        if (bButtonPauseDisplayed) {
            startReadyPlayer();
        }
    }

    private void updateSeekBarWithPosition(int position) {
        if (0 <= position && position <= 30000) {
            mSeekBar.setProgress(position);
            mCurrentPosition = position;
        }
    }

    private void updateSeekBarWithPlayerPosition() {
        int playerPosition = mMediaPlayer.getCurrentPosition();
        if (!mOnTouch && 0 <= playerPosition && playerPosition <= 30000) {
            updateSeekBarWithPosition(playerPosition);
        }
    }

    public void updatePlayerWithPosition(int position) {
        if (0 <= position && position <= 30000) {
            mCurrentPosition = position;
            mMediaPlayer.seekTo(mCurrentPosition);
        }
    }

    private void updateTrackOnScreen() {
        ContentValues values = mReturnedTracks[mCurrentPlayingItem];
        String thumbnailUrl = values.getAsString(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB);
        mTrackName.setText(values.getAsString(TrackContract.TrackEntry.COLUMN_TRACK_NAME));
        mAlbumName.setText(values.getAsString(TrackContract.TrackEntry.COLUMN_ALBUM_NAME));
        mArtistName.setText(values.getAsString(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME));

        if (thumbnailUrl.isEmpty()) {
            Picasso.with(getActivity()).load(R.drawable.artist_placeholder).into(mAlbumAvatar);
        } else {
            Picasso.with(getActivity()).load(thumbnailUrl).placeholder(R.drawable.artist_placeholder).into(mAlbumAvatar);
        }
    }

    public void isOnTouch(boolean touch) {
        mOnTouch = touch;
    }

    public void updatePlayerTimers (String start, String end) {
        mStartTime.setText(start);
        mEndTime.setText(end);
    }

    public void playerCompleted() {
        if (mCurrentPosition >= 29900) { //End of track
            bPlayerCompleted = true; //track finished
            setPlayerButtonOnPause(false);
            mMediaPlayer.reset();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_ITEM, mCurrentPlayingItem);
        outState.putInt(CURRENT_POSITION, mCurrentPosition);
        outState.putBoolean(PLAYER_ON_PAUSED, bPlayerOnPaused);
        outState.putBoolean(BUTTON_PLAYER_ON_PAUSED, bButtonPauseDisplayed);
        outState.putBoolean(PLAYER_COMPLETED, bPlayerCompleted);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        //If there is no loader with that ID, create one
        // Create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        ArrayList<String> projectionArray = new ArrayList<>(Arrays.asList(TrackContract.TrackEntry.TRACK_COLUMNS));
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
        int count = data.getCount();
        if (count > 0) {
            mReturnedTracks = new ContentValues[count];
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
                if (mCurrentPlayingItem == -1 && id==trackId) mCurrentPlayingItem =i;
            }

            updateTrackOnScreen();

            if (!bPlayerCompleted) {
                preparePlayerForPlaying(!bPlayerOnPaused);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

}