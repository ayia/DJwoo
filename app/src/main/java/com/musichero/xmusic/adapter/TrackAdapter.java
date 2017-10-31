package com.musichero.xmusic.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYFragmentActivity;
import com.musichero.xmusic.abtractclass.DBRecyclerViewAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.imageloader.GlideImageLoader;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.utils.StringUtils;
import com.musichero.xmusic.view.MaterialIconView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:NhacVui
 * @Date:Jul 14, 2015
 */

public class TrackAdapter extends DBRecyclerViewAdapter implements IXMusicConstants {

    public static final String TAG = TrackAdapter.class.getSimpleName();
    private int mType;
    private Typeface mTypefaceBold;
    private Typeface mTypefaceLight;

    private OnTrackListener onTrackListener;
    private LayoutInflater mInflater;

    public TrackAdapter(YPYFragmentActivity mContext, ArrayList<TrackModel> mListObjects,
                        Typeface mTypefaceBold, Typeface mTypefaceLight, int type) {
        super(mContext, mListObjects);
        this.mTypefaceBold = mTypefaceBold;
        this.mListObjects = mListObjects;
        this.mTypefaceLight = mTypefaceLight;
        this.mType = type;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setOnTrackListener(OnTrackListener onTrackListener) {
        this.onTrackListener = onTrackListener;
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position) {
        final TrackModel mTrackObject = (TrackModel) mListObjects.get(position);
        if (holder instanceof TrackHolder) {
            final TrackHolder mTrackHolder = (TrackHolder) holder;
            mTrackHolder.mTvSongName.setText(mTrackObject.getTitle());
            String author = mTrackObject.getAuthor();
            if (StringUtils.isEmptyString(author) || author.toLowerCase(Locale.US).contains(PREFIX_UNKNOWN)) {
                author = mContext.getString(R.string.title_unknown);
            }
            mTrackHolder.mTvSinger.setText(author);

            String artwork = mTrackObject.getArtworkUrl();
            if (!TextUtils.isEmpty(artwork)) {
                GlideImageLoader.displayImage(mContext, mTrackHolder.mImgSongs, artwork, R.drawable.ic_rect_music_default);
            } else {
                Uri mUri = mTrackObject.getURI();
                if (mUri != null) {
                    GlideImageLoader.displayImageFromMediaStore(mContext, mTrackHolder.mImgSongs, mUri, R.drawable.ic_rect_music_default);
                } else {
                    mTrackHolder.mImgSongs.setImageResource(R.drawable.ic_rect_music_default);
                }
            }

            if (mTrackHolder.mCardView != null) {
                mTrackHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onTrackListener != null) {
                            onTrackListener.onListenTrack(mTrackObject);
                        }
                    }
                });
            } else {
                mTrackHolder.mLayoutRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onTrackListener != null) {
                            onTrackListener.onListenTrack(mTrackObject);
                        }
                    }
                });
            }


            mTrackHolder.mImgMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTrackListener != null) {
                        onTrackListener.onShowMenu(mTrackHolder.mImgMenu, mTrackObject);
                    }
                }
            });
        } else if (holder instanceof ViewNativeHolder) {
            ViewNativeHolder mHolder = (ViewNativeHolder) holder;
            mHolder.mRootLayoutAds.removeAllViews();
            if (mTrackObject.getNativeExpressAdView() == null) {
                NativeExpressAdView mAdView = (NativeExpressAdView) LayoutInflater.from(mContext).inflate(R.layout.item_native, null);
                mTrackObject.setNativeExpressAdView(mAdView);
                AdRequest mAdrequest = new AdRequest.Builder()
                        .addTestDevice(TEST_DEVICE).build();
                mAdView.loadAd(mAdrequest);
            } else {
                NativeExpressAdView mAdView = mTrackObject.getNativeExpressAdView();
                if (mAdView.getParent() != null) {
                    ((ViewGroup) mAdView.getParent()).removeAllViews();
                }
            }
            mHolder.mRootLayoutAds.addView(mTrackObject.getNativeExpressAdView());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        RecyclerView.ViewHolder mHolder = null;
        if (viewType == 0) {
            View mView = mInflater.inflate(mType == TYPE_UI_GRID ? R.layout.item_grid_track : R.layout.item_list_track, v, false);
            mHolder = new TrackHolder(mView);
        } else if (viewType == 1) {
            View mView = mInflater.inflate(R.layout.item_native_ads, v, false);
            mHolder = new ViewNativeHolder(mView);
        }
        return mHolder;
    }

    @Override
    public int getItemViewType(int position) {
        TrackModel mItemListRadio = (TrackModel) mListObjects.get(position);
        if (mItemListRadio.isNativeAds()) {
            return 1;
        }
        return 0;
    }


    public interface OnTrackListener {
        void onListenTrack(TrackModel mTrackObject);

        void onShowMenu(View mView, TrackModel mTrackObject);
    }

    public class TrackHolder extends RecyclerView.ViewHolder {
        public ImageView mImgSongs;
        public MaterialIconView mImgMenu;
        public TextView mTvSongName;
        public TextView mTvSinger;
        public View mLayoutRoot;
        public CardView mCardView;

        public TrackHolder(View convertView) {
            super(convertView);
            mImgSongs = (ImageView) convertView.findViewById(R.id.img_songs);
            mImgMenu = (MaterialIconView) convertView.findViewById(R.id.img_menu);

            mTvSongName = (TextView) convertView.findViewById(R.id.tv_song);
            mTvSinger = (TextView) convertView.findViewById(R.id.tv_singer);
            mCardView = (CardView) convertView.findViewById(R.id.card_view);

            mLayoutRoot = convertView.findViewById(R.id.layout_root);
            mTvSongName.setTypeface(mTypefaceBold);
            mTvSinger.setTypeface(mTypefaceLight);

        }
    }

    public class ViewNativeHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mRootLayoutAds;

        public ViewNativeHolder(View convertView) {
            super(convertView);
            mRootLayoutAds = (RelativeLayout) convertView.findViewById(R.id.layout_ad_root);
        }
    }


}
