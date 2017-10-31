package com.musichero.xmusic.model;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.annotations.SerializedName;
import com.musichero.xmusic.dataMng.MusicNetUtils;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.StringUtils;

import java.util.Date;

public class TrackModel {

    @SerializedName("id")
    private long id;


    @SerializedName("duration")
    private long duration;

    @SerializedName("title")
    private String title;

    @SerializedName("artwork_url")
    private String artworkUrl;

    @SerializedName("user")
    private UserModel userObject;

    @SerializedName("path")
    private String path;

    @SerializedName("permalink_url")
    private String permalinkUrl;

    private transient Date dateCreated;

    private transient boolean isNativeAds;
    private transient NativeExpressAdView nativeExpressAdView;

    public TrackModel(boolean isNativeAds) {
        this.isNativeAds = isNativeAds;
    }

    public TrackModel(long id, long duration, String title, String artworkUrl) {
        super();
        this.id = id;
        this.duration = duration;
        this.title = title;
        this.artworkUrl = artworkUrl;
    }

    public TrackModel(String path, String title) {
        this.path = path;
        this.title = title;
    }


    public TrackModel() {
        super();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtworkUrl() {
        if (StringUtils.isEmptyString(artworkUrl)) {
            if (userObject != null) {
                artworkUrl = userObject.getAvatar();
            }
        }
        if (!StringUtils.isEmptyString(artworkUrl) && artworkUrl.contains("large")) {
            this.artworkUrl = artworkUrl.replace("large", "crop");
        }
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
        if (!StringUtils.isEmptyString(artworkUrl)) {
            this.artworkUrl = artworkUrl.replace("large", "crop");
        }
    }

    public TrackModel clone() {
        TrackModel mTrackObject;
        if (StringUtils.isEmptyString(path)) {
            mTrackObject = new TrackModel(id, duration, title, artworkUrl);
            if (userObject != null) {
                mTrackObject.setUserObject(userObject.cloneObject());
            }
            mTrackObject.setPermalinkUrl(permalinkUrl);
        } else {
            mTrackObject = new TrackModel(path, title);
            mTrackObject.setArtworkUrl(artworkUrl);
            if (userObject != null) {
                mTrackObject.setUserObject(userObject.cloneObject());
            }
            mTrackObject.setDuration(duration);
            mTrackObject.setDateCreated(dateCreated);
            mTrackObject.setId(id);
        }
        return mTrackObject;
    }


    public String getAuthor() {
        if (userObject != null) {
            return userObject.getUsername();
        }
        return null;
    }


    public String getPermalinkUrl() {
        return permalinkUrl;
    }

    public void setPermalinkUrl(String permalinkUrl) {
        this.permalinkUrl = permalinkUrl;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Uri getURI() {
        return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }


    public boolean isNativeAds() {
        return isNativeAds;
    }

    public void setNativeAds(boolean nativeAds) {
        isNativeAds = nativeAds;
    }

    public void setUserObject(UserModel userObject) {
        this.userObject = userObject;
    }

    public String startGetLinkStream(Context mContext) {
        if (!StringUtils.isEmptyString(path)) {
            return path;
        } else {
            if (ApplicationUtils.isOnline(mContext)) {
                String finalUrl = MusicNetUtils.getLinkStreamFromSoundCloud(id);
                return finalUrl;
            }
        }
        return null;
    }

    public NativeExpressAdView getNativeExpressAdView() {
        return nativeExpressAdView;
    }

    public void setNativeExpressAdView(NativeExpressAdView nativeExpressAdView) {
        this.nativeExpressAdView = nativeExpressAdView;
    }
}
