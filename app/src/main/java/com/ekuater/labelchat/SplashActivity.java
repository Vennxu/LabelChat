package com.ekuater.labelchat;

import android.os.Bundle;
import android.os.Handler;

import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.CoreServiceStarter;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class SplashActivity extends TitleIconActivity {

    private static final long LAUNCH_TIMEOUT = 8 * 1000; // 8 seconds

    private SettingHelper mSettingHelper;
    private AccountManager mAccountManager;
    private Handler mHandler;
    private final Runnable mLaunchRunnable = new Runnable() {
        @Override
        public void run() {
            autoLoginOrRegister();
        }
    };
    private final CoreServiceStarter.OnStartListener mStartListener
            = new CoreServiceStarter.OnStartListener() {
        @Override
        public void onStarted() {
            mHandler.post(mLaunchRunnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For Umeng update
        UmengUpdateAgent.setDefault();
        UmengUpdateAgent.update(this);
        MobclickAgent.updateOnlineConfig(this);

        mSettingHelper = SettingHelper.getInstance(this);
        mAccountManager = AccountManager.getInstance(this);
        mHandler = new Handler();
        if (!(mAccountManager.available() && (mAccountManager.isLogin()
                || isAutoLogin() && !mSettingHelper.isPrevCurrVersionSame()))) {
            setContentView(R.layout.activity_splash);
        }
        mSettingHelper.setManualExitApp(false);
        initialize();
    }

    private void initialize() {
        CoreServiceStarter.start(this, mStartListener);
        mHandler.postDelayed(mLaunchRunnable, LAUNCH_TIMEOUT);
    }

    private synchronized void autoLoginOrRegister() {
        mHandler.removeCallbacks(mLaunchRunnable);
        goMainUI();
    }

    private boolean isAutoLogin() {
        return mAccountManager.isAutoLogin();
    }

    private void goMainUI() {
        UILauncher.launchMainUI(this);
        finish();
    }
}
