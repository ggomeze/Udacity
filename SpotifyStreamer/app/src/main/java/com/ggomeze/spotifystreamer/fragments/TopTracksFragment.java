package com.ggomeze.spotifystreamer.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.adapters.TrackAdapter;
import com.ggomeze.spotifystreamer.models.ParcelableTrack;
import com.ggomeze.spotifystreamer.tasks.FetchArtistTopTracksTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    static final String RESTURNED_TRACKS = "returnedTracks";

    private ArrayList<ParcelableTrack> mReturnedTracks;
    private TrackAdapter mTrackAdapter;
    private String mArtistId;

    //Mandatory empty constructor for the activity to instantiate
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnedTracks = new ArrayList<>();
        mTrackAdapter = new TrackAdapter(getActivity(), 0, mReturnedTracks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            mArtistId = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        View fragment = inflater.inflate(R.layout.fragment_detail, container, false);

        ListView artistList = (ListView)fragment.findViewById(R.id.artist_top_tracks);

        //Find listView and assign artistList adapter
        artistList.setAdapter(mTrackAdapter);

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO Play the track
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(RESTURNED_TRACKS)) {
            mReturnedTracks.addAll(savedInstanceState.<ParcelableTrack>getParcelableArrayList(RESTURNED_TRACKS));
        } else {
            updateArtistTopTracks();
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

    public void updateArtistTopTracks() {
        //Search top tracks for identified artist
        new FetchArtistTopTracksTask(getActivity(), mTrackAdapter, mReturnedTracks).execute(mArtistId);
    }
}


