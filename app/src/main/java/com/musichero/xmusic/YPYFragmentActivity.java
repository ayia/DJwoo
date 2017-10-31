package com.musichero.xmusic;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.musichero.xmusic.abtractclass.fragment.DBFragment;
import com.musichero.xmusic.abtractclass.fragment.IDBFragmentConstants;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.dataMng.MusicDataMng;
import com.musichero.xmusic.dataMng.TotalDataManager;
import com.musichero.xmusic.imageloader.target.GlideViewGroupTarget;
import com.musichero.xmusic.listener.IDBMusicPlayerListener;
import com.musichero.xmusic.listener.IDBSearchViewInterface;
import com.musichero.xmusic.model.PlaylistModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.playservice.IYPYMusicConstant;
import com.musichero.xmusic.playservice.YPYMusicService;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.task.IYPYCallback;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.DBLog;
import com.musichero.xmusic.utils.IOUtils;
import com.musichero.xmusic.utils.ResolutionUtils;
import com.musichero.xmusic.utils.ShareActionUtils;
import com.musichero.xmusic.utils.StringUtils;
import com.musichero.xmusic.view.DividerItemDecoration;
import com.triggertrap.seekarc.SeekArc;

import java.io.File;
import java.util.ArrayList;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class YPYFragmentActivity extends AppCompatActivity implements IXMusicConstants, IYPYMusicConstant, IDBFragmentConstants {

    public static final String TAG = YPYFragmentActivity.class.getSimpleName();
    public ArrayList<Fragment> mListFragments;
    public Typeface mTypefaceNormal;
    public Typeface mTypefaceLight;
    public Typeface mTypefaceBold;
    public Typeface mTypefaceItalic;
    public TotalDataManager mTotalMng;
    public SearchView searchView;
    //process favorite, playlist
    public boolean isNeedProcessOther;
    public BlurTransformation mBlurBgTranform;
    public Drawable mBackDrawable;
    public int mContentActionColor;
    public int mIconColor;
    private Dialog mProgressDialog;
    private int screenWidth;
    private int screenHeight;
    private boolean isAllowPressMoreToExit;
    private int countToExit;
    private long pivotTime;
    private IDBMusicPlayerListener musicPlayerListener;
    private MusicPlayerBroadcast mPlayerBroadcast;
    private ConnectionChangeReceiver mNetworkBroadcast;
    private INetworkListener mNetworkListener;
    private RelativeLayout mLayoutAds;
    private String[] mListStr;
    private boolean isLoadingBg;
    private GlideViewGroupTarget mTarget;
    private boolean isPausing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFormat(PixelFormat.RGBA_8888);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        this.createProgressDialog();

        this.mTypefaceNormal = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        this.mTypefaceLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        this.mTypefaceBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        this.mTypefaceItalic = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Italic.ttf");

        this.mTotalMng = TotalDataManager.getInstance();
        setStatusBarTranslucent(true);

        this.mBlurBgTranform = new BlurTransformation(this);

        this.mContentActionColor = getResources().getColor(R.color.icon_action_bar_color);
        this.mIconColor = getResources().getColor(R.color.icon_color);

        this.mBackDrawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        this.mBackDrawable.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);

        int[] mRes = ResolutionUtils.getDeviceResolution(this);
        if (mRes != null && mRes.length == 2) {
            screenWidth = mRes[0];
            screenHeight = mRes[1];
        }
    }

    public void setStatusBarTranslucent(boolean makeTranslucent) {
        if (IOUtils.hasKitKat()) {
            if (makeTranslucent) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

    }

    public void setUpImageViewBaseOnColor(int id, int color, int idDrawabe, boolean isReset) {
        setUpImageViewBaseOnColor(findViewById(id), color, idDrawabe, isReset);
    }

    public void setUpImageViewBaseOnColor(View mView, int color, int idDrawabe, boolean isReset) {
        Drawable mDrawable = getResources().getDrawable(idDrawabe);
        if (isReset) {
            mDrawable.clearColorFilter();
        } else {
            mDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        if (mView instanceof Button) {
            mView.setBackgroundDrawable(mDrawable);
        } else if (mView instanceof ImageView) {
            ((ImageView) mView).setImageDrawable(mDrawable);
        } else if (mView instanceof ImageButton) {
            ((ImageView) mView).setImageDrawable(mDrawable);
        }
    }

    public void setUpBackground() {
        try {
            RelativeLayout mLayoutBg = (RelativeLayout) findViewById(R.id.layout_bg);
            if (mLayoutBg != null) {
                mTarget = new GlideViewGroupTarget(this, mLayoutBg) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                    }
                };
                String imgBg = YPYSettingManager.getBackground(this);
                Log.e("DCM", "=============>getBackground=" + imgBg);
                if (!TextUtils.isEmpty(imgBg)) {
                    Glide.with(this).load(Uri.parse(imgBg)).asBitmap()
                            .transform(mBlurBgTranform)
                            .placeholder(R.drawable.default_bg_app).into(mTarget);
                } else {
                    if (this instanceof YPYSplashActivity) {
                        mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPausing = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPausing || !isLoadingBg) {
            isPausing = false;
            isLoadingBg = true;
            setUpBackground();
        }
    }


    public void showAppRate() {
        if (!YPYSettingManager.getRateApp(this)) {
            AppRate.with(this).setInstallDays(NUMBER_INSTALL_DAYS) // default 10, 0 means install day.
                    .setLaunchTimes(NUMBER_LAUNCH_TIMES) // default 10
                    .setRemindInterval(REMIND_TIME_INTERVAL) // default 1
                    .setShowLaterButton(true) // default true
                    .setShowNeverButton(false) // default true
                    .setDebug(false).setOnClickButtonListener(new OnClickButtonListener() {
                @Override
                public void onClickButton(int which) {
                    if (which == -1) {
                        YPYSettingManager.setRateApp(YPYFragmentActivity.this, true);
                        ShareActionUtils.goToUrl(YPYFragmentActivity.this, String.format(URL_FORMAT_LINK_APP, getPackageName()));
                    }
                }
            }).monitor();

            AppRate.showRateDialogIfMeetsConditions(this);
        }

    }

    public void setUpLayoutAdmob() {
        mLayoutAds = (RelativeLayout) findViewById(R.id.layout_ads);
        boolean b = SHOW_ADS;
        if (b) {
            if (ApplicationUtils.isOnline(this) && mLayoutAds != null) {
                AdView adView = new AdView(this);
                adView.setAdUnitId(ADMOB_BANNER_ID);
                adView.setAdSize(AdSize.SMART_BANNER);
                mLayoutAds.addView(adView);
                AdRequest mAdRequest = new AdRequest.Builder().addTestDevice(TEST_DEVICE).build();
                adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        DBLog.d(TAG, "===========>Add loaded");
                        showAds();

                    }
                });
                adView.loadAd(mAdRequest);
                hideAds();
                return;

            }
        }
        hideAds();

    }

    public void showInterstitial(final IYPYCallback mCallback) {
        boolean b = SHOW_ADS;
        if (ApplicationUtils.isOnline(this) && b) {
            final InterstitialAd admob = new InterstitialAd(getApplicationContext());
            admob.setAdUnitId(ADMOB_INTERSTITIAL_ID);
            AdRequest adRequestPlayer = new AdRequest.Builder().addTestDevice(TEST_DEVICE).build();
            admob.loadAd(adRequestPlayer);
            admob.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (admob != null) {
                        admob.show();
                    }
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }
            });
            return;

        }
        if (mCallback != null) {
            mCallback.onAction();
        }

    }

    public void showAds() {
        mLayoutAds.setVisibility(View.VISIBLE);
    }

    public void hideAds() {
        mLayoutAds.setVisibility(View.GONE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerBroadcast != null) {
            unregisterReceiver(mPlayerBroadcast);
            mPlayerBroadcast = null;
        }
        if (mNetworkBroadcast != null) {
            unregisterReceiver(mNetworkBroadcast);
            mNetworkBroadcast = null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isAllowPressMoreToExit) {
                showQuitDialog();
            } else {
                pressMoreToExitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setIsAllowPressMoreToExit(boolean isAllowPressMoreToExit) {
        this.isAllowPressMoreToExit = isAllowPressMoreToExit;
    }

    private void pressMoreToExitApp() {
        if (countToExit >= 1) {
            long delaTime = System.currentTimeMillis() - pivotTime;
            if (delaTime <= 2000) {
                onDestroyData();
                finish();
                return;
            } else {
                countToExit = 0;
            }
        }
        pivotTime = System.currentTimeMillis();
        showToast(R.string.info_press_again_to_exit);
        countToExit++;
    }

    public MaterialDialog createFullDialog(int iconId, int mTitleId, int mYesId, int mNoId, String messageId, final IYPYCallback mCallback, final IYPYCallback mNeCallback) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title(mTitleId);
        if (iconId != -1) {
            mBuilder.iconRes(iconId);
        }
        mBuilder.content(messageId);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_text));
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.negativeColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.negativeText(mNoId);
        mBuilder.positiveText(mYesId);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceLight);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (mCallback != null) {
                    mCallback.onAction();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                if (mNeCallback != null) {
                    mNeCallback.onAction();
                }
            }
        });
        return mBuilder.build();
    }


    public void showFullDialog(int titleId, String message, int idPositive, int idNegative, final IYPYCallback mDBCallback) {
        createFullDialog(-1, titleId, idPositive, idNegative, message, mDBCallback, null).show();
    }

    public void showFullDialog(int titleId, String message, int idPositive, int idNegative, final IYPYCallback mDBCallback, final IYPYCallback mNegative) {
        createFullDialog(-1, titleId, idPositive, idNegative, message, mDBCallback, mNegative).show();
    }

    public void showQuitDialog() {
        int mNoId = R.string.title_no;
        int mTitleId = R.string.title_confirm;
        int mYesId = R.string.title_yes;
        int iconId = R.drawable.ic_launcher;
        int messageId = R.string.info_close_app;

        createFullDialog(iconId, mTitleId, mYesId, mNoId, getString(messageId), new IYPYCallback() {
            @Override
            public void onAction() {
                onDestroyData();
                finish();
            }
        }, new IYPYCallback() {

            @Override
            public void onAction() {
            }
        }).show();

    }

    private void createProgressDialog() {
        this.mProgressDialog = new Dialog(this);
        this.mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mProgressDialog.setContentView(R.layout.item_progress_bar);
        TextView mTvMessage = (TextView) mProgressDialog.findViewById(R.id.tv_message);
        mTvMessage.setTypeface(mTypefaceLight);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
    }

    public void showProgressDialog() {
        showProgressDialog(R.string.info_loading);
    }

    public void showProgressDialog(int messageId) {
        showProgressDialog(getString(messageId));
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog != null) {
            TextView mTvMessage = (TextView) mProgressDialog.findViewById(R.id.tv_message);
            mTvMessage.setText(message);
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    public void dimissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showToastWithLongTime(int resId) {
        showToastWithLongTime(getString(resId));
    }

    public void showToastWithLongTime(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public void onDestroyData() {
        YPYSettingManager.setOnline(this, false);
        mTotalMng.onDestroy();
    }

    public void createArrayFragment() {
        mListFragments = new ArrayList<>();
    }

    public void addFragment(Fragment mFragment) {
        if (mFragment != null && mListFragments != null) {
            synchronized (mListFragments) {
                mListFragments.add(mFragment);
            }
        }
    }

    public Fragment getFragmentHome(String nameFragment, int idFragment) {
        Fragment mFragmentHome = null;
        if (idFragment > 0) {
            mFragmentHome = getSupportFragmentManager().findFragmentById(idFragment);
        } else {
            if (!StringUtils.isEmptyString(nameFragment)) {
                mFragmentHome = getSupportFragmentManager().findFragmentByTag(nameFragment);
            }
        }
        return mFragmentHome;
    }

    public String getStringDuration(long duration) {
        String minute = String.valueOf((int) (duration / 60));
        String seconds = String.valueOf((int) (duration % 60));
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        if (seconds.length() < 2) {
            seconds = "0" + seconds;
        }
        return minute + ":" + seconds;
    }

    public boolean backStack(IYPYCallback mCallback) {
        if (mListFragments != null && mListFragments.size() > 0) {
            int count = mListFragments.size();
            if (count > 0) {
                synchronized (mListFragments) {
                    Fragment mFragment = mListFragments.remove(count - 1);
                    if (mFragment != null) {
                        if (mFragment instanceof DBFragment) {
                            ((DBFragment) mFragment).backToHome(this);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public void goToFragment(String tag, int idContainer, String fragmentName, String parentTag, Bundle mBundle) {
        goToFragment(tag, idContainer, fragmentName, 0, parentTag, mBundle);
    }

    public void goToFragment(String tag, int idContainer, String fragmentName, int parentId, Bundle mBundle) {
        goToFragment(tag, idContainer, fragmentName, parentId, null, mBundle);
    }

    public void goToFragment(String tag, int idContainer, String fragmentName, int parentId, String parentTag, Bundle mBundle) {
        if (!StringUtils.isEmptyString(tag) && getSupportFragmentManager().findFragmentByTag(tag) != null) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mBundle != null) {
            if (parentId != 0) {
                mBundle.putInt(KEY_ID_FRAGMENT, parentId);
            }
            if (!StringUtils.isEmptyString(parentTag)) {
                mBundle.putString(KEY_NAME_FRAGMENT, parentTag);
            }
        }
        Fragment mFragment = Fragment.instantiate(this, fragmentName, mBundle);
        addFragment(mFragment);
        transaction.add(idContainer, mFragment, tag);
        if (parentId != 0) {
            Fragment mFragmentParent = getSupportFragmentManager().findFragmentById(parentId);
            if (mFragmentParent != null) {
                transaction.hide(mFragmentParent);
            }
        }
        if (!StringUtils.isEmptyString(parentTag)) {
            Fragment mFragmentParent = getSupportFragmentManager().findFragmentByTag(parentTag);
            if (mFragmentParent != null) {
                transaction.hide(mFragmentParent);
            }
        }
        transaction.commit();
    }

    public void showDialogPlaylist(final TrackModel mTrackObject, final IYPYCallback mCallback) {
        final ArrayList<PlaylistModel> mListPlaylist = mTotalMng.getListPlaylistObjects();
        if (mListPlaylist != null && mListPlaylist.size() > 0) {
            int size = mListPlaylist.size() + 1;
            mListStr = new String[size];
            mListStr[0] = getResources().getStringArray(R.array.list_create_playlist)[0];
            for (int i = 1; i < size; i++) {
                PlaylistModel mPlaylistObject = mListPlaylist.get(i - 1);
                mListStr[i] = mPlaylistObject.getName();
            }
        } else {
            mListStr = getResources().getStringArray(R.array.list_create_playlist);
        }
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_select_playlist);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.items(mListStr);
        mBuilder.itemColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(R.string.title_cancel);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                mListStr = null;
            }
        });
        mBuilder.itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                if (mListPlaylist != null && mListPlaylist.size() > 0 && which > 0) {
                    mTotalMng.addTrackToPlaylist(YPYFragmentActivity.this, mTrackObject,
                            mListPlaylist.get(which - 1), true, mCallback);
                } else {
                    showDialogCreatePlaylist(false, null, new IYPYCallback() {
                        @Override
                        public void onAction() {
                            final ArrayList<PlaylistModel> mListPlaylist = mTotalMng.getListPlaylistObjects();
                            mTotalMng.addTrackToPlaylist(YPYFragmentActivity.this, mTrackObject, mListPlaylist.get(mListPlaylist.size() - 1), true, mCallback);
                            if (isNeedProcessOther) {
                                notifyData(TYPE_PLAYLIST);
                            } else {
                                sendBroadcastPlayer(ACTION_PLAYLIST);
                            }

                        }
                    });
                }
                mListStr = null;
            }
        });
        mBuilder.build().show();
    }

    public void notifyData(int type) {

    }

    public void notifyData(int type, long value) {

    }

    public void notifyFragment() {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof DBFragment) {
                    ((DBFragment) mFragment).notifyData();
                }
            }
        }
    }

    public void justNotifyFragment() {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof DBFragment) {
                    ((DBFragment) mFragment).justNotifyData();
                }
            }
        }
    }


    public void showDialogCreatePlaylist(final boolean isEdit, final PlaylistModel mPlaylistObject, final IYPYCallback mCallback) {
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText mEdPlaylistName = (EditText) mView.findViewById(R.id.ed_name);
        mEdPlaylistName.setTextColor(getResources().getColor(R.color.main_color_text));
        mEdPlaylistName.setHighlightColor(getResources().getColor(R.color.main_color_secondary_text));
        mEdPlaylistName.setHint(R.string.title_playlist_name);
        if (isEdit) {
            mEdPlaylistName.setText(mPlaylistObject.getName());
        }
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_playlist_name);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_text));
        mBuilder.customView(mView, false);
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(isEdit ? R.string.title_save : R.string.title_create);
        mBuilder.negativeText(R.string.title_cancel);
        mBuilder.negativeColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                ApplicationUtils.hiddenVirtualKeyboard(YPYFragmentActivity.this, mEdPlaylistName);
                String mPlaylistName = mEdPlaylistName.getText().toString();
                checkCreatePlaylist(isEdit, mPlaylistObject, mPlaylistName, mCallback);
            }
        });
        final MaterialDialog mDialog = mBuilder.build();
        mEdPlaylistName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String mPlaylistName = mEdPlaylistName.getText().toString();
                    checkCreatePlaylist(isEdit, mPlaylistObject, mPlaylistName, mCallback);
                    mDialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mDialog.show();
    }

    private void checkCreatePlaylist(boolean isEdit, PlaylistModel mPlaylistObject, String mPlaylistName, IYPYCallback mCallback) {
        if (StringUtils.isEmptyString(mPlaylistName)) {
            showToast(R.string.info_playlist_error);
            return;
        }
        if (mTotalMng.isPlaylistNameExisted(mPlaylistName)) {
            showToast(R.string.info_playlist_name_existed);
            return;
        }
        if (!isEdit) {
            mPlaylistObject = new PlaylistModel(System.currentTimeMillis(), mPlaylistName);
            mPlaylistObject.setListTrackObjects(new ArrayList<TrackModel>());
            mTotalMng.addPlaylistObject(mPlaylistObject);
        } else {
            mTotalMng.editPlaylistObject(mPlaylistObject, mPlaylistName);
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    public void shareFile(TrackModel mTrackObject) {
        String path = mTrackObject.getPath();
        if (TextUtils.isEmpty(path)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mTrackObject.getTitle() + "\n" + mTrackObject.getPermalinkUrl());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share Via"));
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mTrackObject.getPath())));
            startActivity(Intent.createChooser(shareIntent, "Share Via"));
        }
    }

    public void onProcessSeekAudio(int currentPos) {
        startMusicService(ACTION_SEEK, currentPos);
    }

    public void startMusicService(String action) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(PREFIX_ACTION + action);
        startService(mIntent1);
    }

    public void startMusicService(String action, boolean data) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(PREFIX_ACTION + action);
        mIntent1.putExtra(KEY_VALUE, data);
        startService(mIntent1);
    }

    public void startMusicService(String action, int data) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(PREFIX_ACTION + action);
        mIntent1.putExtra(KEY_VALUE, data);
        startService(mIntent1);
    }

    public void registerMusicPlayerBroadCastReceiver(IDBMusicPlayerListener musicPlayerListener) {
        if (mPlayerBroadcast != null) {
            return;
        }
        this.musicPlayerListener = musicPlayerListener;
        mPlayerBroadcast = new MusicPlayerBroadcast();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(PREFIX_ACTION + ACTION_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcast, mIntentFilter);
    }

    public void sendBroadcastPlayer(String action) {
        Intent mIntent = new Intent(PREFIX_ACTION + ACTION_BROADCAST_PLAYER);
        mIntent.putExtra(KEY_ACTION, PREFIX_ACTION + action);
        sendBroadcast(mIntent);
    }

    public void sendBroadcastPlayer(String action, int type) {
        Intent mIntent = new Intent(PREFIX_ACTION + ACTION_BROADCAST_PLAYER);
        mIntent.putExtra(KEY_ACTION, PREFIX_ACTION + action);
        mIntent.putExtra(KEY_TYPE, type);
        sendBroadcast(mIntent);
    }

    public void showPopupMenu(View v, final TrackModel mTrackObject) {
        showPopupMenu(v, mTrackObject, null);
    }

    public void showPopupMenu(View v, final TrackModel mTrackObject, final PlaylistModel mPlaylistObject) {
        boolean isOffline = mTotalMng.isLibraryTracks(mTrackObject);
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_track, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_playlist:
                        showDialogPlaylist(mTrackObject, new IYPYCallback() {
                            @Override
                            public void onAction() {
                                notifyData(TYPE_PLAYLIST);
                            }
                        });
                        break;
                    case R.id.action_remove_from_playlist:
                        mTotalMng.removeTrackToPlaylist(mTrackObject, mPlaylistObject, new IYPYCallback() {
                            @Override
                            public void onAction() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifyData(TYPE_PLAYLIST);
                                    }
                                });

                            }
                        });
                        break;
                    case R.id.action_delete_song:
                        showDialogDelete(mTrackObject);
                        break;
                    case R.id.action_share:
                        shareFile(mTrackObject);
                        break;
                }
                return true;
            }
        });
        if (!isOffline) {
            popupMenu.getMenu().findItem(R.id.action_delete_song).setVisible(false);
        }

        if (mPlaylistObject == null) {
            popupMenu.getMenu().findItem(R.id.action_remove_from_playlist).setVisible(false);
        } else {
            popupMenu.getMenu().findItem(R.id.action_add_playlist).setVisible(false);
        }
        popupMenu.show();
    }

    public void showDialogDelete(final TrackModel mTrackObject) {
        showFullDialog(R.string.title_confirm, getString(R.string.info_delete_song), R.string.title_ok, R.string.title_cancel, new IYPYCallback() {
            @Override
            public void onAction() {
                showProgressDialog();
                mTotalMng.deleteSong(mTrackObject, new IYPYCallback() {
                    @Override
                    public void onAction() {
                        showToast(R.string.info_delete_song_done);
                        dimissProgressDialog();
                        notifyData(TYPE_DELETE, mTrackObject.getId());
                    }
                });
            }
        });
    }

    public void registerNetworkBroadcastReceiver(INetworkListener networkListener) {
        if (mNetworkBroadcast != null) {
            return;
        }
        mNetworkBroadcast = new ConnectionChangeReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkBroadcast, mIntentFilter);
        mNetworkListener = networkListener;
    }

    public void goToUrl(String name, String url) {
        Intent mIntent = new Intent(this, YPYShowUrlActivity.class);
        mIntent.putExtra(KEY_HEADER, name);
        mIntent.putExtra(KEY_SHOW_URL, url);
        startActivity(mIntent);
    }

    public void setTypefaceForTab(TabLayout mTabLayout, Typeface sMaterialDesignIcons) {
        try {
            ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof AppCompatTextView || tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTypeface(sMaterialDesignIcons);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setActionBarTitle(String title) {
        ActionBar mAb = getSupportActionBar();
        if (mAb != null) {
            mAb.setTitle(title);
        }
    }

    public void setActionBarTitle(int titleId) {
        setActionBarTitle(getResources().getString(titleId));
    }

    public void setUpCustomizeActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setTitleTextColor(mContentActionColor);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert_white_24dp);
            drawable.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setOverflowIcon(drawable);
        }
    }

    public void setUpEvalationActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(getResources().getDimensionPixelOffset(R.dimen.card_elevation));
        }
    }

    public void removeEvalationActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(0);
        }
    }

    public void setColorForActionBar(int color) {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    public void initSetupForSearchView(Menu menu, int idSearch, final IDBSearchViewInterface mListener) {
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(idSearch));
        ImageView searchBtn = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        setUpImageViewBaseOnColor(searchBtn, mContentActionColor, R.drawable.ic_search_white_24dp, false);

        ImageView closeBtn = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        setUpImageViewBaseOnColor(closeBtn, mContentActionColor, R.drawable.ic_close_white_24dp, false);

        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(mContentActionColor);
        searchEditText.setHintTextColor(mContentActionColor);

        try {
            ImageView searchSubmit = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_go_btn);
            if (searchSubmit != null) {
                searchSubmit.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                hiddenKeyBoardForSearchView();
                if (mListener != null) {
                    mListener.onProcessSearchData(keyword);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                if (mListener != null) {
                    mListener.onStartSuggestion(keyword);
                }
                return true;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickSearchView();
                }
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (mListener != null) {
                    mListener.onCloseSearchView();
                }
                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.title_search_music));
        searchView.setSubmitButtonEnabled(true);
    }

    public void hiddenKeyBoardForSearchView() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
            ApplicationUtils.hiddenVirtualKeyboard(this, searchView);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            hiddenKeyBoardForSearchView();
        } else {
            super.onBackPressed();
        }

    }

    public String getCurrentFragmentTag() {
        if (mListFragments != null && mListFragments.size() > 0) {
            Fragment mFragment = mListFragments.get(0);
            if (mFragment instanceof DBFragment) {
                return mFragment.getTag();
            }
        }
        return null;
    }

    public void showDialogSleepMode() {
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_time, null);
        final TextView mTvInfo = (TextView) mView.findViewById(R.id.tv_info);
        mTvInfo.setTypeface(mTypefaceNormal);
        if (YPYSettingManager.getSleepMode(this) > 0) {
            mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(YPYSettingManager.getSleepMode(YPYFragmentActivity.this))));
        } else {
            mTvInfo.setText(R.string.title_off);
        }

        SeekArc mCircularVir = (SeekArc) mView.findViewById(R.id.seek_sleep);
        mCircularVir.setProgressColor(getResources().getColor(R.color.colorAccent));
        mCircularVir.setArcColor(getResources().getColor(R.color.main_color_secondary_text));
        mCircularVir.setMax((MAX_SLEEP_MODE - MIN_SLEEP_MODE) / STEP_SLEEP_MODE + 1);
        mCircularVir.setProgressWidth(getResources().getDimensionPixelOffset(R.dimen.tiny_margin));
        mCircularVir.setProgress(YPYSettingManager.getSleepMode(this) / STEP_SLEEP_MODE);
        mCircularVir.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                try {
                    YPYSettingManager.setSleepMode(YPYFragmentActivity.this, progress * STEP_SLEEP_MODE);
                    if (progress == 0) {
                        mTvInfo.setText(R.string.title_off);
                    } else {
                        mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(YPYSettingManager.getSleepMode(YPYFragmentActivity.this))));
                    }
                    ArrayList<TrackModel> mListSongs = MusicDataMng.getInstance().getListPlayingTrackObjects();
                    if (mListSongs != null && mListSongs.size() > 0) {
                        startMusicService(ACTION_UPDATE_SLEEP_MODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_sleep_mode);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.customView(mView, false);
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(R.string.title_done);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
            }
        });
        final MaterialDialog mDialog = mBuilder.build();
        mDialog.show();
    }

    public void goToEqualizer() {
        Intent mIntent = new Intent(this, YPYEqualizerActivity.class);
        startActivity(mIntent);
    }

    public void setUpRecyclerViewAsListView(RecyclerView mListViewTrack, Drawable mDivider) {
        if (mDivider != null) {
            mListViewTrack.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, mDivider));
        }
        mListViewTrack.setHasFixedSize(true);
        LinearLayoutManager mLayoutMngList = new LinearLayoutManager(this);
        mLayoutMngList.setOrientation(LinearLayoutManager.VERTICAL);
        mListViewTrack.setLayoutManager(mLayoutMngList);
    }

    public void setUpRecyclerViewAsGridView(RecyclerView mGridView, int numberColumn) {
        mGridView.setHasFixedSize(false);
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberColumn);
        mGridView.setLayoutManager(layoutManager);

        DividerItemDecoration mItemDecorVerti = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, this.getResources().getDrawable(R.drawable.alpha_divider_verti));
        mGridView.addItemDecoration(mItemDecorVerti);
    }

    public interface INetworkListener {
        void onNetworkState(boolean isNetworkOn);
    }

    private class MusicPlayerBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    if (!StringUtils.isEmptyString(action)) {
                        String packageName = PREFIX_ACTION;
                        if (action.equals(packageName + ACTION_BROADCAST_PLAYER)) {
                            String actionPlay = intent.getStringExtra(KEY_ACTION);
                            if (!StringUtils.isEmptyString(actionPlay)) {
                                if (actionPlay.equals(packageName + ACTION_NEXT)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(false);
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_LOADING)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerLoading();
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_DIMISS_LOADING)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerStopLoading();
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_ERROR)) {
                                    showToast(R.string.info_play_song_error);
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerError();
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_PAUSE)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(false);
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_STOP)) {
                                    MusicDataMng.getInstance().onResetData();
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerStop();
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_PLAY)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(true);
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_UPDATE_POS)) {
                                    int currentPos = intent.getIntExtra(KEY_VALUE, -1);
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdatePos(currentPos);
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_UPDATE_STATUS)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateStatus();
                                    }
                                } else if (actionPlay.equals(packageName + ACTION_FAVORITE) && isNeedProcessOther) {
                                    int type = intent.getIntExtra(KEY_TYPE, -1);
                                    notifyData(type);
                                } else if (actionPlay.equals(packageName + ACTION_PLAYLIST) && isNeedProcessOther) {
                                    notifyData(TYPE_PLAYLIST);
                                } else if (actionPlay.equals(packageName + ACTION_DELETE_SONG) && isNeedProcessOther) {
                                    long idSong = intent.getLongExtra(KEY_SONG_ID, -1);
                                    notifyData(TYPE_DELETE, idSong);
                                }
                            }
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNetworkListener != null) {
                mNetworkListener.onNetworkState(ApplicationUtils.isOnline(YPYFragmentActivity.this));
            }
        }
    }


}
