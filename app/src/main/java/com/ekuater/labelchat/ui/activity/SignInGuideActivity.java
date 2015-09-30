package com.ekuater.labelchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.ui.OAuthLogin;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class SignInGuideActivity extends TitleIconActivity implements View.OnClickListener {

    private static final String LOGIN_DIALOG_TAG = "LoginDialog";

    private OAuthLogin mOAuthLoginManager;
    private boolean mInOAuthLogin = false;
    private SimpleProgressDialog mLoginDialog;

    private OAuthLogin.LoginListener mOAuthLoginListener
            = new OAuthLogin.LoginListener() {

        @Override
        public void onOAuthVerifyResult(boolean success) {
            handleOAuthVerifyResult(success);
        }

        @Override
        public void onGetOAuthInfoResult(boolean success) {
            handleGetOAuthInfoResult(success);
        }

        @Override
        public void onOAuthLoginResult(int result) {
            handleOAuthLoginResult(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_guide);

        mOAuthLoginManager = new OAuthLogin(this, getMainLooper(), mOAuthLoginListener);
        mInOAuthLogin = false;

        Button registerBtn = (Button) findViewById(R.id.btn_register);
        Button loginBtn = (Button) findViewById(R.id.btn_login);
        View qqView = findViewById(R.id.login_qq);
        View wxView = findViewById(R.id.login_wx);
        View sinaView = findViewById(R.id.login_sina);

        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        qqView.setOnClickListener(this);
        wxView.setOnClickListener(this);
        sinaView.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOAuthLoginManager.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInOAuthLogin) {
            showLoginDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOAuthLoginManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                launchRegisterUI();
                break;
            case R.id.btn_login:
                launchLoginUI();
                break;
            case R.id.login_qq:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_QQ);
                break;
            case R.id.login_wx:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_WEIXIN);
                break;
            case R.id.login_sina:
                doOAuthLogin(ConstantCode.OAUTH_PLATFORM_SINA_WEIBO);
                break;
            default:
                break;
        }
    }

    private void doOAuthLogin(String platform) {
        mInOAuthLogin = mOAuthLoginManager.doOAuthLogin(platform);
        if (mInOAuthLogin) {
            showLoginDialog();
        }
    }

    private void handleOAuthVerifyResult(boolean success) {
        if (!success) {
            mInOAuthLogin = false;
            dismissLoginDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.third_platform_oauth_failure)).show();
        }
    }

    private void handleGetOAuthInfoResult(boolean success) {
        if (!success) {
            mInOAuthLogin = false;
            dismissLoginDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.third_platform_oauth_failure)).show();
        }
    }

    private void handleOAuthLoginResult(int result) {
        mInOAuthLogin = false;
        dismissLoginDialog();

        switch (result) {
            case ConstantCode.ACCOUNT_OPERATION_SUCCESS:
                launchMainUI();
                break;
            default:
                ShowToast.makeText(this, R.drawable.emoji_cry,
                        getString(R.string.login_failure)).show();
                break;
        }
    }

    private void launchMainUI() {
        UILauncher.launchMainUIWhenJustLogin(this);
        finish();
    }

    private void launchLoginUI() {
        UILauncher.launchLoginUI(this);
        finish();
    }

    private void launchRegisterUI() {
        UILauncher.launchRegisterUI(this);
        finish();
    }

    private void showLoginDialog() {
        if (mLoginDialog == null) {
            mLoginDialog = SimpleProgressDialog.newInstance();
            mLoginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
        }
    }

    private void dismissLoginDialog() {
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }
}
