package com.ggomeze.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistTopTracksFragment extends Fragment {

    private ArrayAdapter<String> mTrackListAdapter;

    //Mandatory empty constructor for the activity to instantiate
    public ArtistTopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add this line to indicate this fragment handles menu options
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            String artist = intent.getStringExtra(Intent.EXTRA_TEXT);
            Toast.makeText(getActivity(), artist, Toast.LENGTH_SHORT).show();
        }

        //TODO Create a new list with this artist top10 tracks
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
                // Executed in an Activity, so 'this' is the Context
            //Play the track
            }
        });
        //TODO Search top tracks for identified artist
        new SearchArtistTopTracksAsyncTask().execute("Beyonce");

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.fragment_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    private class SearchArtistTopTracksAsyncTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = SearchArtistTopTracksAsyncTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {

        }
        @Override
        protected List<Artist> doInBackground(String... artists) {
            if(artists.length == 0) {
                return null;
            }
            String artistString = artists[0];
            //Call Spotify and get and artist list
            SpotifyService spotify = new SpotifyApi().getService();
            ArtistsPager results = spotify.searchArtists(artistString);
            return results.artists.items;
        }
        @Override
        protected void onPostExecute(List<Artist> returnedArtists) {
            if (returnedArtists != null) {
                mTrackListAdapter.clear();
                for (Artist artist : returnedArtists) {
                    mTrackListAdapter.add(artist.name);
                }
            }

        }
    }
}


