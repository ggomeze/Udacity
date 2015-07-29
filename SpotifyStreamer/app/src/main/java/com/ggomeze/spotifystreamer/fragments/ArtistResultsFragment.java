package com.ggomeze.spotifystreamer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.activities.DetailActivity;
import com.ggomeze.spotifystreamer.adapters.ArtistAdapter;
import com.ggomeze.spotifystreamer.models.ParcelableArtist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistResultsFragment extends Fragment {

    static final String RESTURNED_ARTISTS = "returnedArtists";

    private ArrayList<ParcelableArtist> mReturnedArtists;
    private ArtistAdapter mArtistAdapter;
    private EditText mSearchText;

    //Mandatory empty constructor for the activity to instantiate
    public ArtistResultsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnedArtists = new ArrayList<ParcelableArtist>();
        mArtistAdapter = new ArtistAdapter(getActivity(), 0, mReturnedArtists);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_main, container, false);

        ListView artistList = (ListView)fragment.findViewById(R.id.list_view_artists);
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


        artistList.setAdapter(mArtistAdapter);

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"
                ParcelableArtist artist = mReturnedArtists.get(position);
                Intent artistDetail = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, artist.id);
                artistDetail.putExtra(getString(R.string.album_intent_extra), artist.name);
                startActivity(artistDetail);
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

    public void searchArtists() {
        String text = mSearchText.getText().toString();
        if (!(text.isEmpty()))
            new SearchArtistsAsyncTask(getActivity()).execute(text);
    }

    private class SearchArtistsAsyncTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = SearchArtistsAsyncTask.class.getSimpleName();
        private WeakReference<Context> mContext;

        public SearchArtistsAsyncTask (Context context){
            mContext = new WeakReference<Context>(context);
        }
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected List<Artist> doInBackground(String... artists) {
            if(artists.length == 0) {
                return null;
            }
            String artistString = artists[0];
            SpotifyService spotify = new SpotifyApi().getService();
            try {
                ArtistsPager results = spotify.searchArtists(artistString);
                return results.artists.items;
            } catch (Exception exception) {
                Log.e(LOG_TAG,getString(R.string.connection_error));
                return null;
            }
        }
        @Override
        protected void onPostExecute(List<Artist> returnedArtists) {
            if (returnedArtists != null) {
                if (returnedArtists.size() > 0) {
                    mArtistAdapter.clear();
                    for (Artist artist : returnedArtists) {
                        mReturnedArtists.add(new ParcelableArtist(artist));
                    }
                } else {
                    Context context = mContext.get();
                    if (context != null) {
                        Toast.makeText(context, getString(R.string.no_artists_found), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Context context = mContext.get();
                if (context != null) {
                    Toast.makeText(context, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}


