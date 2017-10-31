package com.musichero.xmusic.fragment;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYMainActivity;
import com.musichero.xmusic.abtractclass.fragment.DBFragment;
import com.musichero.xmusic.adapter.TrackAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.dataMng.MusicNetUtils;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.StringUtils;
import com.musichero.xmusic.view.CircularProgressBar;

import java.util.ArrayList;

public class FragmentYPYSearchTrack extends DBFragment implements IXMusicConstants {

    public static final String TAG = FragmentYPYSearchTrack.class.getSimpleName();

    private YPYMainActivity mContext;

    private RecyclerView mRecyclerView;

    private TextView mTvNoResult;
    private TrackAdapter mTrackAdapter;

    private ArrayList<TrackModel> mListTrackObjects;

    private CircularProgressBar mProgressBar;

    private String mKeyword;
    private boolean isDestroy;
    private int mTypeUI;

    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void findView() {
        mContext = (YPYMainActivity) getActivity();

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.list_datas);

        mTvNoResult = (TextView) mRootView.findViewById(R.id.tv_no_result);
        mTvNoResult.setTypeface(mContext.mTypefaceNormal);

        this.mProgressBar = (CircularProgressBar) mRootView.findViewById(R.id.progressBar1);
        ConfigureModel configureModel = mContext.mTotalMng.getConfigureModel();
        mTypeUI = configureModel != null ? configureModel.getTypeSearch() : TYPE_UI_LIST;
        if (mTypeUI == TYPE_UI_LIST) {
            mContext.setUpRecyclerViewAsListView(mRecyclerView, null);
        } else {
            mContext.setUpRecyclerViewAsGridView(mRecyclerView, 2);
        }

        startSearch(mKeyword);


    }


    private void setUpInfo(final ArrayList<TrackModel> mListTrackObjects) {
        if (isDestroy) {
            return;
        }
        mRecyclerView.setAdapter(null);
        if (this.mListTrackObjects != null) {
            this.mListTrackObjects.clear();
            this.mListTrackObjects = null;
        }
        if (mListTrackObjects != null && mListTrackObjects.size() > 0) {
            this.mListTrackObjects = mListTrackObjects;
            mRecyclerView.setVisibility(View.VISIBLE);
            mTvNoResult.setVisibility(View.GONE);
            mTrackAdapter = new TrackAdapter(mContext, this.mListTrackObjects, mContext.mTypefaceBold,
                    mContext.mTypefaceLight, mTypeUI);
            mRecyclerView.setAdapter(mTrackAdapter);
            mTrackAdapter.setOnTrackListener(new TrackAdapter.OnTrackListener() {
                @Override
                public void onListenTrack(TrackModel mTrackObject) {
                    mContext.startPlayingList(mTrackObject, (ArrayList<TrackModel>) mListTrackObjects.clone());
                }

                @Override
                public void onShowMenu(View mView, TrackModel mTrackObject) {
                    mContext.showPopupMenu(mView, mTrackObject);
                }
            });
        } else {
            mTvNoResult.setText(R.string.title_no_songs);
            mTvNoResult.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        if (mListTrackObjects != null) {
            mListTrackObjects.clear();
            mListTrackObjects = null;
        }

    }

    public void startSearch(String keyword) {
        if (StringUtils.isEmptyString(keyword)) {
            mContext.showToast(R.string.info_empty);
            return;
        }
        if (!ApplicationUtils.isOnline(mContext)) {
            mContext.showToast(R.string.info_lose_internet);
            return;
        }
        this.mKeyword = keyword;
        this.mTvNoResult.setVisibility(View.GONE);
        showLoading(true);
        mRecyclerView.setVisibility(View.GONE);

        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<TrackModel> mListTrack = MusicNetUtils.getListTrackObjectsByQuery(StringUtils.urlEncodeString(mKeyword), 0, MAX_SEARCH_SONG);
                final ArrayList<TrackModel> mListDatas = mContext.mTotalMng.getAllTrackWithAdmob(mContext, mListTrack, mTypeUI);
                if (mListDatas != null && mListDatas.size() > 0) {
                    mListTrack.clear();
                    mListTrack = null;
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        mContext.dimissProgressDialog();
                        if (mListDatas == null) {
                            mContext.showToast(R.string.info_server_error);
                            return;
                        }
                        setUpInfo(mListDatas);
                    }
                });
            }
        });
    }

    public void showLoading(boolean b) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyData() {
        if (mTrackAdapter != null) {
            mTrackAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onExtractData() {
        super.onExtractData();
        Bundle mBundle = getArguments();
        if (mBundle != null) {
            mKeyword = mBundle.getString(KEY_BONUS);
        }
    }

    @Override
    public boolean isCheckBack() {
        return (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE);
    }
}
