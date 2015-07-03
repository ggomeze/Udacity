package com.ggomeze.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
public class ArtistTopTracksFragment extends Fragment {

    private ArrayAdapter<String> mTrackListAdapter;
    private String mArtistId;

    //Mandatory empty constructor for the activity to instantiate
    public ArtistTopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //Create adapter
        mTrackListAdapter = new ArrayAdapter<String>(
                //Current context (this fragment's parent activity
                getActivity(),
                //ID of list item layout
                R.layout.artist_item,
                //ID of TextView to populate
                R.id.artist_list_textview,
                new ArrayList<String>());

        //Find listView and assign artistList adapter
        artistList.setAdapter(mTrackListAdapter);

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO Play the track
            }
        });

        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateArtistTopTracks();
    }

    public void updateArtistTopTracks() {
        //Search top tracks for identified artist
        new SearchArtistTopTracksAsyncTask().execute(mArtistId);
    }

    private class SearchArtistTopTracksAsyncTask extends AsyncTask<String, Void, Tracks> {

        private final String LOG_TAG = SearchArtistTopTracksAsyncTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {

        }
        @Override
        protected Tracks doInBackground(String... artistsIDs) {
            if(artistsIDs.length == 0) {
                return null;
            }
            String artistId = artistsIDs[0];
            //Call Spotify and get and artist list
            SpotifyService spotify = new SpotifyApi().getService();
            Map artistTopTracksParams = new HashMap<String,String>();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            artistTopTracksParams.put(getString(R.string.spotify_country_param), sharedPref.getString(getString(R.string.pref_country_key), "US"));
            return spotify.getArtistTopTrack(artistId, artistTopTracksParams);
        }
        @Override
        protected void onPostExecute(Tracks returnedTracks) {
            if (returnedTracks != null) {
                mTrackListAdapter.clear();
                for (Track track : returnedTracks.tracks) {
                    mTrackListAdapter.add(track.name);
                }
            }

        }
    }
}


