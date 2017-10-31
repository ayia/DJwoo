package com.musichero.xmusic.fragment;

import android.os.Bundle;
import android.os.Handler;
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
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.view.CircularProgressBar;

import java.util.ArrayList;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:MusicPlayer
 * @Date:Dec 25, 2014
 */
public class FragmentYPYMyMusic extends DBFragment implements IXMusicConstants {

    public static final String TAG = FragmentYPYMyMusic.class.getSimpleName();

    private YPYMainActivity mContext;
    private TextView mTvNoResult;
    private RecyclerView mRecyclerViewTrack;
    private ArrayList<TrackModel> mListTracks;

    private Handler mHandler = new Handler();

    private CircularProgressBar mProgressBar;
    private TrackAdapter mTrackAdapter;
    private boolean isSearching;
    private int mTypeUI;

    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void findView() {
        mContext = (YPYMainActivity) getActivity();

        mTvNoResult = (TextView) mRootView.findViewById(R.id.tv_no_result);
        mTvNoResult.setTypeface(mContext.mTypefaceNormal);

        mRecyclerViewTrack = (RecyclerView) mRootView.findViewById(R.id.list_datas);
        mProgressBar = (CircularProgressBar) mRootView.findViewById(R.id.progressBar1);

        ConfigureModel configureModel = mContext.mTotalMng.getConfigureModel();
        mTypeUI = configureModel != null ? configureModel.getTypeMyMusic() : TYPE_UI_LIST;
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
        super.startLoadData();
        if ((!isLoadingData() || isSearching) && mContext != null) {
            setLoadingData(true);
            isSearching = false;
            startGetData();
        }
    }

    private void startGetData() {
        mTvNoResult.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<TrackModel> mTracks = mContext.mTotalMng.getListLibraryTrackObjects();
                if (mTracks == null) {
                    mContext.mTotalMng.readLibraryTrack(mContext);
                    mTracks = mContext.mTotalMng.getListLibraryTrackObjects();
                }
                final ArrayList<TrackModel> mListLibrary = mContext.mTotalMng.getAllTrackWithAdmob(mContext, mTracks, mTypeUI);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        setUpInfo(mListLibrary);
                    }
                });
            }
        });
    }

    public void startSearchData(final String keyword) {
        if (mContext != null && mRecyclerViewTrack != null) {
            isSearching = true;
            DBExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<TrackModel> mTracks = mContext.mTotalMng.startSearchSong(keyword);
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUpInfo(mTracks);
                        }
                    });
                }
            });
        }

    }


    private void setUpInfo(ArrayList<TrackModel> mListTracks) {
        mRecyclerViewTrack.setAdapter(null);
        if (this.mListTracks != null) {
            this.mListTracks.clear();
            this.mListTracks = null;
        }
        this.mListTracks = mListTracks;
        if (mListTracks != null && mListTracks.size() > 0) {
            mTrackAdapter = new TrackAdapter(mContext, mListTracks, mContext.mTypefaceBold
                    , mContext.mTypefaceLight, mTypeUI);
            mRecyclerViewTrack.setAdapter(mTrackAdapter);
            mTrackAdapter.setOnTrackListener(new TrackAdapter.OnTrackListener() {
                @Override
                public void onListenTrack(TrackModel mTrackObject) {
                    mContext.hiddenKeyBoardForSearchView();
                    mContext.startPlayingList(mTrackObject, mContext.mTotalMng.getListLibraryTrackObjects());
                }

                @Override
                public void onShowMenu(View mView, TrackModel mTrackObject) {
                    mContext.showPopupMenu(mView, mTrackObject);
                }
            });
        }
        updateInfo();

    }


    @Override
    public void onResume() {
        super.onResume();
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
        mHandler.removeCallbacksAndMessages(null);
        if (mListTracks != null) {
            mListTracks.clear();
            mListTracks = null;
        }
    }

    @Override
    public void notifyData() {
        startGetData();
    }


}
