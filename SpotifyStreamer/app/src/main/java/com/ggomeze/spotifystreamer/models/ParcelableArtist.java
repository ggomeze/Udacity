package com.ggomeze.spotifystreamer.models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.ggomeze.spotifystreamer.data.ArtistContract;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by ggomeze on 11/07/15.
 * TODO Maybe better create a wrapper than extending
 */
public class ParcelableArtist extends Artist implements Parcelable {

    public String mThumbnailUrl = "";

    private ParcelableArtist(Parcel in) {
        super();
        name = in.readString();
        mThumbnailUrl = in.readString();
    }

    public ParcelableArtist(Artist artist) {
        super();
        name = artist.name;
        images = artist.images;
        id = artist.id;
    }

    public String getThumbnailUrl() {
        if (mThumbnailUrl.isEmpty()) {
            Integer lowerResolution = 0;
            for (Image image : images) {
                Integer height = image.height;
                if (lowerResolution == 0 || height < lowerResolution) {
                    lowerResolution = height;
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
        parcel.writeString(getThumbnailUrl());
    }

    public final Parcelable.Creator<ParcelableArtist> CREATOR = new Parcelable.Creator<ParcelableArtist>() {
        @Override
        public ParcelableArtist createFromParcel(Parcel parcel) {
            return new ParcelableArtist(parcel);
        }

        @Override
        public ParcelableArtist[] newArray(int i) {
            return new ParcelableArtist[i];
        }
    };

    public ContentValues getArtistContentValues() {
        ContentValues artistValues = new ContentValues();
        artistValues.put(ArtistContract.ArtistEntry.COLUMN_IMAGE_THUMB, getThumbnailUrl());
        artistValues.put(ArtistContract.ArtistEntry.COLUMN_ARTIST_NAME, name);
        artistValues.put(ArtistContract.ArtistEntry.COLUMN_SPOTIFY_ARTIST_ID, id);

        return artistValues;
    }
}
