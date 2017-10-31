package com.musichero.xmusic.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYFragmentActivity;
import com.musichero.xmusic.abtractclass.DBRecyclerViewAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.imageloader.GlideImageLoader;
import com.musichero.xmusic.model.GenreModel;

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

public class GenreAdapter extends DBRecyclerViewAdapter implements IXMusicConstants {

    public static final String TAG = GenreAdapter.class.getSimpleName();
    private int mType;
    private Typeface mTypefaceBold;

    private OnGenreListener onGenreListener;
    private LayoutInflater mInflater;

    public GenreAdapter(YPYFragmentActivity mContext, ArrayList<GenreModel> mListObjects,
                        Typeface mTypefaceBold, int type) {
        super(mContext, mListObjects);
        this.mTypefaceBold = mTypefaceBold;
        this.mListObjects = mListObjects;
        this.mType = type;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setOnGenreListener(OnGenreListener onTrackListener) {
        this.onGenreListener = onTrackListener;
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GenreModel mGenreObject = (GenreModel) mListObjects.get(position);
        final GenreHolder mGenreHolder = (GenreHolder) holder;
        mGenreHolder.mTvGenreName.setText(mGenreObject.getName());

        String artwork = mGenreObject.getImg();
        if (!TextUtils.isEmpty(artwork)) {
            GlideImageLoader.displayImage(mContext, mGenreHolder.mImgGenre, artwork, R.drawable.ic_rect_music_default);
        } else {
            mGenreHolder.mImgGenre.setImageResource(R.drawable.ic_rect_music_default);
        }

        if (mGenreHolder.mCardView != null) {
            mGenreHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onGenreListener != null) {
                        onGenreListener.goToDetail(mGenreObject);
                    }
                }
            });
        } else {
            mGenreHolder.mLayoutRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onGenreListener != null) {
                        onGenreListener.goToDetail(mGenreObject);
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        View mView = mInflater.inflate(mType == TYPE_UI_GRID ? R.layout.item_grid_genre : R.layout.item_list_genre, v, false);
        RecyclerView.ViewHolder mHolder = new GenreHolder(mView);
        return mHolder;
    }

    public interface OnGenreListener {
        void goToDetail(GenreModel mTrackObject);
    }

    public class GenreHolder extends RecyclerView.ViewHolder {
        public ImageView mImgGenre;
        public TextView mTvGenreName;
        public View mLayoutRoot;
        public CardView mCardView;

        public GenreHolder(View convertView) {
            super(convertView);
            mImgGenre = (ImageView) convertView.findViewById(R.id.img_genre);
            mTvGenreName = (TextView) convertView.findViewById(R.id.tv_genre_name);
            mCardView = (CardView) convertView.findViewById(R.id.card_view);
            mLayoutRoot = convertView.findViewById(R.id.layout_root);
            mTvGenreName.setTypeface(mTypefaceBold);

        }
    }


}
