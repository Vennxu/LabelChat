package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.register.SendIdentifyCodeFragment;
import com.ekuater.labelchat.ui.fragment.register.SetPasswordFragment;
import com.ekuater.labelchat.ui.fragment.register.ValidateIdentifyCodeFragment;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class ResetPasswordActivity extends BackIconActivity {

    private static final int MSG_HANDLE_MODIFY_RESULT = 101;
    private static final int MSG_HANDLE_LOGIN_RESULT = 102;

    private AccountManager mAccountManager;
    private String mMobile;
    private String mPassword;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_MODIFY_RESULT:
                    handleModifyResult(msg.arg1);
                    break;
                case MSG_HANDLE_LOGIN_RESULT:
                    handleOnLoginResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private final ValidateIdentifyCodeFragment.IListener mValidateListener
            = new ValidateIdentifyCodeFragment.IListener() {
        @Override
        public void validateDone(String mobile, String verifyCOde) {
            onValidateDone(mobile, verifyCOde);
        }
    };
    private final SendIdentifyCodeFragment.IListener mSendListener
            = new SendIdentifyCodeFragment.IListener() {
        @Override
        public void identifyingCodeSent(String mobile, boolean success) {
            onIdentifyingCodeSent(mobile, success);
        }
    };
    private final SetPasswordFragment.IListener mSetPasswordListener
            = new SetPasswordFragment.IListener() {
        @Override
        public void setPasswordResult(String mobile, String verifyCode, String password) {
            onSetPasswordResult(mobile, verifyCode, password);
        }
    };
    private final AccountManager.IListener mAccountListener = new AccountManager.AbsListener() {
        @Override
        public void onLogin(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_LOGIN_RESULT, result, 0);
            mHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        mAccountManager = AccountManager.getInstance(this);
        mAccountManager.registerListener(mAccountListener);
        showSendIdentifyCodeFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccountManager.unregisterListener(mAccountListener);
    }

    private void showSendIdentifyCodeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = SendIdentifyCodeFragment.newInstance(mSendListener,
                CommandFields.Normal.SCENARIO_MODIFY_PASSWORD,
                getString(R.string.forgot_password));

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showValidateIdentifyCodeFragment(String mobile) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = ValidateIdentifyCodeFragment.newInstance(mobile,
                mValidateListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void showSetPasswordFragment(String mobile, String verifyCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = SetPasswordFragment.newInstance(mobile, verifyCode,
                getString(R.string.reset_password), mSetPasswordListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void onIdentifyingCodeSent(String mobile, boolean success) {
        if (success) {
            showValidateIdentifyCodeFragment(mobile);
        }
    }

    private void onValidateDone(String mobile, String verifyCOde) {
        showSetPasswordFragment(mobile, verifyCOde);
    }

    private void onSetPasswordResult(String mobile, String verifyCode, String password) {
        modifyPassword(mobile, verifyCode, password);
    }

    private void modifyPassword(String mobile, String verifyCode, String password) {
        mMobile = mobile;
        mPassword = password;
        showProgressDialog();
        mAccountManager.resetPassword(mobile, verifyCode, password,
                new FunctionCallListener() {
                    @Override
                    public void onCallResult(int result, int errorCode, String errorDesc) {
                        Message message = mHandler.obtainMessage(MSG_HANDLE_MODIFY_RESULT,
                                errorCode, 0);
                        mHandler.sendMessage(message);
                    }
                });
    }

    private void handleModifyResult(int errorCode) {
        if (errorCode == CommandErrorCode.REQUEST_SUCCESS) {
            ShowToast.makeText(this, R.drawable.emoji_smile, getString(R.string.modify_password_success)).show();
            mAccountManager.login(mMobile, mPassword);
        } else {
            dismissProgressDialog();
            ShowToast.makeText(this, R.drawable.emoji_cry, getString(R.string.modify_password_failed)).show();
            finish();
        }
    }

    private void handleOnLoginResult(int result) {
        dismissProgressDialog();
        if (result != ConstantCode.ACCOUNT_OPERATION_SUCCESS) {
            ShowToast.makeText(this, R.drawable.emoji_cry, getString(R.string.login_failure)).show();
        } else {
            UILauncher.launchMainUIWhenJustLogin(this);
        }
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
}
