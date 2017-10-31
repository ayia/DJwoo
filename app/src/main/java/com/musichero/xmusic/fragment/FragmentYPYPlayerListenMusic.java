package com.musichero.xmusic.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYMainActivity;
import com.musichero.xmusic.abtractclass.fragment.DBFragment;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.dataMng.MusicDataMng;
import com.musichero.xmusic.imageloader.GlideImageLoader;
import com.musichero.xmusic.imageloader.target.GlideViewGroupTarget;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.task.IYPYCallback;
import com.musichero.xmusic.utils.StringUtils;
import com.musichero.xmusic.view.CircularProgressBar;
import com.musichero.xmusic.view.MaterialIconView;
import com.musichero.xmusic.view.SliderView;

import java.util.ArrayList;

import eu.gsottbauer.equalizerview.EqualizerView;

import static com.musichero.xmusic.playservice.IYPYMusicConstant.ACTION_NEXT;
import static com.musichero.xmusic.playservice.IYPYMusicConstant.ACTION_PLAY;
import static com.musichero.xmusic.playservice.IYPYMusicConstant.ACTION_PREVIOUS;
import static com.musichero.xmusic.playservice.IYPYMusicConstant.ACTION_TOGGLE_PLAYBACK;

public class FragmentYPYPlayerListenMusic extends DBFragment implements IXMusicConstants, View.OnClickListener {

    public static final String TAG = FragmentYPYPlayerListenMusic.class.getSimpleName();

    public static final int[] RES_ID_CLICKS = {R.id.btn_close,
            R.id.img_share, R.id.btn_next, R.id.btn_prev, R.id.img_add_playlist
            , R.id.img_equalizer, R.id.img_sleep_mode};

    public static final int[] RES_IMAGE = {R.drawable.ic_arrow_down_white_36dp,
            R.drawable.ic_share_white_36dp, R.drawable.ic_skip_next_white_36dp, R.drawable.ic_skip_previous_white_36dp
            , R.drawable.ic_add_to_playlist_white_36dp
            , R.drawable.ic_equalizer_white_36dp, R.drawable.ic_sleep_mode_white_36dp};

    private YPYMainActivity mContext;
    private ImageView mImgTrack;

    private TextView mTvSong;

    private TextView mTvSinger;
    private MaterialIconView mBtnPlay;
    private EqualizerView mEqualizer;
    private TrackModel mCurrentTrackObject;
    private ArrayList<TrackModel> mListSongs;
    private long mCurrentId;

    private CircularProgressBar mProgressBar;
    private TextView mTvCurrentTime;
    private TextView mTvDuration;
    private SliderView mSeekbar;

