
package com.ekuater.labelchat.coreservice.account;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

import com.ekuater.labelchat.BuildConfig;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.account.LoginCommand;
import com.ekuater.labelchat.command.account.LogoutCommand;
import com.ekuater.labelchat.command.account.OAuthBindAccountCommand;
import com.ekuater.labelchat.command.account.OAuthLoginCommand;
import com.ekuater.labelchat.command.account.PersonalInfo;
import com.ekuater.labelchat.command.account.RegisterCommand;
import com.ekuater.labelchat.command.account.UpdatePersonalInfoCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LoginInOtherClientMessage;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author LinYong
 */
public class AccountManagerCore {

    private interface ListenerNotifier {
        public void notify(IAccountListener listener);
    }

    private static final String TAG = AccountManagerCore.class.getSimpleName();

    private static final long UPDATE_LOGIN_INFO_INTERVAL = 23 * 60 * 60 * 1000L;
    private static final String ACTION_UPDATE_LOGIN_INFO_ALARM
            = BuildConfig.APPLICATION_ID + ".ACTION_UPDATE_LOGIN_INFO_ALARM";

    private final ICoreServiceCallback mCallback;
    private final SettingHelper mSettingHelper;
    private final List<WeakReference<IAccountListener>> mListeners = new ArrayList<>();

    private static abstract class ObjCmdRespHandler implements ICommandResponseHandler {

        protected final Object mObj;

        public ObjCmdRespHandler(Object obj) {
            mObj = obj;
        }
    }

    private static final class CommandResult {

        public final int result;
        public final String response;
        public final Object extra;

        public CommandResult(int result, String response, Object extra) {
            this.result = result;
            this.response = response;
            this.extra = extra;
        }
    }

