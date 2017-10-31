package com.musichero.xmusic.fragment;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYMainActivity;
import com.musichero.xmusic.abtractclass.fragment.DBFragment;
import com.musichero.xmusic.adapter.TrackAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.constants.IXmusicSoundCloudConstants;
import com.musichero.xmusic.dataMng.MusicNetUtils;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.view.CircularProgressBar;

import java.util.ArrayList;

public class FragmentYPYTopChart extends DBFragment implements IXMusicConstants {

    public static final String TAG = FragmentYPYTopChart.class.getSimpleName();

    private YPYMainActivity mContext;

    private TextView mTvNoResult;
    private boolean isDestroy;
    private CircularProgressBar mProgressBar;
    private RecyclerView mRecyclerViewTrack;
    private int mTypeUI;
    private TrackAdapter mTrackAdapter;
    private ArrayList<TrackModel> mListTracks;
    private ArrayList<TrackModel> mOriginalTrack;


    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void findView() {
        mContext = (YPYMainActivity) getActivity();

        mContext = (YPYMainActivity) getActivity();

        mTvNoResult = (TextView) mRootView.findViewById(R.id.tv_no_result);
        mTvNoResult.setTypeface(mContext.mTypefaceNormal);

        mRecyclerViewTrack = (RecyclerView) mRootView.findViewById(R.id.list_datas);
        mProgressBar = (CircularProgressBar) mRootView.findViewById(R.id.progressBar1);

        ConfigureModel configureModel = mContext.mTotalMng.getConfigureModel();
        mTypeUI = configureModel != null ? configureModel.getTypeTopChart() : TYPE_UI_LIST;
        if (mTypeUI == TYPE_UI_LIST) {
            mContext.setUpRecyclerViewAsListView(mRecyclerViewTrack, null);
        } else {
            mContext.setUpRecyclerViewAsGridView(mRecyclerViewTrack, 2);
        }
        if (isFirstInTab()) {
            startLoadData();
        }

    }

    @Override
    public void startLoadData() {
        if (!isLoadingData() && mContext != null) {
            setLoadingData(true);
            startGetData(true);
        }
    }

    @Override
    public void onNetworkChange(boolean isNetworkOn) {
        super.onNetworkChange(isNetworkOn);
        if (isNetworkOn) {
            if (mTrackAdapter == null && mContext != null && mProgressBar != null) {
                startGetData(true);
            }
        }
    }


    private void startGetData(final boolean isNeedShowProgress) {
        if (!ApplicationUtils.isOnline(mContext)) {
            mProgressBar.setVisibility(View.GONE);
            mTvNoResult.setVisibility(View.VISIBLE);
            mTvNoResult.setText(R.string.info_lose_internet);
            return;
        }
        if (mProgressBar.getVisibility() == View.VISIBLE && !isNeedShowProgress) {
            return;
        }
        mTvNoResult.setText(R.string.title_no_songs);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvNoResult.setVisibility(View.GONE);
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                ConfigureModel mModel = mContext.mTotalMng.getConfigureModel();
                String genre = mModel != null ? mModel.getTopChartGenre() : IXmusicSoundCloudConstants.ALL_MUSIC_GENRE;
                String kind = mModel != null ? mModel.getTopChartKind() : IXmusicSoundCloudConstants.KIND_TOP;
                Log.e("DCM", "=====>genre=" + genre + "==>kind=" + kind);
                final ArrayList<TrackModel> mTracks = MusicNetUtils.getListHotTrackObjectsInGenre(genre, kind, 0, MAX_SONG_CACHED);
                final ArrayList<TrackModel> mListFinalTrack = mContext.mTotalMng.getAllTrackWithAdmob(mContext, mTracks, mTypeUI);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDestroy) {
                            mProgressBar.setVisibility(View.GONE);
                            setUpInfo(mListFinalTrack, mTracks);
                        }

                    }
                });
            }
        });
    }

    private void setUpInfo(ArrayList<TrackModel> mListTracks, final ArrayList<TrackModel> mOriginalTrack) {
        mRecyclerViewTrack.setAdapter(null);
        if (this.mListTracks != null) {
            this.mListTracks.clear();
            this.mListTracks = null;
        }
        if (this.mOriginalTrack != null) {
            this.mOriginalTrack.clear();
            this.mOriginalTrack = null;
        }
        this.mListTracks = mListTracks;
        this.mOriginalTrack = mOriginalTrack;
        if (mListTracks != null && mListTracks.size() > 0) {
            mTrackAdapter = new TrackAdapter(mContext, mListTracks, mContext.mTypefaceBold
                    , mContext.mTypefaceLight, mTypeUI);
            mRecyclerViewTrack.setAdapter(mTrackAdapter);
            mTrackAdapter.setOnTrackListener(new TrackAdapter.OnTrackListener() {
                @Override
                public void onListenTrack(TrackModel mTrackObject) {
                    mContext.hiddenKeyBoardForSearchView();
                    mContext.startPlayingList(mTrackObject, mOriginalTrack);
                }

                @Override
                public void onShowMenu(View mView, TrackModel mTrackObject) {
                    mContext.showPopupMenu(mView, mTrackObject);
                }
            });
        }
        updateInfo();

    }

    private void updateInfo() {
        if (mTvNoResult != null) {
            boolean b = mListTracks != null && mListTracks.size() > 0;
            mTvNoResult.setVisibility(b ? View.GONE : View.VISIBLE);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        if (mOriginalTrack != null) {
            mOriginalTrack.clear();
            mOriginalTrack = null;
        }
        if (mListTracks != null) {
            mListTracks.clear();
            mListTracks = null;
        }
    }
}
