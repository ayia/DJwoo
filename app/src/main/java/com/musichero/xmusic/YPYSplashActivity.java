package com.musichero.xmusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.task.IYPYCallback;
import com.musichero.xmusic.utils.DBLog;
import com.musichero.xmusic.utils.DirectionUtils;
import com.musichero.xmusic.utils.IOUtils;
import com.musichero.xmusic.view.CircularProgressBar;

import java.io.File;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:MusicPlayer
 */
public class YPYSplashActivity extends YPYFragmentActivity {

    public static final String TAG = YPYSplashActivity.class.getSimpleName();
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1000;

    public static final int REQUEST_PERMISSION_CODE = 1001;
    public static final String REQUEST_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private Handler mHandler = new Handler();

    private boolean isLoading;

    private CircularProgressBar mProgressBar;
    private boolean isCheckGoogle;
    private GoogleApiAvailability googleAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash);

        this.mProgressBar = (CircularProgressBar) findViewById(R.id.progressBar1);

        TextView mTvLoading = (TextView) findViewById(R.id.tv_loading);
        mTvLoading.setTypeface(mTypefaceNormal);

        YPYSettingManager.setOnline(this, true);
        DBLog.setDebug(DEBUG);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCheckGoogle) {
            isCheckGoogle = true;
            checkGooglePlayService();
        }
    }


    private void startLoad() {
        File mFile = mTotalMng.getDirectoryCached();
        if (mFile == null) {
            createFullDialog(-1, R.string.title_info, R.string.title_settings, R.string.title_cancel,
                    getString(R.string.info_error_sdcard), new IYPYCallback() {
                        @Override
                        public void onAction() {
                            isCheckGoogle = false;
                            startActivityForResult(new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS), 0);
                        }
                    }, new IYPYCallback() {
                        @Override
                        public void onAction() {
                            onDestroyData();
                            finish();
                        }
                    }).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);

        boolean b = isNeedGrantPermission();
        if (!b) {
            startExecuteTask();
        }

    }

    private void startExecuteTask() {
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                mTotalMng.readConfigure(YPYSplashActivity.this);
                mTotalMng.readGenreData(YPYSplashActivity.this);
                mTotalMng.readCached(TYPE_FILTER_SAVED);
                mTotalMng.readPlaylistCached();
                mTotalMng.readLibraryTrack(YPYSplashActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goToMainActivity();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void goToMainActivity() {
        showInterstitial(new IYPYCallback() {
            @Override
            public void onAction() {
                mProgressBar.setVisibility(View.INVISIBLE);
                Intent mIntent = new Intent(YPYSplashActivity.this, YPYMainActivity.class);
                DirectionUtils.changeActivity(YPYSplashActivity.this, R.anim.slide_in_from_right, R.anim.slide_out_to_left, true, mIntent);

            }
        });
    }


    private void startInit() {
        if (!isLoading) {
            isLoading = true;
            mProgressBar.setVisibility(View.VISIBLE);
            startLoad();
        }
    }

    private void checkGooglePlayService() {
        googleAPI = GoogleApiAvailability.getInstance();
        try {
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            if (result == ConnectionResult.SUCCESS) {
                startInit();
            } else {
                if (googleAPI.isUserResolvableError(result)) {
                    isCheckGoogle = false;
                    googleAPI.showErrorDialogFragment(this, result, REQUEST_GOOGLE_PLAY_SERVICES);
                } else {
                    showToast(googleAPI.getErrorString(result));
                    startInit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isNeedGrantPermission() {
        try {
            if (IOUtils.hasMarsallow()) {
                if (ContextCompat.checkSelfPermission(this, REQUEST_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUEST_PERMISSION)) {
                        String msg = String.format(getString(R.string.format_request_permision), getString(R.string.app_name));
                        showFullDialog(R.string.title_confirm, msg, R.string.title_grant, R.string.title_cancel, new IYPYCallback() {
                            @Override
                            public void onAction() {
                                ActivityCompat.requestPermissions(YPYSplashActivity.this, new String[]{REQUEST_PERMISSION}, REQUEST_PERMISSION_CODE);
                            }
                        }, new IYPYCallback() {
                            @Override
                            public void onAction() {
                                onDestroyData();
                                finish();
                            }
                        });
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{REQUEST_PERMISSION}, REQUEST_PERMISSION_CODE);
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUEST_PERMISSION_CODE) {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startExecuteTask();
                } else {
                    showToast(R.string.info_permission_denied);
                    onDestroyData();
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.info_permission_denied);
            onDestroyData();
            finish();
        }

    }


}
