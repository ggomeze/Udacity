package com.ggomeze.spotifystreamer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ggomeze.spotifystreamer.R;
import com.ggomeze.spotifystreamer.fragments.PlayerFragment;

/**
 * Created by ggomeze on 12/08/15.
 */
public class PlayerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            PlayerFragment fragment = new PlayerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.player_frame_layout, fragment, PlayerFragment.PLAYER_FRAGMENT_TAG).commit();
        }
    }
}
