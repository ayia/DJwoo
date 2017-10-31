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
import com.musichero.xmusic.adapter.GenreAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.GenreModel;
import com.musichero.xmusic.view.CircularProgressBar;

import java.util.ArrayList;

public class FragmentYPYGenre extends DBFragment implements IXMusicConstants {

    public static final String TAG = FragmentYPYGenre.class.getSimpleName();

    private YPYMainActivity mContext;

    private TextView mTvNoResult;
    private CircularProgressBar mProgressBar;
    private RecyclerView mRecyclerViewTrack;
    private int mTypeUI;
    private GenreAdapter mGenreAdapter;
    private ArrayList<GenreModel> mListGenres;


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
        mTypeUI = configureModel != null ? configureModel.getTypeGenre() : TYPE_UI_LIST;
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
            startGetData();
        }
    }


    private void startGetData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTvNoResult.setVisibility(View.GONE);
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<GenreModel> mList = mContext.mTotalMng.getListGenreObjects();
                if (mList == null) {
                    mContext.mTotalMng.readGenreData(mContext);
                    mList = mContext.mTotalMng.getListGenreObjects();
                }
                final ArrayList<GenreModel> finalMList = mList;
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        setUpInfo(finalMList);
                    }
                });
            }
        });
    }

    private void setUpInfo(ArrayList<GenreModel> mListTracks) {
        mRecyclerViewTrack.setAdapter(null);
        if (this.mListGenres != null) {
            this.mListGenres.clear();
            this.mListGenres = null;
        }
        this.mListGenres = mListTracks;
        if (mListTracks != null && mListTracks.size() > 0) {
            mGenreAdapter = new GenreAdapter(mContext, mListTracks, mContext.mTypefaceBold, mTypeUI);
            mRecyclerViewTrack.setAdapter(mGenreAdapter);
            mGenreAdapter.setOnGenreListener(new GenreAdapter.OnGenreListener() {
                @Override
                public void goToDetail(GenreModel mGenreObject) {
                    mContext.goToGenre(mGenreObject);
                }
            });
        }
        updateInfo();

    }

    private void updateInfo() {
        if (mTvNoResult != null) {
            boolean b = mListGenres != null && mListGenres.size() > 0;
            mTvNoResult.setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListGenres != null) {
            mListGenres.clear();
            mListGenres = null;
        }
    }
}