    private RelativeLayout mLayoutControl;
    private LinearLayout mLayoutContent;
    private ImageView mCbShuffe;
    private ImageView mCbRepeat;
    private GlideViewGroupTarget mTarget;


    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_listen_music, container, false);
    }

    @Override
    public void findView() {
        this.mContext = (YPYMainActivity) getActivity();
        this.mImgTrack = (ImageView) mRootView.findViewById(R.id.img_track);

        this.mTvSong = (TextView) mRootView.findViewById(R.id.tv_current_song);
        this.mTvSong.setTypeface(mContext.mTypefaceBold);

        this.mTvSinger = (TextView) mRootView.findViewById(R.id.tv_current_singer);
        this.mTvSinger.setTypeface(mContext.mTypefaceNormal);

        TextView mTvNowPlaying = (TextView) mRootView.findViewById(R.id.tv_now_playing);
        mTvNowPlaying.setTypeface(mContext.mTypefaceBold);

        mEqualizer = (EqualizerView) mRootView.findViewById(R.id.big_equalizer);

        int len = RES_ID_CLICKS.length;
        for (int i = 0; i < len; i++) {
            ImageView mImgView = (ImageView) mRootView.findViewById(RES_ID_CLICKS[i]);
            mImgView.setOnClickListener(this);
            mContext.setUpImageViewBaseOnColor(mImgView, mContext.mIconColor, RES_IMAGE[i], false);
        }

        FloatingActionButton mFloatingActionButton = (FloatingActionButton) mRootView.findViewById(R.id.fb_play);
        mFloatingActionButton.setColorNormal(mContext.getResources().getColor(R.color.colorAccent));
        mFloatingActionButton.setColorPressed(mContext.getResources().getColor(R.color.colorAccent));
        mFloatingActionButton.setColorRipple(getResources().getColor(R.color.main_color_divider));
        mFloatingActionButton.setOnClickListener(this);

        this.mProgressBar = (CircularProgressBar) mRootView.findViewById(R.id.progressBar1);
        this.mProgressBar.setVisibility(View.VISIBLE);

        this.mLayoutControl = (RelativeLayout) mRootView.findViewById(R.id.layout_control);
        this.mLayoutContent = (LinearLayout) mRootView.findViewById(R.id.layout_content);
        this.mBtnPlay = (MaterialIconView) mRootView.findViewById(R.id.btn_play);

        this.mTvCurrentTime = (TextView) mRootView.findViewById(R.id.tv_current_time);
        this.mTvCurrentTime.setTypeface(mContext.mTypefaceLight);

        this.mTvDuration = (TextView) mRootView.findViewById(R.id.tv_duration);
        this.mTvDuration.setTypeface(mContext.mTypefaceLight);

        mSeekbar = (SliderView) mRootView.findViewById(R.id.seekBar1);
        mSeekbar.setProcessColor(getResources().getColor(R.color.colorAccent));
        mSeekbar.setBackgroundColor(getResources().getColor(R.color.default_image_color));
        this.mSeekbar.setOnValueChangedListener(new SliderView.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                if (mCurrentTrackObject != null) {
                    int currentPos = (int) (value * mCurrentTrackObject.getDuration() / 100f);
                    mContext.onProcessSeekAudio(currentPos);
                }
            }
        });

        this.mCbShuffe = (ImageView) mRootView.findViewById(R.id.cb_shuffle);
        this.mCbShuffe.setOnClickListener(this);
        updateTypeShuffle();

        setUpBackground();

        this.mCbRepeat = (ImageView) mRootView.findViewById(R.id.cb_repeat);
        this.mCbRepeat.setOnClickListener(this);
        updateTypeRepeat();


        mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        mCurrentId = mCurrentTrackObject != null ? mCurrentTrackObject.getId() : 0;

        boolean isLoading = MusicDataMng.getInstance().isLoading();
        showLoading(isLoading);

        if (MusicDataMng.getInstance().isPlayingMusic()) {
            mEqualizer.animateBars();
        } else {
            mEqualizer.stopBars();
        }
        onPlayerUpdateState(MusicDataMng.getInstance().isPlayingMusic());

        updateInformation();


    }

    private void updateTypeShuffle() {
        if (mCbShuffe != null) {
            int color = getResources().getColor(YPYSettingManager.getShuffle(mContext) ? R.color.colorAccent : R.color.icon_color);
            mContext.setUpImageViewBaseOnColor(mCbShuffe, color, R.drawable.ic_shuffle_white_36dp, false);
        }
    }

    private void updateTypeRepeat() {
        if (mCbRepeat != null) {
            int type = YPYSettingManager.getNewRepeat(mContext);
            if (type == 0) {
                mContext.setUpImageViewBaseOnColor(mCbRepeat, mContext.mIconColor, R.drawable.ic_repeat_white_36dp, false);
            } else if (type == 1) {
                mContext.setUpImageViewBaseOnColor(mCbRepeat, mContext.getResources().getColor(R.color.colorAccent), R.drawable.ic_repeat_one_white_36dp, false);
            } else if (type == 2) {
                mContext.setUpImageViewBaseOnColor(mCbRepeat, mContext.getResources().getColor(R.color.colorAccent), R.drawable.ic_repeat_white_36dp, false);
            }
        }

    }

    public void setUpInfo(ArrayList<TrackModel> mListSongs) {
        if (this.mListSongs != null) {
            this.mListSongs.clear();
            this.mListSongs = null;
        }
        mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        if (mListSongs == null || mListSongs.size() == 0 || mCurrentTrackObject == null) {
            return;
        }
        this.mListSongs = (ArrayList<TrackModel>) mListSongs.clone();
        updateInformation();

    }


    public void updateInformation() {
        final TrackModel mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        if (mCurrentTrackObject != null) {
            this.mTvSong.setText(String.format(getString(R.string.format_current_song), mCurrentTrackObject.getTitle()));
            String artist = mCurrentTrackObject.getAuthor();
            if (!StringUtils.isEmptyString(artist) && !artist.equalsIgnoreCase(PREFIX_UNKNOWN)) {
                mTvSinger.setText(String.format(getString(R.string.format_current_singer), mCurrentTrackObject.getAuthor()));
            } else {
                mTvSinger.setText(String.format(getString(R.string.format_current_singer), mContext.getString(R.string.title_unknown)));
            }
            String artworkUrl = mCurrentTrackObject.getArtworkUrl();
            if (!TextUtils.isEmpty(artworkUrl)) {
                GlideImageLoader.displayImage(mContext, mImgTrack, artworkUrl, R.drawable.ic_disk);

            } else {
                Uri mUri = mCurrentTrackObject.getURI();
                if (mUri != null) {
                    GlideImageLoader.displayImageFromMediaStore(mContext, mImgTrack, mUri, R.drawable.ic_disk);
                } else {
                    mImgTrack.setImageResource(R.drawable.ic_disk);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEqualizer != null) {
            mEqualizer.stopBars();
        }
        if (mListSongs != null) {
            mListSongs.clear();
            mListSongs = null;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                mContext.startMusicService(ACTION_NEXT);
                break;
            case R.id.btn_prev:
                mContext.startMusicService(ACTION_PREVIOUS);
                break;
            case R.id.fb_play:
                onActionPlay();
                break;
            case R.id.cb_shuffle:
                boolean b = YPYSettingManager.getShuffle(mContext);
                YPYSettingManager.setShuffle(mContext, !b);
                updateTypeShuffle();
                break;
            case R.id.cb_repeat:
                int repeat = YPYSettingManager.getNewRepeat(mContext);
                repeat++;
                if (repeat > 2) {
                    repeat = 0;
                }
                YPYSettingManager.setNewRepeat(mContext, repeat);
                updateTypeRepeat();
                break;
            case R.id.img_add_playlist:
                TrackModel mCurrentTrack = MusicDataMng.getInstance().getCurrentTrackObject();
                if (mCurrentTrack != null) {
                    mContext.showDialogPlaylist(mCurrentTrack, new IYPYCallback() {
                        @Override
                        public void onAction() {
                            mContext.notifyData(TYPE_PLAYLIST);
                        }
                    });
                }
                break;
            case R.id.img_share:
                TrackModel mCurrentTrack1 = MusicDataMng.getInstance().getCurrentTrackObject();
                if (mCurrentTrack1 != null) {
                    mContext.shareFile(mCurrentTrack1);
                }
                break;
            case R.id.btn_close:
                mContext.collapseListenMusic();
                break;
            case R.id.img_equalizer:
                mContext.goToEqualizer();
                break;
            case R.id.img_sleep_mode:
                mContext.showDialogSleepMode();
                break;
            default:
                break;
        }
    }

    public void onActionPlay() {
        ArrayList<TrackModel> mListMusics = MusicDataMng.getInstance().getListPlayingTrackObjects();
        int size = mListMusics != null ? mListMusics.size() : 0;
        if (size > 0) {
            if (MusicDataMng.getInstance().isPrepaireDone()) {
                mContext.startMusicService(ACTION_TOGGLE_PLAYBACK);
                return;
            }
        }
        if (mListSongs != null && mListSongs.size() > 0) {
            MusicDataMng.getInstance().setListPlayingTrackObjects((ArrayList<TrackModel>) mListSongs.clone());
            for (TrackModel mTrackObject : mListSongs) {
                if (mTrackObject.getId() == mCurrentId) {
                    MusicDataMng.getInstance().setCurrentIndex(mTrackObject);
                    mContext.startMusicService(ACTION_PLAY);
                    return;
                }
            }
            MusicDataMng.getInstance().setCurrentIndex(mListSongs.get(0));
            mContext.startMusicService(ACTION_PLAY);
        }

    }

    public void showLoading(boolean isShow) {
        if (mLayoutContent != null) {
            mLayoutContent.setVisibility(isShow ? View.INVISIBLE : View.VISIBLE);
            mProgressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
            mEqualizer.stopBars();
            if (isShow) {
                updateInformation();
            }

        }
    }

    public void onUpdatePos(long currentPos) {
        if (currentPos > 0 && mCurrentTrackObject != null && mTvCurrentTime != null) {
            mTvCurrentTime.setText(mContext.getStringDuration(currentPos / 1000));
            int percent = (int) (((float) currentPos / (float) mCurrentTrackObject.getDuration()) * 100f);
            mSeekbar.setValue(percent);
        }
    }

    public void onPlayerStop() {
        if (mBtnPlay != null) {
            mBtnPlay.setText(Html.fromHtml(getString(R.string.icon_play)));
            mSeekbar.setValue(0);
            mTvCurrentTime.setText(mContext.getStringDuration(0));
            mTvDuration.setText(mContext.getStringDuration(0));

            mEqualizer.stopBars();
            setUpInfo(null);
        }

    }

    public void onPlayerUpdateState(boolean isPlay) {
        if (mBtnPlay != null) {
            mBtnPlay.setText(Html.fromHtml(getString(isPlay ? R.string.icon_pause : R.string.icon_play)));
            mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
            if (mCurrentTrackObject != null) {
                mCurrentId = mCurrentTrackObject.getId();
                if (mCurrentTrackObject != null) {
                    mTvDuration.setText(mContext.getStringDuration(mCurrentTrackObject.getDuration() / 1000));
                }
            }
            if (isPlay) {
                mEqualizer.animateBars();
            } else {
                mEqualizer.stopBars();
            }
        }

    }

//    private void updateBackground(){
//        TrackModel mTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
//        if(mTrackObject!=null && mDBBlurBackground!=null){
//            if(!mTrackObject.hasImageFromLibrary()){
//                String mUri = mTrackObject.getArtworkUrl();
//                if (!StringUtils.isEmptyString(mUri)) {
//                    if(!mUri.startsWith(PREFIX_HTTP)){
//                        mUri =PREFIX_FILE+mUri;
//                    }
//                    mDBBlurBackground.displayImage(mUri);
//
//                }
//                else {
//                    mDBBlurBackground.setImageResource(R.drawable.bg,true);
//                }
//            }
//            else{
//                Uri mUri = mTrackObject.getURI();
//                if (mUri!=null) {
//                    GlideImageLoader.displayImageFromMediaStore(mContext,mImageBackground,mUri,R.drawable.back);
//                    mDBBlurBackground.displayImageFromMediaStore(mUri);
//                }
//                else {
//                    mDBBlurBackground.setImageResource(R.drawable.bg,true);
//                }
//            }
//        }
//    }

    public void setUpBackground() {
        try {
            RelativeLayout mLayoutBg = (RelativeLayout) mRootView.findViewById(R.id.layout_listen_bg);
            if (mLayoutBg != null) {
                mTarget = new GlideViewGroupTarget(mContext, mLayoutBg) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                    }
                };
                String imgBg = YPYSettingManager.getBackground(mContext);
                Log.e("DCM", "=============>getBackground=" + imgBg);
                if (!TextUtils.isEmpty(imgBg)) {
                    Glide.with(this).load(Uri.parse(imgBg)).asBitmap()
                            .transform(mContext.mBlurBgTranform)
                            .placeholder(R.drawable.default_bg_app).into(mTarget);
                } else {
                    mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
