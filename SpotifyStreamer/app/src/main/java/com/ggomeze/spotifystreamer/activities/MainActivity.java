package com.ggomeze.spotifystreamer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.fragments.ArtistResultsFragment;
import com.ggomeze.spotifystreamer.fragments.PlayerFragment;
import com.ggomeze.spotifystreamer.fragments.TopTracksFragment;
import com.ggomeze.spotifystreamer.utils.Utility;


public class MainActivity extends ActionBarActivity implements ArtistResultsFragment.ArtistCallback, TopTracksFragment.TrackCallback {

    private String mCountryPreference;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top_artist_tracks) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_artist_tracks, new TopTracksFragment(), TopTracksFragment.TOP_TRACKS_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String countryPreference = Utility.getCountryPreference(this);

        // update the countryPreference in our second pane using the fragment manager
        if (countryPreference != null && !countryPreference.equals(mCountryPreference)) {
            ArtistResultsFragment arf = (ArtistResultsFragment)getSupportFragmentManager().findFragmentById(R.id.returned_artists);
            if ( arf != null ) {
                arf.onCountryPreferenceChanged();
            }

            TopTracksFragment ttf = (TopTracksFragment)getSupportFragmentManager().findFragmentByTag(TopTracksFragment.TOP_TRACKS_FRAGMENT_TAG);
            if ( null != ttf ) {
                ttf.onCountryPreferenceChanged(countryPreference);
            }
            mCountryPreference = countryPreference;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent preferences = new Intent(this, SettingsActivity.class);
            startActivity(preferences);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArtistSelected(Uri artistTopTracksUri, String artistName) {
        if (mTwoPane) {

            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.ARTIST_TOP_TRACKS_URI, artistTopTracksUri);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_artist_tracks, fragment, TopTracksFragment.TOP_TRACKS_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent artistDetail = new Intent(this, DetailActivity.class)
                    .setData(artistTopTracksUri);//artists/#/tracks
            //TODO Remove this info and get it from database
            artistDetail.putExtra(getString(R.string.album_intent_extra), artistName);
            startActivity(artistDetail);
        }
    }

    @Override
    public void onTrackSelected(Uri trackUri) {
        Bundle args = new Bundle();
        args.putParcelable(PlayerFragment.TOP_TRACKS_URI, trackUri);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);

        fragment.show(getSupportFragmentManager(), PlayerFragment.PLAYER_FRAGMENT_TAG);
    }
}
