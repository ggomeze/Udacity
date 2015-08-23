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

    private WeakReference<PlayerFragment> mFragment;

    public MediaPlayerListener(PlayerFragment fragment) {
        mFragment = new WeakReference<>(fragment);
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.prepared();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.playerCompleted();
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.updatePlayerTimers(Utility.convertMillisToText(100 +   progress), Utility.convertMillisToText(30900 - progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.isOnTouch(true);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PlayerFragment fragment = mFragment.get();
        if(fragment!=null){
            fragment.updatePlayerWithPosition(seekBar.getProgress());
            fragment.isOnTouch(false);
        }
    }
}