    private static final int MSG_HANDLE_LOGIN_RESULT = 101;
    private static final int MSG_HANDLE_AUTO_LOGIN = 102;
    private static final int MSG_HANDLE_UPDATE_LOGIN_INFO = 103;
    private static final int MSG_HANDLE_OAUTH_LOGIN_RESULT = 104;
    private static final int MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT = 105;
    private static final int MSG_HANDLE_LOGOUT = 106;
    private static final int MSG_HANDLE_LOGOUT_RESULT = 107;
    private static final int MSG_HANDLE_REGISTER_RESULT = 108;
    private static final int MSG_HANDLE_UPDATE_PERSONAL_INFO_RESULT = 109;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_LOGIN_RESULT:
                    handleLoginResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_AUTO_LOGIN:
                    handleAutomaticLogin(msg.arg1 != 0);
                    break;
                case MSG_HANDLE_UPDATE_LOGIN_INFO:
                    handleUpdateLoginInfo();
                    break;
                case MSG_HANDLE_OAUTH_LOGIN_RESULT:
                    handleOAuthLoginResult(msg.obj);
                    break;
                case MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT:
                    handleOAuthBindAccountResult(msg.obj);
                    break;
                case MSG_HANDLE_LOGOUT:
                    handleLogout();
                    break;
                case MSG_HANDLE_LOGOUT_RESULT:
                    handleLogoutResult(msg.obj);
                    break;
                case MSG_HANDLE_REGISTER_RESULT:
                    handleRegisterResult(msg.obj);
                    break;
                case MSG_HANDLE_UPDATE_PERSONAL_INFO_RESULT:
                    handleUpdatePersonalInfoResult(msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final Context mContext;
    private final ProcessHandler mProcessHandler;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPi;
    private BroadcastReceiver mAlarmReceiver;
    private boolean mWantUpdateLoginInfo;
    // Account about information
    private String mPassword; // MD5 twice encoded password
    private String mSession;
    private String mUserId;
    private String mLabelCode;
    private long mUpdateTime;
    private final AtomicBoolean mIsLogin = new AtomicBoolean(false);
    private final AtomicBoolean mDoingLogin = new AtomicBoolean(false);

    public AccountManagerCore(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmPi = null;
        mAlarmReceiver = new AlarmReceiver(this);
        mWantUpdateLoginInfo = false;
        mCallback = callback;
        mProcessHandler = new ProcessHandler(mCallback.getProcessLooper());
        mSettingHelper = SettingHelper.getInstance(mContext);
        loadAccountInfoFromLocal();

        mContext.registerReceiver(mAlarmReceiver,
                new IntentFilter(ACTION_UPDATE_LOGIN_INFO_ALARM));

        automaticLogin();
    }

    public void deInit() {
        mContext.unregisterReceiver(mAlarmReceiver);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mContext.unregisterReceiver(mAlarmReceiver);
        cancelUpdateLoginInfo();
    }

    public void registerListener(final IAccountListener listener) {
        for (WeakReference<IAccountListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<>(listener));
        unregisterListener(null);
    }

    public void unregisterListener(final IAccountListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    private int convertResultCode(int commandResultCode) {
        int code = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;

        switch (commandResultCode) {
            case CommandErrorCode.REQUEST_SUCCESS:
                code = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
                break;
            case CommandErrorCode.NO_DATA:
                code = ConstantCode.ACCOUNT_OPERATION_NO_DATA;
                break;
            case CommandErrorCode.DATA_ALREADY_EXIST:
                code = ConstantCode.ACCOUNT_OPERATION_DATA_ALREADY_EXIST;
                break;
            case CommandErrorCode.USER_OR_PASSWORD_ERROR:
                code = ConstantCode.ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR;
                break;
            case CommandErrorCode.AUTHORIZE_FAILURE:
                code = ConstantCode.ACCOUNT_OPERATION_AUTHORIZE_FAILURE;
                break;
            case CommandErrorCode.USER_NOT_EXIST:
                code = ConstantCode.ACCOUNT_OPERATION_USER_NOT_EXIST;
                break;
            case CommandErrorCode.ILLEGAL_PASSWORD:
                code = ConstantCode.ACCOUNT_OPERATION_ILLEGAL_PASSWORD;
                break;
            case CommandErrorCode.SYSTEM_ERROR:
                code = ConstantCode.ACCOUNT_OPERATION_SYSTEM_ERROR;
                break;
            case CommandErrorCode.VERIFY_CODE_EXPIRED:
                code = ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_EXPIRED;
                break;
            case CommandErrorCode.VERIFY_CODE_WRONG:
                code = ConstantCode.ACCOUNT_OPERATION_VERIFY_CODE_WRONG;
                break;
            case CommandErrorCode.MOBILE_ALREADY_EXIST:
                code = ConstantCode.ACCOUNT_OPERATION_MOBILE_ALREADY_EXIST;
                break;
            default:
                break;
        }

        return code;
    }

    public synchronized void login(String user, String password) {
        if (mDoingLogin.get()) {
            return;
        }

        mDoingLogin.set(true);
        LoginCommand command = new LoginCommand();
        command.putParamLoginText(user);
        command.putParamPassword(password);
        ICommandResponseHandler handler = new ObjCmdRespHandler((Object) password) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(MSG_HANDLE_LOGIN_RESULT,
                        new CommandResult(result, response, mObj));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleLoginResult(CommandResult commandResult) {
        final int result = commandResult.result;
        final String response = commandResult.response;
        final Object extra = commandResult.extra;

        int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;
        String oldUserId = mUserId;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                _ret = parseLoginResponse(response, (String) extra);
                break;
            default:
                break;
        }

        boolean infoUpdated = false;
        if (_ret == ConstantCode.ACCOUNT_OPERATION_SUCCESS) {
            handleUpdateLoginInfo();
            mSettingHelper.setAccountLoginAuthType(ConstantCode.AUTH_TYPE_NORMAL);
            mSettingHelper.setAccountOAuthPlatform("");
            mSettingHelper.setAccountOAuthOpenId("");
            infoUpdated = true;
        }
        mDoingLogin.set(false);

        // Check if account has been changed or not
        boolean accountChanged = (oldUserId == null
                || !oldUserId.equals(mUserId));
        notifyLoginResult(_ret, accountChanged, infoUpdated);
    }

    private synchronized int parseLoginResponse(String response, String password) {
        int result;

        try {
            LoginCommand.CommandResponse cmdResp
                    = new LoginCommand.CommandResponse(response);

            if (cmdResp.requestSuccess()) {
                result = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
                PersonalInfo info = cmdResp.getPersonalInfo();
                String session = cmdResp.getSession();
                String labelCode = info.getLabelCode();
                String userId = info.getUserId();
                setAccountInfo(session, labelCode, password, userId,
                        System.currentTimeMillis());
                info.saveToSetting(mContext);
                mSettingHelper.setAccountRongCloudToken(cmdResp.getToken());
                mIsLogin.set(true);
            } else {
                result = convertResultCode(cmdResp.getErrorCode());
            }
        } catch (JSONException e) {
            L.w(TAG, e);
            result = ConstantCode.ACCOUNT_OPERATION_RESPONSE_DATA_ERROR;
        }

        return result;
    }

    private void notifyLoginResult(final int result, final boolean accountChanged,
                                   final boolean infoUpdated) {
        notifyListeners(new ListenerNotifier() {
            @Override
            public void notify(IAccountListener listener) {
                listener.onLoginResult(result, accountChanged, infoUpdated);
            }
        });
    }

    public synchronized void automaticLogin() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_AUTO_LOGIN, 0, 0);
        mProcessHandler.removeMessages(MSG_HANDLE_AUTO_LOGIN);
        mProcessHandler.sendMessage(message);
    }

