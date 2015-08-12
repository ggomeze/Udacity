package com.ggomeze.spotifystreamer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ggomeze.spotifystreamer.R;

/**
 * Created by ggomeze on 12/08/15.
 */
public class PlayerFragment extends Fragment{

    public static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private Uri mTrackUri;

    //Mandatory empty constructor for the activity to instantiate
    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent == null) return null;

        mTrackUri = intent.getData();
        View fragment = inflater.inflate(R.layout.fragment_player, container, false);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}