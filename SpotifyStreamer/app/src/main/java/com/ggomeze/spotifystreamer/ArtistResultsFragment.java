package com.ggomeze.spotifystreamer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistResultsFragment extends Fragment {

    private ArrayAdapter<String> mArtistListAdapter;
    private ArrayList<Artist> mReturnedArtists;

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

        ListView artistList = (ListView)fragment.findViewById(R.id.list_view_artists);
        //Create adapter
        mArtistListAdapter = new ArrayAdapter<String>(
                        //Current context (this fragment's parent activity
                        getActivity(),
                        //ID of list item layout
                        R.layout.artist_item,
                        //ID of TextView to populate
                        R.id.artist_list_textview,
                        new ArrayList<String>());

        //Find listView and assign artistList adapter
        artistList.setAdapter(mArtistListAdapter);

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"
                Artist artist = mReturnedArtists.get(position);
                Intent artistDetail = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, artist.id);
                startActivity(artistDetail);
            }
        });
        new SearchArtistsAsyncTask().execute("Coldplay");

        return fragment;
    }

    private class SearchArtistsAsyncTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = SearchArtistsAsyncTask.class.getSimpleName();

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
                mArtistListAdapter.clear();
                mReturnedArtists.clear();
                for (Artist artist : returnedArtists) {
                    mReturnedArtists.add(artist);
                    mArtistListAdapter.add(artist.name);
                }
            }

        }
    }
}


