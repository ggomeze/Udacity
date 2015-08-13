package com.ggomeze.spotifystreamer.listeners;

import android.media.MediaPlayer;

import com.ggomeze.spotifystreamer.fragments.PlayerFragment;

import java.lang.ref.WeakReference;

/**
 * Created by ggomeze on 13/08/15.
 */
public class MediaPlayerListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    WeakReference<PlayerFragment> mFragment;

    public MediaPlayerListener(PlayerFragment fragment) {
        mFragment = new WeakReference<PlayerFragment>(fragment);
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.setPlayerButtonOnPause(false);
            mp.reset();
        }

    }

}
