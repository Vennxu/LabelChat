package com.ekuater.labelchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.util.L;

/**
 * Created by Leo on 2014/12/24.
 *
 * @author LinYong
 */
public class OAuthLogin {

    private static final String TAG = OAuthLogin.class.getSimpleName();

    private static final int MSG_HANDLE_OAUTH_LOGIN_RESULT = 101;
    private static final int MSG_HANDLE_OAUTH_VERIFY_RESULT = 102;
    private static final int MSG_HANDLE_GET_OAUTH_INFO_RESULT = 103;

    public interface LoginListener {

        public void onOAuthVerifyResult(boolean success);

        public void onGetOAuthInfoResult(boolean success);

        public void onOAuthLoginResult(int result);
    }

    private interface ListenerNotifier {
        public void notify(LoginListener listener);
    }

    private static class OAuthResult {

        public final ThirdOAuth.OAuthInfo oAuthInfo;
        public final boolean success;

        public OAuthResult(ThirdOAuth.OAuthInfo info, boolean success) {
            this.oAuthInfo = info;
            this.success = success;
        }
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_OAUTH_LOGIN_RESULT:
                    handleOAuthLoginResult(msg.arg1);
                    break;
                case MSG_HANDLE_OAUTH_VERIFY_RESULT:
                    handleOAuthVerifyResult((OAuthResult) msg.obj);
                    break;
                case MSG_HANDLE_GET_OAUTH_INFO_RESULT:
                    handleGetOAuthInfoResult((OAuthResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final LoginListener mListener;
    private final ThirdOAuth mThirdOAuth;
    private final AccountManager mAccountManager;
    private final Handler mHandler;
    private final AccountManager.IListener mAccountListener = new AccountManager.AbsListener() {
        @Override
        public void onLogin(int result) {
            Message message = mHandler.obtainMessage(MSG_HANDLE_OAUTH_LOGIN_RESULT, result, 0);
            mHandler.sendMessage(message);
        }
    };

    public OAuthLogin(Activity activity, Looper looper, LoginListener listener) {
        if (activity == null) {
            throw new NullPointerException("OAuthLoginManager empty activity");
        }
        if (listener == null) {
            throw new NullPointerException("OAuthLoginManager empty listener");
        }

        if (looper == null) {
            looper = activity.getMainLooper();
        }

        mListener = listener;
        mHandler = new MainHandler(looper);
        mThirdOAuth = new ThirdOAuth(activity, new ThirdOAuth.IOAuthListener() {
            @Override
            public void onOAuthResult(ThirdOAuth.OAuthInfo info, boolean success) {
                Message message = mHandler.obtainMessage(MSG_HANDLE_OAUTH_VERIFY_RESULT,
                        new OAuthResult(info, success));
                mHandler.sendMessage(message);
            }

            @Override
            public void onGetOAuthInfoResult(ThirdOAuth.OAuthInfo info, boolean success) {
                Message message = mHandler.obtainMessage(MSG_HANDLE_GET_OAUTH_INFO_RESULT,
                        new OAuthResult(info, success));
                mHandler.sendMessage(message);
            }
        });
        mAccountManager = AccountManager.getInstance(activity);
        mAccountManager.registerListener(mAccountListener);
    }

    public void onDestroy() {
        mAccountManager.unregisterListener(mAccountListener);
    }

    public boolean doOAuthLogin(String platform) {
        L.v(TAG, "doOAuthLogin(), platform=" + platform);

        boolean _ret = true;

        if (ConstantCode.OAUTH_PLATFORM_QQ.equals(platform)) {
            mThirdOAuth.doQQOAuthVerify();
        } else if (ConstantCode.OAUTH_PLATFORM_SINA_WEIBO.equals(platform)) {
            mThirdOAuth.doSinaOAuthVerify();
        } else if(ConstantCode.OAUTH_PLATFORM_WEIXIN.equals(platform)) {
            mThirdOAuth.doWXOAuthVerify();
        } else {
            _ret = false;
        }

        return _ret;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mThirdOAuth.onActivityResult(requestCode, resultCode, data);
    }

    private void handleOAuthVerifyResult(OAuthResult oAuthResult) {
        L.v(TAG, "handleOAuthVerifyResult(), success=" + oAuthResult.success);
        notifyOAuthVerifyResult(oAuthResult.success);
    }

    private void handleGetOAuthInfoResult(OAuthResult oAuthResult) {
        L.v(TAG, "handleGetOAuthInfoResult(), success=" + oAuthResult.success);
        if (oAuthResult.success) {
            oAuthLoginInternal(oAuthResult.oAuthInfo);
        }
        notifyGetOAuthInfoResult(oAuthResult.success);
    }

    private void oAuthLoginInternal(ThirdOAuth.OAuthInfo oAuthInfo) {
        L.v(TAG, "oAuthLoginInternal(), start third platform login");
        PersonalUpdateInfo userInfo = new PersonalUpdateInfo();
        userInfo.setNickname(oAuthInfo.nickname);
        userInfo.setSex(oAuthInfo.sex);
        userInfo.setAvatar(oAuthInfo.avatarUrl);
        mAccountManager.oAuthLogin(oAuthInfo.platform, oAuthInfo.openId,
                oAuthInfo.accessToken, oAuthInfo.tokenExpire, userInfo);
    }

    private void handleOAuthLoginResult(int result) {
        notifyOAuthLoginResult(result);
    }

    private void notifyListener(ListenerNotifier notifier) {
        notifier.notify(mListener);
    }

    private void notifyOAuthVerifyResult(boolean success) {
        notifyListener(new OAuthVerifyResultNotifier(success));
    }

    private void notifyGetOAuthInfoResult(boolean success) {
        notifyListener(new GetOAuthInfoResultNotifier(success));
    }

    private void notifyOAuthLoginResult(int result) {
        notifyListener(new OAuthLoginResultNotifier(result));
    }

    private static class OAuthVerifyResultNotifier implements ListenerNotifier {

        private final boolean mSuccess;

        public OAuthVerifyResultNotifier(boolean success) {
            mSuccess = success;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onOAuthVerifyResult(mSuccess);
        }
    }

    private static class GetOAuthInfoResultNotifier implements ListenerNotifier {

        private final boolean mSuccess;

        public GetOAuthInfoResultNotifier(boolean success) {
            mSuccess = success;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onGetOAuthInfoResult(mSuccess);
        }
    }

    private static class OAuthLoginResultNotifier implements ListenerNotifier {

        private final int mResult;

        public OAuthLoginResultNotifier(int result) {
            mResult = result;
        }

        @Override
        public void notify(LoginListener listener) {
            listener.onOAuthLoginResult(mResult);
        }
    }
}
