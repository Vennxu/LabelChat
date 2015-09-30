package com.ekuater.labelchat.ui.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.register.RegisterSetUserInfoFragment;
import com.ekuater.labelchat.ui.fragment.register.RegisterVerifyMobileFragment;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class RegisterActivity extends BackIconActivity implements Handler.Callback {

    private static final int MSG_HANDLE_REGISTER_RESULT = 101;
    private static final int MSG_HANDLE_LOGIN_RESULT = 102;
    private static final int MSG_HANDLE_LABEL_ADD_RESULT = 103;

    private AccountManager mAccountManager;
    private UserLabelManager mLabelManager;
    private FragmentManager mFm;
    private String mMobile;
    private String mPassword;
    private UserLabel[] mPreSetLabels;
    private Handler mHandler;

    private final RegisterVerifyMobileFragment.Listener mVerifyMobileListener
            = new RegisterVerifyMobileFragment.Listener() {
        @Override
        public void verifyDone(String mobile, String verifyCode) {
            showSetUserInfoFragment(mobile, verifyCode);
        }
    };
    private final RegisterSetUserInfoFragment.Listener mSetUserInfoListener
            = new RegisterSetUserInfoFragment.Listener() {
        @Override
        public void setUserInfoDone(String mobile, String verifyCode, String password,
                                    String nickname, int gender) {
            onSetUserInfoResult(mobile, verifyCode, password, nickname, gender);
        }
    };

    private final AccountManager.IListener mAccountListener = new AccountManager.AbsListener() {
        @Override
        public void onRegister(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_REGISTER_RESULT, result, 0);
            mHandler.sendMessage(message);
        }

        @Override
        public void onLogin(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_LOGIN_RESULT, result, 0);
            mHandler.sendMessage(message);
        }
    };
    private final UserLabelManager.IListener mLabelListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_LABEL_ADD_RESULT, result, 0);
            mHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mHandler = new Handler(this);
        mAccountManager = AccountManager.getInstance(this);
        mAccountManager.registerListener(mAccountListener);
        mLabelManager = UserLabelManager.getInstance(this);
        mLabelManager.registerListener(mLabelListener);
        mFm = getSupportFragmentManager();
        showVerifyMobileFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccountManager.unregisterListener(mAccountListener);
        mLabelManager.unregisterListener(mLabelListener);
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_REGISTER_RESULT:
                handleRegisterResult(msg.arg1);
                break;
            case MSG_HANDLE_LOGIN_RESULT:
                handleOnLoginResult(msg.arg1);
                break;
            case MSG_HANDLE_LABEL_ADD_RESULT:
                handleLabelAddResult(msg.arg1);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void showVerifyMobileFragment() {
        FragmentTransaction transaction = mFm.beginTransaction();
        Fragment fragment = RegisterVerifyMobileFragment.newInstance(mVerifyMobileListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showSetUserInfoFragment(String mobile, String verifyCode) {
        FragmentTransaction transaction = mFm.beginTransaction();
        Fragment fragment = RegisterSetUserInfoFragment.newInstance(mobile, verifyCode,
                mSetUserInfoListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void onSetUserInfoResult(String mobile, String verifyCode, String password,
                                     String nickname, int gender) {
        if (mAccountManager.isInGuestMode()) {
            mPreSetLabels = mLabelManager.getAllLabels();
        } else {
            mPreSetLabels = null;
        }
        registerAccount(mobile, verifyCode, password, nickname, gender);
    }

    private void registerAccount(String mobile, String verifyCode, String password,
                                 String nickname, int gender) {
        mAccountManager.register(mobile, verifyCode, password, nickname, gender);
        showProgressDialog();
        mMobile = mobile;
        mPassword = password;
    }

    private void handleRegisterResult(int result) {
        boolean success = false;
        int resId = R.string.register_failed;

        switch (result) {
            case ConstantCode.ACCOUNT_OPERATION_SUCCESS:
                mAccountManager.login(mMobile, mPassword);
                success = true;
                break;
            case ConstantCode.ACCOUNT_OPERATION_MOBILE_ALREADY_EXIST:
                resId = R.string.mobile_has_been_registered;
                break;
            case ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_EXPIRED:
                resId = R.string.verify_code_expired;
                break;
            case ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_WRONG:
                resId = R.string.verify_code_wrong;
                break;
            default:
                break;
        }

        if (!success) {
            dismissProgressDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry, getString(resId)).show();
        }
    }

    private void handleOnLoginResult(int result) {
        if (result != ConstantCode.ACCOUNT_OPERATION_SUCCESS) {
            dismissProgressDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry, getString(R.string.login_failure)).show();
            finish();
        } else {
            // Now add pre set user labels.
            if (mPreSetLabels != null && mPreSetLabels.length > 0) {
                final int length = mPreSetLabels.length;
                final BaseLabel[] addLabels = new BaseLabel[length];

                for (int i = 0; i < length; ++i) {
                    addLabels[i] = mPreSetLabels[i].toBaseLabel();
                }
                mLabelManager.addUserLabels(addLabels);
            } else {
                dismissProgressDialog();
                editPersonalInfo();
                finish();
            }
        }
    }

    private void handleLabelAddResult(int result) {
        dismissProgressDialog();
        if (result != ConstantCode.LABEL_OPERATION_SUCCESS) {
            ShowToast.makeText(this, R.drawable.emoji_cry, getString(R.string.add_label_failure)).show();
        }
        UILauncher.launchMainUIWhenJustLogin(this);
        editPersonalInfo();
        finish();
    }

    private static final String LOGIN_DIALOG_TAG = "ProgressDialog";
    private SimpleProgressDialog mProgressDialog;

    private void showProgressDialog() {
        dismissProgressDialog();
        mProgressDialog = SimpleProgressDialog.newInstance();
        mProgressDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void editPersonalInfo() {
        UILauncher.launchUserInfoSettingUI(this);
    }
}
