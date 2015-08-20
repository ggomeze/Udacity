package com.ggomeze.spotifystreamer.models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.ggomeze.spotifystreamer.data.TrackContract;

import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ggomeze on 11/07/15.
 * TODO Maybe better create a wrapper than extending
 */
public class ParcelableTrack extends Track implements Parcelable {

    public String mThumbnailUrl = "";

    private ParcelableTrack(Parcel in) {
        super();
        name = in.readString();
        album = new Album();
        album.name = in.readString();
        mThumbnailUrl = in.readString();
        preview_url = in.readString();
        id = in.readString();
    }

    public ParcelableTrack(Track track) {
        super();
        name = track.name;
        album = track.album;
        preview_url = track.preview_url;
        id = track.id;
    }

    //For the case of track we get the best resolution image, as it will be displayed bigger on the player
    public String getThumbnailUrl() {
        if (mThumbnailUrl.isEmpty()) {
            List<Image> images = album.images;
            Integer higherResolution = 0;
            for (Image image : images) {
                Integer height = image.height;
                if (higherResolution == 0 || height > higherResolution) {
                    higherResolution = height;
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
        parcel.writeString(preview_url);
        parcel.writeString(id);
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

    public ContentValues getTrackContentValues() {
        ContentValues trackValues = new ContentValues();
        trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_THUMB, getThumbnailUrl());
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_URL, preview_url);
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_NAME, name);
        trackValues.put(TrackContract.TrackEntry.COLUMN_TRACK_ID, id);
        trackValues.put(TrackContract.TrackEntry.COLUMN_ALBUM_NAME, album.name);
        //TODO Analyse returned images to see which is more appropriated
        trackValues.put(TrackContract.TrackEntry.COLUMN_IMAGE_MED, getThumbnailUrl());
        return trackValues;
    }
}
