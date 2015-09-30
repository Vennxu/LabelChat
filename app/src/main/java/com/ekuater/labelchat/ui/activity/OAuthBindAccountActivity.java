package com.ekuater.labelchat.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.register.SendIdentifyCodeFragment;
import com.ekuater.labelchat.ui.fragment.register.SetPasswordFragment;
import com.ekuater.labelchat.ui.fragment.register.ValidateIdentifyCodeFragment;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * @author LinYong
 */
public class OAuthBindAccountActivity extends BackIconActivity {

    private static final int MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT = 101;

    private AccountManager mAccountManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT:
                    handleBindAccountResult(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private final AccountManager.IListener mAccountListener = new AccountManager.AbsListener() {
        @Override
        public void onOAuthBindAccount(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT,
                    result, 0);
            mHandler.sendMessage(message);
        }
    };

    private final SendIdentifyCodeFragment.IListener mVerifyCodeSendListener
            = new SendIdentifyCodeFragment.IListener() {
        @Override
        public void identifyingCodeSent(String mobile, boolean success) {
            onVerifyCodeSent(mobile, success);
        }
    };
    private final ValidateIdentifyCodeFragment.IListener mValidateListener
            = new ValidateIdentifyCodeFragment.IListener() {
        @Override
        public void validateDone(String mobile, String verifyCOde) {
            onValidateDone(mobile, verifyCOde);
        }
    };
    private final SetPasswordFragment.IListener mSetPasswordListener
            = new SetPasswordFragment.IListener() {
        @Override
        public void setPasswordResult(String mobile, String verifyCode, String password) {
            onSetPasswordResult(mobile, verifyCode, password);
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
        Fragment fragment = SendIdentifyCodeFragment.newInstance(mVerifyCodeSendListener,
                CommandFields.Normal.SCENARIO_BIND_MOBILE,
                getString(R.string.bind_account));

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void onVerifyCodeSent(String mobile, boolean success) {
        if (success) {
            showValidateVerifyCodeFragment(mobile);
        }
    }

    private void showValidateVerifyCodeFragment(String mobile) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = ValidateIdentifyCodeFragment.newInstance(mobile,
                mValidateListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void onValidateDone(String mobile, String verifyCOde) {
        showSetPasswordFragment(mobile, verifyCOde);
    }

    private void showSetPasswordFragment(String mobile, String verifyCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = SetPasswordFragment.newInstance(mobile, verifyCode,
                mSetPasswordListener);

        transaction.replace(R.id.fragment_container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void onSetPasswordResult(String mobile, String verifyCode, String password) {
        bindAccount(mobile, verifyCode, password);
    }

    private void bindAccount(String mobile, String verifyCode, String password) {
        showProgressDialog();
        mAccountManager.oAuthBindAccount(mobile, verifyCode, password);
    }

    private void handleBindAccountResult(int result) {
        dismissProgressDialog();

        int resId;

        switch (result) {
            case ConstantCode.ACCOUNT_OPERATION_SUCCESS:
                resId = R.string.oauth_bind_success;
                ShowToast.makeText(this, R.drawable.emoji_smile, getString(resId)).show();
                break;
            case ConstantCode.ACCOUNT_OPERATION_DO_NOT_NEED:
                resId = R.string.operation_not_need;
                ShowToast.makeText(this, R.drawable.emoji_smile, getString(resId)).show();
                break;
            case ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_EXPIRED:
                resId = R.string.oauth_bind_verify_code_expired;
                ShowToast.makeText(this, R.drawable.emoji_cry, getString(resId)).show();
                break;
            case ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_WRONG:
                resId = R.string.oauth_bind_verify_code_wrong;
                ShowToast.makeText(this, R.drawable.emoji_cry, getString(resId)).show();
                break;
            case ConstantCode.ACCOUNT_OPERATION_MOBILE_ALREADY_EXIST:
                resId = R.string.mobile_has_been_registered;
                ShowToast.makeText(this, R.drawable.emoji_cry, getString(resId)).show();
                break;
            case ConstantCode.ACCOUNT_OPERATION_USER_NOT_EXIST:
            default:
                resId = R.string.oauth_bind_failure;
                ShowToast.makeText(this, R.drawable.emoji_cry, getString(resId)).show();
                break;
        }
        finish();
    }

    private static final String LOGIN_DIALOG_TAG = "ProgressDialog";
    private SimpleProgressDialog mProgressDialog;

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
