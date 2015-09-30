package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.command.account.CheckVerifyCodeCommand;
import com.ekuater.labelchat.command.account.ModifyPasswordCommand;
import com.ekuater.labelchat.command.account.QueryPersonalInfoCommand;
import com.ekuater.labelchat.command.account.RequestVerifyCodeCommand;
import com.ekuater.labelchat.command.account.ResetPasswordCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.delegate.event.LoginEvent;
import com.ekuater.labelchat.encoder.MD5Encoder;
import com.ekuater.labelchat.settings.SettingConstants;
import com.ekuater.labelchat.settings.SettingHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class AccountManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {

        /**
         * notify the im connection result
         *
         * @param result im connect result, success or error code
         */
        public void onImConnected(int result);

        /**
         * notify login result
         *
         * @param result success or error code
         */
        public void onLogin(int result);

        /**
         * notify logout result
         *
         * @param result success or error code
         */
        public void onLogout(int result);

        /**
         * notify register new user result
         *
         * @param result success or error code
         */
        public void onRegister(int result);

        /**
         * notify update personal information result
         *
         * @param result success or error code
         */
        public void onPersonalInfoUpdated(int result);

        /**
         * Notify third platform OAuth user bind account result
         *
         * @param result success or error code
         */
        public void onOAuthBindAccount(int result);

        /**
         * Notify account login in other client
         */
        public void onLoginInOtherClient();
    }

    public static class AbsListener implements IListener {

        @Override
        public void onImConnected(int result) {
        }

        @Override
        public void onLogin(int result) {
        }

        @Override
        public void onLogout(int result) {
        }

        @Override
        public void onRegister(int result) {
        }

        @Override
        public void onPersonalInfoUpdated(int result) {
        }

        @Override
        public void onOAuthBindAccount(int result) {
        }

        @Override
        public void onLoginInOtherClient() {
        }

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }
    }

    private static AccountManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountManager(context.getApplicationContext());
        }
    }

    public static AccountManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final SettingHelper mSettingHelper;
    private final List<WeakReference<IListener>> mListeners = new ArrayList<>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onCoreServiceConnected() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceConnected();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onCoreServiceDied() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceDied();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onImConnected(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onImConnected(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onAccountLogin(int result) {
            mSettingHelper.setLoginMethod((result == ConstantCode.ACCOUNT_OPERATION_SUCCESS)
                    ? SettingConstants.LOGIN_METHOD_AUTO_LOGIN
                    : SettingConstants.LOGIN_METHOD_MANUAL_LOGOUT);

            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLogin(result);
                } else {
                    mListeners.remove(i);
                }
            }
            CoreEventBusHub.getDefaultEventBus().post(new LoginEvent(
                    result, LoginEvent.From.ACCOUNT_MANAGER));
        }

        @Override
        public void onAccountLogout(int result) {
            mSettingHelper.setLoginMethod(SettingConstants.LOGIN_METHOD_MANUAL_LOGOUT);

            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLogout(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onAccountRegistered(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onRegister(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onAccountPersonalInfoUpdated(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onPersonalInfoUpdated(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onAccountOAuthBindAccount(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onOAuthBindAccount(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onAccountLoginInOtherClient() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLoginInOtherClient();
                } else {
                    mListeners.remove(i);
                }
            }
        }
    };

    private AccountManager(Context context) {
        super(context);
        mSettingHelper = SettingHelper.getInstance(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }

            mListeners.add(new WeakReference<>(listener));
            unregisterListener(null);
        }
    }

    public void unregisterListener(IListener listener) {
        synchronized (mListeners) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                if (mListeners.get(i).get() == listener) {
                    mListeners.remove(i);
                }
            }
        }
    }

    // twice encrypt password by md5
    private String encodePassword(String plainPassword) {
        MD5Encoder encoder = MD5Encoder.getInstance();
        String cipherPassword = encoder.encode(encoder.encode(plainPassword));

        return (cipherPassword == null) ? plainPassword : cipherPassword;
    }

    /**
     * register a new account
     *
     * @param mobile     mobile number
     * @param verifyCode verify code from server
     * @param password   password, MD5 encode twice
     * @param nickname   initialize nickname
     * @param gender     user gender
     */
    public void register(String mobile, String verifyCode, String password,
                         String nickname, int gender) {
        mCoreService.accountRegister(mobile, verifyCode, encodePassword(password),
                nickname, gender);
    }

    /**
     * account login
     *
     * @param user     account user name
     * @param password account password, MD5 encode twice
     */
    public void login(String user, String password) {
        mCoreService.accountLogin(user, encodePassword(password));
    }

    /**
     * automatic login
     */
    public void automaticLogin() {
        mCoreService.accountAutomaticLogin();
    }

    /**
     * Third platform OAuth login
     *
     * @param platform    third platform
     * @param openId      open id
     * @param accessToken third platform access token
     * @param tokenExpire token expire time
     * @param userInfo    user information get from third platform
     */
    public void oAuthLogin(String platform, String openId, String accessToken,
                           String tokenExpire, PersonalUpdateInfo userInfo) {
        mCoreService.accountOAuthLogin(platform, openId, accessToken, tokenExpire, userInfo);
    }

    /**
     * Third platform OAuth login, user information will be get by server.
     *
     * @param platform    third platform
     * @param openId      open id
     * @param accessToken third platform access token
     * @param tokenExpire token expire time
     */
    public void oAuthLogin(String platform, String openId, String accessToken,
                           String tokenExpire) {
        oAuthLogin(platform, openId, accessToken, tokenExpire, null);
    }

    /**
     * Convert third platform user to our own user by mobile
     *
     * @param mobile      mobile number
     * @param verifyCode  verify code from server
     * @param newPassword new password
     */
    public void oAuthBindAccount(String mobile, String verifyCode, String newPassword) {
        if (isInGuestMode()) {
            mNotifier.onAccountOAuthBindAccount(ConstantCode.ACCOUNT_OPERATION_SUCCESS);
            return;
        }
        mCoreService.accountOAuthBindAccount(mobile, verifyCode, encodePassword(newPassword));
    }

    /**
     * Logout current active account
     */
    public void logout() {
        mCoreService.accountLogout();
    }

    /**
     * update personal information of current account
     *
     * @param newInfo new personal information to be updated
     */
    public void updatePersonalInfo(PersonalUpdateInfo newInfo) {
        if (isInGuestMode()) {
            mNotifier.onAccountPersonalInfoUpdated(ConstantCode.ACCOUNT_OPERATION_SUCCESS);
            return;
        }

        mCoreService.accountUpdatePersonalInfo(newInfo);
    }

    /**
     * Get current account logon session
     *
     * @return current account logon session
     */
    public String getSession() {
        if (isInGuestMode()) {
            return mGuestMode.getSession();
        }

        return mCoreService.accountGetSession();
    }

    /**
     * Get current account user id
     *
     * @return current account user id
     */
    public String getUserId() {
        if (isInGuestMode()) {
            return mGuestMode.getUserId();
        }

        return mCoreService.accountGetUserId();
    }

    /**
     * Get current account label code
     *
     * @return current account labelCode
     */
    public String getLabelCode() {
        if (isInGuestMode()) {
            return mGuestMode.getLabelCode();
        }

        return mCoreService.accountGetLabelCode();
    }

    /**
     * Is now account login or not
     *
     * @return login or not
     */
    public boolean isLogin() {
        return mCoreService.accountIsLogin();
    }

    /**
     * Is now account im server connected or not
     *
     * @return connected or not
     */
    public boolean isImConnected() {
        return mCoreService.accountIsImConnected();
    }

    /**
     * Get login method, auto login or other
     *
     * @return login method
     */
    public int getLoginMethod() {
        return mSettingHelper.getLoginMethod();
    }

    /**
     * auto login or not
     *
     * @return auto login or not
     */
    public boolean isAutoLogin() {
        return (SettingConstants.LOGIN_METHOD_AUTO_LOGIN == getLoginMethod())
                && !TextUtils.isEmpty(mCoreService.accountGetSession())
                && !TextUtils.isEmpty(mCoreService.accountGetUserId())
                && !TextUtils.isEmpty(mCoreService.accountGetLabelCode());
    }

    /**
     * Get location of current account
     *
     * @return location information
     */
    public LocationInfo getLocation() {
        return mCoreService.getCurrentLocationInfo();
    }

    /**
     * Request verify code from server
     *
     * @param mobile   mobile number
     * @param scenario scenario
     * @param listener function call result listener
     */
    public void requestVerifyCode(String mobile, String scenario, FunctionCallListener listener) {
        RequestVerifyCodeCommand command = new RequestVerifyCodeCommand();
        command.putParamMobile(mobile);
        command.putParamScenario(scenario);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    /**
     * Check verify code expired or not
     *
     * @param mobile     mobile number
     * @param verifyCode verifyCode
     * @param listener   function call result listener
     */
    public void checkVerifyCode(String mobile, String verifyCode, FunctionCallListener listener) {
        CheckVerifyCodeCommand command = new CheckVerifyCodeCommand();
        command.putParamMobile(mobile);
        command.putParamVerifyCode(verifyCode);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    /**
     * Modify account password normally
     *
     * @param oldPassword old password
     * @param newPassword new password
     * @param listener    function call result listener
     * @deprecated
     */
    public void modifyPasswordNormally(String oldPassword, String newPassword,
                                       FunctionCallListener listener) {
        ModifyPasswordCommand command = new ModifyPasswordCommand(getSession(), getUserId());
        command.putParamOldPassword(encodePassword(oldPassword));
        command.putParamNewPassword(encodePassword(newPassword));
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    /**
     * Modify account password when forgot
     *
     * @param mobile      mobile number
     * @param verifyCode  verify code from server
     * @param newPassword new password
     * @param listener    function call result listener
     */
    public void resetPassword(String mobile, String verifyCode, String newPassword,
                              FunctionCallListener listener) {
        final ResetPasswordCommand command = new ResetPasswordCommand();
        final String encodePassword = encodePassword(newPassword);
        command.putParamMobile(mobile);
        command.putParamVerifyCode(verifyCode);
        command.putParamNewPassword(encodePassword);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void syncAccountInfo(FunctionCallListener listener) {
        QueryPersonalInfoCommand command = new QueryPersonalInfoCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }
}
