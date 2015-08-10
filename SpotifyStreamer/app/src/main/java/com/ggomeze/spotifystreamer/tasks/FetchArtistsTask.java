package com.ggomeze.spotifystreamer.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.data.ArtistContract;
import com.ggomeze.spotifystreamer.utils.Utility;

import java.lang.ref.WeakReference;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

    private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

    private WeakReference<Context> mContext;

    public FetchArtistsTask(Context context){
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

        Context context = mContext.get();
        try {
            ArtistsPager results = spotify.searchArtists(artistString);
            List<Artist> returnedArtists = results.artists.items;
            if (returnedArtists.size() > 0 && context != null) {
                context.getContentResolver().bulkInsert(ArtistContract.ArtistEntry.CONTENT_URI,
                        Utility.getContentValuesFromArtistList(returnedArtists));
            }
            return returnedArtists;
        } catch (Exception exception) {
            Log.e(LOG_TAG, context.getString(R.string.connection_error));
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<Artist> returnedArtists) {
        Context context = mContext.get();
        if (returnedArtists != null) {
            if (returnedArtists.isEmpty())  {
                if (context != null) {
                    Toast.makeText(context, context.getString(R.string.no_artists_found), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}