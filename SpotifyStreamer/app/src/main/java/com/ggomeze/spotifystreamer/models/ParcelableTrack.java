package com.ggomeze.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ggomeze on 11/07/15.
 * TODO Maybe better create a wrapper than extending
 */
public class ParcelableTrack extends Track implements Parcelable {

    private String mThumbnailUrl = "";

    private ParcelableTrack(Parcel in) {
        super();
        name = in.readString();
        album = new Album();
        album.name = in.readString();
        mThumbnailUrl = in.readString();
    }

    public ParcelableTrack(Track artist) {
        super();
        name = artist.name;
        album = artist.album;
    }

    public String getThumbnailUrl() {
        if (mThumbnailUrl.isEmpty()) {
            List<Image> images = album.images;
            Integer lowerResolution = 0;
            for (Image image : images) {
                Integer height = image.height;
                if (lowerResolution == 0 || height > lowerResolution) {
                    mThumbnailUrl = image.url;
                }
            }
        }
        return mThumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(album.name);
        parcel.writeString(getThumbnailUrl());
    }

    public final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel parcel) {
            return new ParcelableTrack(parcel);
        }

        @Override
        public ParcelableTrack[] newArray(int i) {
            return new ParcelableTrack[i];
        }
    };
}