    public synchronized void forceAutomaticLogin() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_AUTO_LOGIN, 1, 0);
        mProcessHandler.removeMessages(MSG_HANDLE_AUTO_LOGIN);
        mProcessHandler.sendMessage(message);
    }

    private void handleAutomaticLogin(boolean force) {
        if (!mIsLogin.get() || force) {
            final boolean accountLegal = !TextUtils.isEmpty(mLabelCode)
                    && !TextUtils.isEmpty(mPassword);
            final long sessionTime = System.currentTimeMillis() - mUpdateTime;
            final boolean sessionLegal = accountLegal && !TextUtils.isEmpty(mSession)
                    && sessionTime < UPDATE_LOGIN_INFO_INTERVAL && !force;

            if (sessionLegal) {
                mIsLogin.set(true);
                notifyLoginResult(ConstantCode.ACCOUNT_OPERATION_SUCCESS, false, false);
                handleUpdateLoginInfo();
            } else if (accountLegal) {
                login(mLabelCode, mPassword);
            } else {
                notifyLoginResult(ConstantCode.ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR,
                        false, false);
            }
        } else {
            notifyLoginResult(ConstantCode.ACCOUNT_OPERATION_SUCCESS, false, false);
        }
    }

    public void onNewPushMessage(SystemPush systemPush) {
        if (systemPush != null) {
            switch (systemPush.getType()) {
                case SystemPushType.TYPE_LOGIN_ON_OTHER_CLIENT:
                    handleLoginInOtherClient(systemPush);
                    break;
                default:
                    break;
            }
        }
    }

    private synchronized void handleLoginInOtherClient(SystemPush systemPush) {
        final LoginInOtherClientMessage message
                = LoginInOtherClientMessage.build(systemPush);

        if (message != null) {
            logout();
            clearAccountInfo();
            notifyLoginInOtherClient();
        }
    }

    private void notifyLoginInOtherClient() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onLoginInOtherClient();
            } else {
                mListeners.remove(i);
            }
        }
    }

    public void networkAvailableChanged(boolean available) {
        if (available && mWantUpdateLoginInfo) {
            updateLoginInfo();
        }
    }

    private synchronized void updateLoginInfo() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_UPDATE_LOGIN_INFO);
        mProcessHandler.removeMessages(MSG_HANDLE_UPDATE_LOGIN_INFO);
        mProcessHandler.sendMessage(message);
    }

    private void handleUpdateLoginInfo() {
        final long timeInterval = System.currentTimeMillis() - mUpdateTime;
        long nextUpdateTime = -1;

        cancelUpdateLoginInfo();

        if (timeInterval >= UPDATE_LOGIN_INFO_INTERVAL) {
            // update account login information now
            if (isNetworkAvailable()) {
                updateLoginInfoInternal();
                nextUpdateTime = UPDATE_LOGIN_INFO_INTERVAL;
            } else {
                // update when network available
                mWantUpdateLoginInfo = true;
            }
        } else {
            nextUpdateTime = UPDATE_LOGIN_INFO_INTERVAL - timeInterval;
        }

        if (nextUpdateTime > 0) {
            mAlarmPi = PendingIntent.getBroadcast(mContext, 0,
                    new Intent(ACTION_UPDATE_LOGIN_INFO_ALARM),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + nextUpdateTime,
                    mAlarmPi);
        }
    }

    private synchronized void cancelUpdateLoginInfo() {
        mWantUpdateLoginInfo = false;
        if (mAlarmPi != null) {
            mAlarmManager.cancel(mAlarmPi);
            mAlarmPi = null;
        }
    }

    private void updateLoginInfoInternal() {
        LoginCommand command = new LoginCommand();
        command.putParamLoginText(mLabelCode);
        command.putParamPassword(mPassword);
        ICommandResponseHandler handler = new ObjCmdRespHandler((Object) mPassword) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;

                switch (result) {
                    case ConstantCode.EXECUTE_RESULT_SUCCESS:
                        _ret = parseLoginResponse(response, (String) mObj);
                        break;
                    default:
                        break;
                }

                switch (_ret) {
                    case ConstantCode.ACCOUNT_OPERATION_SUCCESS:
                        notifyLoginResult(_ret, false, true);
                        break;
                    case ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR:
                        // update when network available
                        mWantUpdateLoginInfo = true;
                        break;
                    case ConstantCode.ACCOUNT_OPERATION_ILLEGAL_PASSWORD:
                    case ConstantCode.ACCOUNT_OPERATION_AUTHORIZE_FAILURE:
                    case ConstantCode.ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR:
                        // logout
                        notifyLoginResult(ConstantCode.ACCOUNT_OPERATION_AUTHORIZE_FAILURE,
                                false, false);
                        logout();
                        break;
                    default:
                        break;
                }
            }
        };
        executeCommand(command, handler);
    }

    private static class OAuthInfo {

        public final String platform;
        public final String openId;

        public OAuthInfo(String platform, String openId) {
            this.platform = platform;
            this.openId = openId;
        }
    }

    public synchronized void oAuthLogin(String platform, String openId,
                                        String accessToken, String tokenExpire,
                                        PersonalUpdateInfo userInfo) {
        if (mDoingLogin.get()) {
            return;
        }

        mDoingLogin.set(true);
        OAuthLoginCommand command = new OAuthLoginCommand();
        command.putParamPlatform(platform);
        command.putParamOpenId(openId);
        command.putParamAccessToken(accessToken);
        command.putParamTokenExpire(tokenExpire);
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                command.putParamAvatar(userInfo.getAvatar());
            }
            if (!TextUtils.isEmpty(userInfo.getAvatarThumb())) {
                command.putParamAvatarThumb(userInfo.getAvatarThumb());
            }
            if (!TextUtils.isEmpty(userInfo.getNickname())) {
                command.putParamNickname(userInfo.getNickname());
            }
            if (userInfo.getSex() > 0) {
                command.putParamSex(userInfo.getSex());
            }
        }
        ICommandResponseHandler handler = new ObjCmdRespHandler(
                new OAuthInfo(platform, openId)) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(MSG_HANDLE_OAUTH_LOGIN_RESULT,
                        new CommandResult(result, response, mObj));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleOAuthLoginResult(Object object) {
        if (!(object instanceof CommandResult)) {
            return;
        }

        final CommandResult commandResult = (CommandResult) object;
        final int result = commandResult.result;
        final String response = commandResult.response;
        final OAuthInfo oAuthInfo = (OAuthInfo) commandResult.extra;

        int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;
        String oldUserId = mUserId;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                _ret = parseOAuthLoginResponse(response);
                break;
            default:
                break;
        }

        boolean infoUpdated = false;
        if (_ret == ConstantCode.ACCOUNT_OPERATION_SUCCESS) {
            handleUpdateLoginInfo();
            mSettingHelper.setAccountLoginAuthType(ConstantCode.AUTH_TYPE_OAUTH);
            mSettingHelper.setAccountOAuthPlatform(oAuthInfo.platform);
            mSettingHelper.setAccountOAuthOpenId(oAuthInfo.openId);
            infoUpdated = true;
        }
        mDoingLogin.set(false);

        // Check if account has been changed or not
        boolean accountChanged = (oldUserId == null
                || !oldUserId.equals(mUserId));
        notifyLoginResult(_ret, accountChanged, infoUpdated);
    }

    private int parseOAuthLoginResponse(String response) {
        int result;

        try {
            OAuthLoginCommand.CommandResponse cmdResp
                    = new OAuthLoginCommand.CommandResponse(response);

            if (cmdResp.requestSuccess()) {
                result = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
                PersonalInfo info = cmdResp.getPersonalInfo();
                String session = cmdResp.getSession();
                String labelCode = info.getLabelCode();
                String userId = info.getUserId();
                String password = cmdResp.getPassword();
                setAccountInfo(session, labelCode, password, userId,
                        System.currentTimeMillis());
                info.saveToSetting(mContext);
                mSettingHelper.setAccountRongCloudToken(cmdResp.getToken());
                mIsLogin.set(true);
            } else {
                result = convertResultCode(cmdResp.getErrorCode());
            }
        } catch (JSONException e) {
            L.w(TAG, e);
            result = ConstantCode.ACCOUNT_OPERATION_RESPONSE_DATA_ERROR;
        }

        return result;
    }

    private static class BindAccountInfo {

        public final String mobile;
        public final String password;

        public BindAccountInfo(String mobile, String password) {
            this.mobile = mobile;
            this.password = password;
        }
    }

    public synchronized void oAuthBindAccount(String mobile, String verifyCode,
                                              String newPassword) {
        if (mSettingHelper.getAccountLoginAuthType() != ConstantCode.AUTH_TYPE_OAUTH) {
            notifyOAuthBindAccountResult(ConstantCode.ACCOUNT_OPERATION_DO_NOT_NEED);
            return;
        }

        String platform = mSettingHelper.getAccountOAuthPlatform();
        String openId = mSettingHelper.getAccountOAuthOpenId();
        OAuthBindAccountCommand command = new OAuthBindAccountCommand(getSession(), getUserId());
        command.putParamPlatform(platform);
        command.putParamOpenId(openId);
        command.putParamMobile(mobile);
        command.putParamVerifyCode(verifyCode);
        command.putParamNewPassword(newPassword);
        ICommandResponseHandler handler = new ObjCmdRespHandler(
                new BindAccountInfo(mobile, newPassword)) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(
                        MSG_HANDLE_OAUTH_BIND_ACCOUNT_RESULT,
                        new CommandResult(result, response, mObj));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleOAuthBindAccountResult(Object object) {
        if (!(object instanceof CommandResult)) {
            return;
        }

        final CommandResult commandResult = (CommandResult) object;
        final int result = commandResult.result;
        final String response = commandResult.response;
        final BindAccountInfo bindInfo = (BindAccountInfo) commandResult.extra;

        int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    OAuthBindAccountCommand.CommandResponse cmdResp
                            = new OAuthBindAccountCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        _ret = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
                        // now save the mobile and new password.
                        setAccountInfo(mSession, mLabelCode, bindInfo.password,
                                mUserId, System.currentTimeMillis());
                        mSettingHelper.setAccountMobile(bindInfo.mobile);
                    } else {
                        _ret = convertResultCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    _ret = ConstantCode.ACCOUNT_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyOAuthBindAccountResult(_ret);
    }

    private void notifyOAuthBindAccountResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onOAuthBindAccountResult(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    public void logout() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_LOGOUT);
        mProcessHandler.removeMessages(MSG_HANDLE_LOGOUT);
        mProcessHandler.sendMessage(message);
    }

    private void handleLogout() {
        LogoutCommand command = new LogoutCommand(mSession);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(
                        MSG_HANDLE_LOGOUT_RESULT,
                        new CommandResult(result, response, null));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleLogoutResult(Object object) {
        if (!(object instanceof CommandResult)) {
            return;
        }

        // Do nothing, just logout directly
        mIsLogin.set(false);
        cancelUpdateLoginInfo();
        notifyLogoutResult(ConstantCode.ACCOUNT_OPERATION_SUCCESS);
    }

    private void notifyLogoutResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onLogoutResult(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    public void register(String mobile, String verifyCode, String password,
                         String nickname, int gender) {
        RegisterCommand command = new RegisterCommand();
        command.putParamMobile(mobile);
        command.putParamPassword(password);
        command.putParamVerifyCode(verifyCode);
        command.putParamNickname(nickname);
        command.putParamSex(gender);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(MSG_HANDLE_REGISTER_RESULT,
                        new CommandResult(result, response, null));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleRegisterResult(Object object) {
        if (!(object instanceof CommandResult)) {
            return;
        }

        final CommandResult commandResult = (CommandResult) object;
        final int result = commandResult.result;
        final String response = commandResult.response;

        int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                _ret = parseRegisterResponse(response);
                break;
            default:
                break;
        }

        notifyRegisterResult(_ret);
    }

    private int parseRegisterResponse(String response) {
        int result = ConstantCode.ACCOUNT_OPERATION_RESPONSE_DATA_ERROR;

        try {
            RegisterCommand.CommandResponse cmdResp
                    = new RegisterCommand.CommandResponse(response);

            if (cmdResp.requestSuccess()) {
                result = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
            } else {
                result = convertResultCode(cmdResp.getErrorCode());
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return result;
    }

    private void notifyRegisterResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onRegisterResult(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    public boolean isLogin() {
        return mIsLogin.get();
    }

    private void putPersonalInfoToCommand(PersonalUpdateInfo info,
                                          UpdatePersonalInfoCommand command) {
        if (!TextUtils.isEmpty(info.getAvatar())) {
            command.putParamAvatar(info.getAvatar());
        }
        if (!TextUtils.isEmpty(info.getNickname())) {
            command.putParamNickname(info.getNickname());
        }
        if (info.getSex() >= 0) {
            command.putParamSex(info.getSex());
        }
        if (!TextUtils.isEmpty(info.getProvince())) {
            command.putParamProvince(info.getProvince());
        }
        if (!TextUtils.isEmpty(info.getCity())) {
            command.putParamCity(info.getCity());
        }
        if (!TextUtils.isEmpty(info.getSchool())) {
            command.putParamSchool(info.getSchool());
        }
        if (info.getConstellation() >= 0) {
            command.putParamConstellation(info.getConstellation());
        }
        if (!TextUtils.isEmpty(info.getSignature())) {
            command.putParamSignature(info.getSignature());
        }
        if (!TextUtils.isEmpty(info.getTheme())) {
            command.putParamTheme(info.getTheme());
        }
    }

    public synchronized void updatePersonalInfo(PersonalUpdateInfo newInfo) {
        UpdatePersonalInfoCommand command = new UpdatePersonalInfoCommand(mSession, mUserId);
        putPersonalInfoToCommand(newInfo, command);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message message = mProcessHandler.obtainMessage(
                        MSG_HANDLE_UPDATE_PERSONAL_INFO_RESULT,
                        new CommandResult(result, response, null));
                mProcessHandler.sendMessage(message);
            }
        };
        executeCommand(command, handler);
    }

    private void handleUpdatePersonalInfoResult(Object object) {
        if (!(object instanceof CommandResult)) {
            return;
        }

        final CommandResult commandResult = (CommandResult) object;
        final int result = commandResult.result;
        final String response = commandResult.response;

        int _ret = ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR;

        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                _ret = parseUpdatePersonalInfoResult(response);
                break;
            default:
                break;
        }
        notifyUpdatePersonalInfoResult(_ret);
    }

    private int parseUpdatePersonalInfoResult(String response) {
        int result = ConstantCode.ACCOUNT_OPERATION_RESPONSE_DATA_ERROR;

        try {
            UpdatePersonalInfoCommand.CommandResponse cmdResp
                    = new UpdatePersonalInfoCommand.CommandResponse(response);

            if (cmdResp.requestSuccess()) {
                result = ConstantCode.ACCOUNT_OPERATION_SUCCESS;
            } else {
                result = convertResultCode(cmdResp.getErrorCode());
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return result;
    }

    private void notifyUpdatePersonalInfoResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onPersonalInfoUpdatedResult(result);
            } else {
                mListeners.remove(i);
            }
        }
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        executeCommand(command.toRequestCommand(), handler);
    }

    private void executeCommand(RequestCommand command, ICommandResponseHandler handler) {
        mCallback.executeCommand(command, handler);
    }

    private boolean isNetworkAvailable() {
        return mCallback.isNetworkAvailable();
    }

    public String getSession() {
        return mSession;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getLabelCode() {
        return mLabelCode;
    }

    public String getPassword() {
        return mPassword;
    }

    private void clearAccountInfo() {
        setAccountInfo("", "", "", "", System.currentTimeMillis());
        mSettingHelper.clearAccountSettings();
    }

    private synchronized void setAccountInfo(String session, String labelCode,
                                             String password, String userId,
                                             long updateTime) {
        mSession = session;
        mLabelCode = labelCode;
        mPassword = password;
        mUserId = userId;
        mUpdateTime = updateTime;
        saveAccountInfoToLocal();
    }

    private void saveAccountInfoToLocal() {
        mSettingHelper.setAccountSession(mSession);
        mSettingHelper.setAccountLabelCode(mLabelCode);
        mSettingHelper.setAccountPassword(mPassword);
        mSettingHelper.setAccountUserId(mUserId);
        mSettingHelper.setAccountUpdateTime(mUpdateTime);
    }

    private void loadAccountInfoFromLocal() {
        mSession = mSettingHelper.getAccountSession();
        mLabelCode = mSettingHelper.getAccountLabelCode();
        mPassword = mSettingHelper.getAccountPassword();
        mUserId = mSettingHelper.getAccountUserId();
        mUpdateTime = mSettingHelper.getAccountUpdateTime();
    }

    private synchronized void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IAccountListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
    }

    private static class AlarmReceiver extends BroadcastReceiver {

        private final WeakReference<AccountManagerCore> mAccountManagerRef;

        public AlarmReceiver(AccountManagerCore accountManager) {
            super();
            mAccountManagerRef = new WeakReference<>(accountManager);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ACTION_UPDATE_LOGIN_INFO_ALARM)) {
                final AccountManagerCore accountManager = mAccountManagerRef.get();
                if (accountManager != null) {
                    accountManager.updateLoginInfo();
                }
            }
        }
    }
}
