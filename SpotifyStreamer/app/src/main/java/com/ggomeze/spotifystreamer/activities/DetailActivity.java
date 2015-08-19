/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ggomeze.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.fragments.TopTracksFragment;
import com.ggomeze.spotifystreamer.utils.Utility;

public class DetailActivity extends ActionBarActivity {

    private String mCountryPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_artist_tracks, new TopTracksFragment(), TopTracksFragment.TOP_TRACKS_FRAGMENT_TAG)
                    .commit();
        }

        ActionBar ab = getSupportActionBar();
        ab.setTitle(getString(R.string.top_10_tracks));

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(getString(R.string.album_intent_extra))){
            ab.setSubtitle(intent.getStringExtra(getString(R.string.album_intent_extra)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String countryPreference = Utility.getCountryPreference(this);

        // update the countryPreference in our second pane using the fragment manager
        if (countryPreference != null && !countryPreference.equals(mCountryPreference)) {
            TopTracksFragment ttf = (TopTracksFragment)getSupportFragmentManager().findFragmentByTag(TopTracksFragment.TOP_TRACKS_FRAGMENT_TAG);
            if ( null != ttf ) {
                ttf.onCountryPreferenceChanged(countryPreference);
            }
            mCountryPreference = countryPreference;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
}