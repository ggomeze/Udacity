package com.ggomeze.spotifystreamer.listeners;

import android.media.MediaPlayer;
import android.widget.SeekBar;

import com.ggomeze.spotifystreamer.fragments.PlayerFragment;
import com.ggomeze.spotifystreamer.utils.Utility;

import java.lang.ref.WeakReference;

/**
 * Created by ggomeze on 13/08/15.
 */
public class MediaPlayerListener implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    WeakReference<PlayerFragment> mFragment;

    public MediaPlayerListener(PlayerFragment fragment) {
        mFragment = new WeakReference<PlayerFragment>(fragment);
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.startReadyPlayer();//Update with current set position
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.setPlayerButtonOnPause(false);
            mp.reset();
            fragment.updatePlayerWithSeekBarPosition(0);
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.updatePlayerTimers(Utility.convertMillisToText(progress), Utility.convertMillisToText(30999 - progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.updatePlayerWithSeekBarPosition(seekBar.getProgress());
        }
    }
}
