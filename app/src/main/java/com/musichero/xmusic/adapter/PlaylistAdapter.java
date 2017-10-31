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
import com.musichero.xmusic.model.PlaylistModel;
import com.musichero.xmusic.view.MaterialIconView;

import java.util.ArrayList;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:NhacVui
 * @Date:Jul 14, 2015
 */

public class PlaylistAdapter extends DBRecyclerViewAdapter implements IXMusicConstants {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();
    private int mTypeUI;

    private Typeface mTypefaceBold;
    private Typeface mTypefaceLight;

    private OnPlaylistListener onPlaylistListener;
    private LayoutInflater mInflater;

    public PlaylistAdapter(YPYFragmentActivity mContext, ArrayList<PlaylistModel> mListObjects,
                           Typeface mTypefaceBold, Typeface mTypefaceLight, View mHeaderView, int typeUI) {
        super(mContext, mListObjects, mHeaderView);
        this.mContext = mContext;
        this.mTypefaceBold = mTypefaceBold;
        this.mTypefaceLight = mTypefaceLight;
        this.mTypeUI = typeUI;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnPlaylistListener(OnPlaylistListener onPlaylistListener) {
        this.onPlaylistListener = onPlaylistListener;
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder1, int position) {
        if (holder1 instanceof PlaylistHolder) {
            final PlaylistModel mPlaylistModel = (PlaylistModel) mListObjects.get(position);
            final PlaylistHolder mHolder = ((PlaylistHolder) holder1);

            mHolder.mTvPlaylistName.setText(mPlaylistModel.getName());
            long size = mPlaylistModel.getNumberVideo();
            String data;
            if (size <= 1) {
                data = String.format(mContext.getString(R.string.format_number_music), String.valueOf(size));
            } else {
                data = String.format(mContext.getString(R.string.format_number_musics), String.valueOf(size));
            }
            String artwork = mPlaylistModel.getArtwork();
            if (!TextUtils.isEmpty(artwork)) {
                GlideImageLoader.displayImage(mContext, mHolder.mImgPlaylist, artwork, R.drawable.ic_rect_music_default);
            } else {
                Uri mUri = mPlaylistModel.getURI();
                if (mUri != null) {
                    GlideImageLoader.displayImageFromMediaStore(mContext, mHolder.mImgPlaylist, mUri, R.drawable.ic_rect_music_default);
                } else {
                    mHolder.mImgPlaylist.setImageResource(R.drawable.ic_rect_music_default);
                }
            }

            mHolder.mTvNumberMusic.setText(data);
            if (mHolder.mCardView != null) {
                mHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onPlaylistListener != null) {
                            onPlaylistListener.onViewDetail(mPlaylistModel);
                        }
                    }
                });
            } else {
                mHolder.mLayoutRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onPlaylistListener != null) {
                            onPlaylistListener.onViewDetail(mPlaylistModel);
                        }
                    }
                });
            }

            mHolder.mImgMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPlaylistListener != null) {
                        onPlaylistListener.showPopUpMenu(mHolder.mImgMenu, mPlaylistModel);
                    }
                }
            });
        } else if (holder1 instanceof ViewNativeHolder) {
            final PlaylistModel mPlaylistModel = (PlaylistModel) mListObjects.get(position);
            ViewNativeHolder mHolder = (ViewNativeHolder) holder1;
            mHolder.mRootLayoutAds.removeAllViews();
            if (mPlaylistModel.getNativeExpressAdView() == null) {
                NativeExpressAdView mAdView = (NativeExpressAdView) LayoutInflater.from(mContext).inflate(R.layout.item_native, null);
                mPlaylistModel.setNativeExpressAdView(mAdView);
                AdRequest mAdrequest = new AdRequest.Builder()
                        .addTestDevice(TEST_DEVICE).build();
                mAdView.loadAd(mAdrequest);
            } else {
                NativeExpressAdView mAdView = mPlaylistModel.getNativeExpressAdView();
                if (mAdView.getParent() != null) {
                    ((ViewGroup) mAdView.getParent()).removeAllViews();
                }
            }
            mHolder.mRootLayoutAds.addView(mPlaylistModel.getNativeExpressAdView());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        if (viewType == 1) {
            View mView = mInflater.inflate(R.layout.item_native_ads, v, false);
            ViewNativeHolder mHolder = new ViewNativeHolder(mView);
            return mHolder;
        }
        View mView = mInflater.inflate(mTypeUI == TYPE_UI_GRID ? R.layout.item_grid_playlist : R.layout.item_list_playlist, v, false);
        PlaylistHolder mHolder = new PlaylistHolder(mView);
        return mHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0) {
            PlaylistModel mItemListRadio = (PlaylistModel) mListObjects.get(position - 1);
            if (mItemListRadio.isNativeAds()) {
                return 1;
            }
        }
        return super.getItemViewType(position);
    }

    public interface OnPlaylistListener {
        void onViewDetail(PlaylistModel mPlaylistObject);

        void showPopUpMenu(View v, PlaylistModel mPlaylistObject);
    }

    public class PlaylistHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTvPlaylistName;
        public TextView mTvNumberMusic;
        public MaterialIconView mImgMenu;
        public ImageView mImgPlaylist;
        public View mLayoutRoot;

        public PlaylistHolder(View convertView) {
            super(convertView);
            mTvPlaylistName = (TextView) convertView.findViewById(R.id.tv_playlist_name);
            mTvPlaylistName.setTypeface(mTypefaceBold);

            mTvNumberMusic = (TextView) convertView.findViewById(R.id.tv_number_music);
            mTvNumberMusic.setTypeface(mTypefaceLight);

            mCardView = (CardView) convertView.findViewById(R.id.card_view);

            mImgMenu = (MaterialIconView) convertView.findViewById(R.id.img_menu);
            mImgPlaylist = (ImageView) convertView.findViewById(R.id.img_playlist);

            mLayoutRoot = convertView.findViewById(R.id.layout_root);

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
