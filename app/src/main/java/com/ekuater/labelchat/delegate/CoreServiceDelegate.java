
package com.ekuater.labelchat.delegate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.UploadCommand;
import com.ekuater.labelchat.coreservice.CoreService;
import com.ekuater.labelchat.coreservice.ICoreService;
import com.ekuater.labelchat.coreservice.ICoreServiceListener;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.notificationcenter.NotificationCenter;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.activity.base.ActivityStack;
import com.ekuater.labelchat.util.L;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A delegate class for CoreService, wrap the operation between client and
 * CoreService
 *
 * @author LinYong
 */
/* package */ class CoreServiceDelegate {

    private static final String TAG = CoreServiceDelegate.class.getSimpleName();

    private static volatile CoreServiceDelegate sInstance = null;

    private static synchronized void initInstance(Context context) {
        sInstance = new CoreServiceDelegate(context.getApplicationContext());
    }

    public synchronized static CoreServiceDelegate getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        sInstance.checkInit();

        return sInstance;
    }

    private interface ListenerNotifier {
        public void notify(ICoreServiceNotifier notifier);
    }

    private enum State {
        INIT, STARTING, RUNNING, STOPPING, STOPPED,
    }

    private final class CoreServiceListenerDelegate extends ICoreServiceListener.Stub {

        public CoreServiceListenerDelegate() {
        }

        @Override
        public void onTestText(String text) throws RemoteException {
            notifyOnTestText(text);
        }

        @Override
        public void onImConnected(int result) throws RemoteException {
            notifyOnImConnectResult(result);
        }

        @Override
        public void onNewChatMessageReceived(ChatMessage chatMsg) throws RemoteException {
            notifyOnNewChatMessageReceived(chatMsg);
        }

        @Override
        public void onChatMessageSendResult(String messageSession, long messageId, int result)
                throws RemoteException {
            notifyOnChatMessageSendResult(messageSession, messageId, result);
        }

        @Override
        public void onNewChatMessageSending(String messageSession, long messageId)
                throws RemoteException {
            notifyOnNewChatMessageSending(messageSession, messageId);
        }

        @Override
        public void onAccountLogin(int result) throws RemoteException {
            notifyOnAccountLogin(result);
        }

        @Override
        public void onAccountLogout(int result) throws RemoteException {
            notifyOnAccountLogout(result);
        }

        @Override
        public void onAccountRegistered(int result) throws RemoteException {
            notifyOnAccountRegistered(result);
        }

        @Override
        public void onAccountPersonalInfoUpdated(int result) throws RemoteException {
            notifyOnAccountPersonalInfoUpdated(result);
        }

        @Override
        public void onAccountLoginInOtherClient() throws RemoteException {
            notifyOnAccountLoginInOtherClient();
        }

        @Override
        public void onAccountOAuthBindAccount(int result) throws RemoteException {
            notifyOnAccountOAuthBindAccount(result);
        }

        @Override
        public void onCommandExecuteResponse(String commandSession, int result, String response)
                throws RemoteException {
            notifyOnCommandExecuteResponse(commandSession, result, response);
        }

        @Override
        public void onUserLabelUpdated() throws RemoteException {
            notifyOnUserLabelsUpdated();
        }

        @Override
        public void onUserLabelAdded(int result) throws RemoteException {
            notifyOnUserLabelsAdded(result);
        }

        @Override
        public void onUserLabelDeleted(int result) throws RemoteException {
            notifyOnUserLabelsDeleted(result);
        }

        @Override
        public void onNetworkAvailableChanged(boolean networkAvailable) {
            notifyOnNetworkAvailableChanged(networkAvailable);
        }

        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            notifyOnNewSystemPushReceived(systemPush);
        }

        @Override
        public void onNewContactAdded(UserContact contact) throws RemoteException {
            notifyOnNewContactAdded(contact);
        }

        @Override
        public void onContactUpdated(UserContact contact) throws RemoteException {
            notifyOnContactUpdated(contact);
        }

        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId,
                                               String friendRemark) throws RemoteException {
            notifyOnModifyFriendRemarkResult(result, friendUserId, friendRemark);
        }

        @Override
        public void onDeleteFriendResult(int result, String friendUserId, String friendLabelCode)
                throws RemoteException {
            notifyOnDeleteFriendResult(result, friendUserId, friendLabelCode);
        }

        @Override
        public void onContactDefriendedMe(String friendUserId) throws RemoteException {
            notifyNotifiers(new ContactDefriendedMeNotifier(friendUserId));
        }

        @Override
        public void onCreateTmpGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group)
                throws RemoteException {
            notifyNotifiers(new CreateTmpGroupRequestResultNotifier(result, labels, group));
        }

        @Override
        public void onDismissTmpGroupRequestResult(int result, String groupId)
                throws RemoteException {
            notifyNotifiers(new DismissTmpGroupRequestResultNotifier(result, groupId));
        }

        @Override
        public void onQueryTmpGroupInfoResult(int result, String groupId, TmpGroup group)
                throws RemoteException {
            notifyNotifiers(new QueryTmpGroupInfoResultNotifier(result, groupId, group));
        }

        @Override
        public void onQuitTmpGroupResult(int result, String groupId) throws RemoteException {
            notifyNotifiers(new QuitTmpGroupResultNotifier(result, groupId));
        }

        @Override
        public void onQueryTmpGroupSystemTimeResult(int result, String groupId,
                                                    TmpGroupTime groupTime)
                throws RemoteException {
            notifyNotifiers(new QueryTmpGroupSystemTimeResultNotifier(result,
                    groupId, groupTime));
        }

        @Override
        public void onTmpGroupDismissRemind(String groupId, long timeRemaining) throws RemoteException {
            notifyNotifiers(new TmpGroupDismissRemindNotifier(groupId, timeRemaining));
        }

        @Override
        public void onJoinLabelChatRoomResult(String labelId, int result) throws RemoteException {
            notifyNotifiers(new JoinLabelChatRoomResultNotifier(labelId, result));
        }

        @Override
        public void onQuitLabelChatRoomResult(String labelId, int result) throws RemoteException {
            notifyNotifiers(new QuitLabelChatRoomResultNotifier(labelId, result));
        }

        @Override
        public void onJoinNormalChatRoomResult(String chatRoomId, int result) throws RemoteException {
            notifyNotifiers(new JoinNormalChatRoomResultNotifier(chatRoomId, result));
        }

        @Override
        public void onQuitNormalChatRoomResult(String chatRoomId, int result) throws RemoteException {
            notifyNotifiers(new QuitNormalChatRoomResultNotifier(chatRoomId, result));
        }

        @Override
        public void onUserTagUpdated() throws RemoteException {
            notifyNotifiers(new ListenerNotifier() {
                @Override
                public void notify(ICoreServiceNotifier notifier) {
                    notifier.onUserTagUpdated();
                }
            });
        }

        @Override
        public void onSetUserTagResult(final int result) throws RemoteException {
            notifyNotifiers(new ListenerNotifier() {
                @Override
                public void notify(ICoreServiceNotifier notifier) {
                    notifier.onSetUserTagResult(result);
                }
            });
        }

        @Override
        public void onFollowUserDataChanged() throws RemoteException {
            notifyNotifiers(new ListenerNotifier() {
                @Override
                public void notify(ICoreServiceNotifier notifier) {
                    notifier.onFollowUserDataChanged();
                }
            });
        }
    }

    private final class CoreServiceDeathRecipient implements IBinder.DeathRecipient {

        @Override
        public void binderDied() {
            notifyCoreServiceDied();
        }
    }

    private final Context mContext;
    private final FileUploader mFileUploader;
    private CoreServiceWrapper mCoreService;
    private final NotificationCenter mNotificationCenter;
    private final ICoreServiceListener mCoreServiceListener = new CoreServiceListenerDelegate();
    private final CoreServiceDeathRecipient mCoreServiceDeathRecipient = new CoreServiceDeathRecipient();
    private final List<WeakReference<ICoreServiceNotifier>> mNotifiers = new ArrayList<>();
    private final Map<String, ICommandResponseHandler> mCommandHandlerMap = new HashMap<>();

    private final ServiceConnection mCoreConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = new CoreServiceWrapper(
                    ICoreService.Stub.asInterface(service));
            try {
                mCoreService.asBinder().linkToDeath(mCoreServiceDeathRecipient, 0);
            } catch (RemoteException rex) {
                L.e(TAG, "Failed to link to listener death");
            }
            mCoreService.registerListener(mCoreServiceListener);
            mState = State.RUNNING;
            notifyCoreServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService = null;
            mState = State.STOPPED;
            checkInit();
        }
    };

    private Handler mHandler;
    private State mState;

    private CoreServiceDelegate(Context context) {
        // use as getInstance()
        mContext = context;
        mFileUploader = new FileUploader(context);
        mNotificationCenter = NotificationCenter.getInstance(mContext);
        mHandler = new Handler(Looper.getMainLooper());
        mState = State.INIT;
        checkInit();
    }

    private synchronized void init() {
        mNotificationCenter.checkInit();
        startService();
        bindService();
        mState = State.STARTING;
    }

    private void checkInit() {
        switch (mState) {
            case INIT:
            case STOPPED:
                init();
                break;
            default:
                break;
        }
    }

    private void notifyNotifiers(ListenerNotifier listenerNotifier) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                listenerNotifier.notify(notifier);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyCoreServiceConnected() {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onCoreServiceConnected();
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyCoreServiceDied() {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onCoreServiceDied();
            } else {
                mNotifiers.remove(i);
            }
        }

        clearCommandHandlers();

        // Now restart CoreService
        unbindService();
        startService();
        bindService();
    }

    private void notifyOnTestText(String text) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onTestText(text);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnImConnectResult(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onImConnected(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnNewChatMessageReceived(ChatMessage chatMsg) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onNewChatMessageReceived(chatMsg);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnChatMessageSendResult(String messageSession, long messageId, int result) {
        L.v(TAG, "notifyOnChatMessageSendResult()"
                + ",messageSession=" + messageSession
                + ",messageId=" + messageId
                + ",result=" + result);
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onChatMessageSendResult(messageSession, messageId, result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnNewChatMessageSending(String messageSession, long messageId) {
        L.v(TAG, "notifyOnNewChatMessageSending()"
                + ",messageSession=" + messageSession
                + ",messageId=" + messageId);
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onNewChatMessageSending(messageSession, messageId);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnAccountLogin(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountLogin(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnAccountLogout(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountLogout(result);
            } else {
                mNotifiers.remove(i);
            }
        }

        if (mState == State.STOPPING) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    exitAppAfterLogout();
                }
            });
        }
    }

    private void notifyOnAccountRegistered(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountRegistered(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnAccountPersonalInfoUpdated(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountPersonalInfoUpdated(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnAccountLoginInOtherClient() {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountLoginInOtherClient();
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnAccountOAuthBindAccount(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onAccountOAuthBindAccount(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnCommandExecuteResponse(String commandSession, int result, String response) {
        executeCommandResponse(commandSession, result, response);
    }

    private void notifyOnUserLabelsUpdated() {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onUserLabelUpdated();
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnUserLabelsAdded(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onUserLabelAdded(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnUserLabelsDeleted(int result) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onUserLabelDeleted(result);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnNetworkAvailableChanged(boolean networkAvailable) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onNetworkAvailableChanged(networkAvailable);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnNewSystemPushReceived(SystemPush systemPush) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onNewSystemPushReceived(systemPush);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnNewContactAdded(UserContact contact) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onNewContactAdded(contact);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnContactUpdated(UserContact contact) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onContactUpdated(contact);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnModifyFriendRemarkResult(int result, String friendUserId,
                                                  String friendRemark) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onModifyFriendRemarkResult(result, friendUserId, friendRemark);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void notifyOnDeleteFriendResult(int result, String friendUserId,
                                            String friendLabelCode) {
        for (int i = mNotifiers.size() - 1; i >= 0; i--) {
            ICoreServiceNotifier notifier = mNotifiers.get(i).get();
            if (notifier != null) {
                notifier.onDeleteFriendResult(result, friendUserId, friendLabelCode);
            } else {
                mNotifiers.remove(i);
            }
        }
    }

    private void startService() {
        mContext.startService(new Intent(mContext, CoreService.class));
    }

    private void stopService() {
        mContext.stopService(new Intent(mContext, CoreService.class));
    }

    private void bindService() {
        if (mCoreService == null) {
            mContext.bindService(new Intent(mContext, CoreService.class), mCoreConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        if (mCoreService != null) {
            mCoreService.unregisterListener(mCoreServiceListener);
            IBinder binder = mCoreService.asBinder();
            if (binder != null) {
                binder.unlinkToDeath(mCoreServiceDeathRecipient, 0);
            }
            mCoreService = null;
            mContext.unbindService(mCoreConnection);
        }
        clearCommandHandlers();
    }

    private void exitAppAfterLogout() {
        mNotificationCenter.exit();
        mCoreService.clearAllData();
        unbindService();
        stopService();
        ActivityStack.getInstance().finishAllActivity();
        mState = State.STOPPED;
        System.exit(0);

    }

    public void exitApp() {
        if (mState == State.STOPPING || mState == State.STOPPED) {
            return;
        }
        SettingHelper settingHelper = SettingHelper.getInstance(mContext);
        settingHelper.setManualExitApp(true);
        mState = State.STOPPING;
        accountLogout();
        settingHelper.setAccountSession("");
        settingHelper.setAccountLabelCode("");
        settingHelper.setAccountPassword("");
        settingHelper.setChatBackground("");
        settingHelper.setUserTheme("");
        settingHelper.setAccountAvatarThumb("");
        settingHelper.setAccountNickname("");
    }

    /**
     * Check CoreService is now available or not
     *
     * @return available or not
     */
    public boolean available() {
        return (mCoreService != null);
    }

    /**
     * Register callback to listen CoreService response
     *
     * @param notifier {@link ICoreServiceNotifier}
     */
    public void registerNotifier(final ICoreServiceNotifier notifier) {
        synchronized (mNotifiers) {
            for (WeakReference<ICoreServiceNotifier> ref : mNotifiers) {
                if (ref.get() == notifier) {
                    return;
                }
            }

            mNotifiers.add(new WeakReference<>(notifier));
            unregisterNotifier(null);
        }
    }

    /**
     * Unregister callback from CoreService
     *
     * @param notifier {@link ICoreServiceNotifier}
     */
    public void unregisterNotifier(final ICoreServiceNotifier notifier) {
        synchronized (mNotifiers) {
            for (int i = mNotifiers.size() - 1; i >= 0; i--) {
                if (mNotifiers.get(i).get() == notifier) {
                    mNotifiers.remove(i);
                }
            }
        }
    }

    public void doUpload(UploadCommand command, IUploadResponseHandler handler)
            throws FileNotFoundException {
        mFileUploader.doUpload(command, handler);
    }

    /**
     * for test
     */
    public void requestTestText() {
        if (mCoreService != null) {
            mCoreService.requestTestText();
        }
    }

    /**
     * account login
     *
     * @param user     account user name
     * @param password account password
     */
    public void accountLogin(String user, String password) {
        if (mCoreService != null) {
            mCoreService.accountLogin(user, password);
        }
    }

    /**
     * automatically login on current active account
     */
    public void accountAutomaticLogin() {
        if (mCoreService != null) {
            mCoreService.accountAutomaticLogin();
        }
    }

    /**
     * register a new account
     *
     * @param mobile     mobile number
     * @param verifyCode verify code from server
     * @param password   password
     * @param nickname   initialize nickname
     * @param gender     user gender
     */
    public void accountRegister(String mobile, String verifyCode, String password,
                                String nickname, int gender) {
        if (mCoreService != null) {
            mCoreService.accountRegister(mobile, verifyCode, password, nickname, gender);
        }
    }

    /**
     * update current account's settings to server
     *
     * @param newInfo new personal information to be updated.
     */
    public void accountUpdatePersonalInfo(PersonalUpdateInfo newInfo) {
        if (mCoreService != null) {
            mCoreService.accountUpdatePersonalInfo(newInfo);
        }
    }

    /**
     * Third platform OAuth login
     *
     * @param platform third platform
     * @param openId   open id
     * @param userInfo user information get from third platform
     */
    public void accountOAuthLogin(String platform, String openId, String accessToken,
                                  String tokenExpire, PersonalUpdateInfo userInfo) {
        if (mCoreService != null) {
            mCoreService.accountOAuthLogin(platform, openId, accessToken, tokenExpire, userInfo);
        }
    }

    /**
     * Convert third platform user to our own user by mobile
     *
     * @param mobile      mobile number
     * @param verifyCode  verify code from server
     * @param newPassword new password
     */
    public void accountOAuthBindAccount(String mobile, String verifyCode, String newPassword) {
        if (mCoreService != null) {
            mCoreService.accountOAuthBindAccount(mobile, verifyCode, newPassword);
        }
    }

    /**
     * Logout current active account
     */
    public void accountLogout() {
        if (mCoreService != null) {
            mCoreService.accountLogout();
        }
    }

    /**
     * Get current account logon session
     *
     * @return current account logon session
     */
    public String accountGetSession() {
        return (mCoreService != null) ? mCoreService.accountGetSession() : null;
    }

    /**
     * Get current account user id
     *
     * @return current account user id
     */
    public String accountGetUserId() {
        return (mCoreService != null) ? mCoreService.accountGetUserId() : null;
    }

    /**
     * Get current account label code
     *
     * @return current account labelCode
     */
    public String accountGetLabelCode() {
        return (mCoreService != null) ? mCoreService.accountGetLabelCode() : null;
    }

    /**
     * Is now account login or not
     *
     * @return login or not
     */
    public boolean accountIsLogin() {
        return (mCoreService != null) && mCoreService.accountIsLogin();
    }

    /**
     * Is now account im server connected or not
     *
     * @return connected or not
     */
    public boolean accountIsImConnected() {
        return (mCoreService != null) && mCoreService.accountIsImConnected();
    }

    /**
     * Get current location information
     *
     * @return current location
     */
    public LocationInfo getCurrentLocationInfo() {
        return (mCoreService != null) ? mCoreService.getCurrentLocationInfo() : null;
    }

    /**
     * Request CoreService to send chat message
     *
     * @param messageSession message session to identify the sending chat
     *                       message
     * @param chatMessage    chat message to be sent
     */
    public void requestSendChatMessage(String messageSession, ChatMessage chatMessage) {
        if (mCoreService != null) {
            mCoreService.requestSendChatMessage(messageSession, chatMessage);
        }
    }

    /**
     * Request CoreService to send chat message
     *
     * @param messageSession message session to identify the sending chat
     *                       message
     * @param messageId      chat message id to be sent
     */
    public void requestReSendChatMessage(String messageSession, long messageId) {
        if (mCoreService != null) {
            mCoreService.requestReSendChatMessage(messageSession, messageId);
        }
    }

    /**
     * request delete a chat message by id
     *
     * @param messageId chat message id to be delete
     */
    public void deleteChatMessage(long messageId) {
        if (mCoreService != null) {
            mCoreService.deleteChatMessage(messageId);
        }
    }

    /**
     * request delete friend's chat message history
     *
     * @param userId friend's labelCode
     */
    public void clearFriendChatMessage(String userId) {
        if (mCoreService != null) {
            mCoreService.clearFriendChatMessage(userId);
        }
    }

    /**
     * request to clear all chat message history
     */
    public void clearAllChatMessage() {
        if (mCoreService != null) {
            mCoreService.clearAllChatMessage();
        }
    }

    /**
     * request to clear all data
     */
    public void claarAllData() {
        if (mCoreService != null) {
            mCoreService.clearAllData();
        }
    }

    /**
     * Request CoreService to execute the command
     *
     * @param command the command to be executed
     * @param handler command execute response handler
     * @return command execution session
     */
    public String executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        if (mCoreService != null) {
            RequestCommand request = addExecuteCommand(command, handler);
            mCoreService.executeCommand(request);
            return request.getSession();
        } else {
            return null;
        }
    }

    /**
     * Request CoreService to execute the command
     *
     * @param request the command request to be executed
     * @param handler command execute response handler
     * @return command execution session
     */
    public String executeCommand(RequestCommand request, ICommandResponseHandler handler) {
        if (mCoreService != null) {
            mCoreService.executeCommand(addExecuteCommand(request, handler));
            return request.getSession();
        } else {
            return null;
        }
    }

    /**
     * Cancel a command which is being executing
     *
     * @param commandSession command execution session
     */
    @SuppressWarnings("UnusedDeclaration")
    public void cancelCommand(String commandSession) {
        removeExecuteCommand(commandSession);
    }

    private void executeCommandResponse(String commandSession, int result, String response) {
        ICommandResponseHandler responseHandler = removeExecuteCommand(commandSession);
        executeCommandResponse(responseHandler, result, response);
    }

    private void executeCommandResponse(ICommandResponseHandler responseHandler,
                                        int result, String response) {
        if (responseHandler != null) {
            try {
                responseHandler.onResponse(result, response);
            } catch (Exception e) {
                L.w(TAG, e);
            }
        }
    }

    private void clearCommandHandlers() {
        synchronized (mCommandHandlerMap) {
            for (ICommandResponseHandler handler : mCommandHandlerMap.values()) {
                executeCommandResponse(handler, ConstantCode.EXECUTE_RESULT_NETWORK_ERROR, null);
            }
            mCommandHandlerMap.clear();
        }
    }

    private RequestCommand addExecuteCommand(BaseCommand command, ICommandResponseHandler handler) {
        return addExecuteCommand(command.toRequestCommand(), handler);
    }

    private RequestCommand addExecuteCommand(RequestCommand request, ICommandResponseHandler handler) {
        if (handler != null) {
            synchronized (mCommandHandlerMap) {
                mCommandHandlerMap.put(request.getSession(), handler);
            }
        }

        return request;
    }

    private ICommandResponseHandler removeExecuteCommand(String commandSession) {
        synchronized (mCommandHandlerMap) {
            return mCommandHandlerMap.remove(commandSession);
        }
    }

    /**
     * Add labels for current account
     *
     * @param labels label names to be added
     */
    public void labelAddUserLabels(BaseLabel[] labels) {
        if (mCoreService != null) {
            mCoreService.labelAddUserLabels(labels);
        }
    }

    /**
     * Delete labels from current account
     *
     * @param labels labels to be deleted
     */
    public void labelDeleteUserLabels(UserLabel[] labels) {
        if (mCoreService != null) {
            mCoreService.labelDeleteUserLabels(labels);
        }
    }

    /**
     * Get all labels of current account
     *
     * @return labels of current account
     */
    public UserLabel[] labelGetAllUserLabels() {
        return (mCoreService != null) ? mCoreService.labelGetAllUserLabels() : null;
    }

    /**
     * Force get user labels from server
     */
    public void labelForceRefreshUserLabels() {
        if (mCoreService != null) {
            mCoreService.labelForceRefreshUserLabels();
        }
    }

    /**
     * Is now network available or not
     *
     * @return available or not
     */
    public boolean isNetworkAvailable() {
        return (mCoreService != null) && mCoreService.isNetworkAvailable();
    }

    /**
     * Delete push messages by type
     *
     * @param type push message type
     */
    public void deleteSystemPushByType(int type) {
        if (mCoreService != null) {
            mCoreService.deleteSystemPushByType(type);
        }
    }


    /**
     * Delete push messages by type
     *
     * @param type push message type
     */
    public void deleteLikeSystemPushByType(int type, String tag) {
        if (mCoreService != null) {
            mCoreService.deleteLikeSystemPushByType(type, tag);
        }
    }


    /**
     * Delete push messages by types
     *
     * @param types push message types
     */
    public void deleteSystemPushByTypes(int[] types) {
        if (mCoreService != null) {
            mCoreService.deleteSystemPushByTypes(types);
        }
    }

    /**
     * Delete push messages by flag
     *
     * @param flag push message flag
     */
    public void deletePushMessageByFlag(String flag) {
        if (mCoreService != null) {
            mCoreService.deleteSystemPushByFlag(flag);
        }
    }

    /**
     * Delete push messages by message id
     *
     * @param messageId push message id
     */
    public void deleteSystemPush(long messageId) {
        if (mCoreService != null) {
            mCoreService.deleteSystemPush(messageId);
        }
    }

    /**
     * Modify friend remark
     *
     * @param friendUserId friend userId
     * @param friendRemark new remark
     */
    public void modifyFriendRemark(String friendUserId, String friendRemark) {
        if (mCoreService != null) {
            mCoreService.modifyFriendRemark(friendUserId, friendRemark);
        }
    }

    /**
     * Delete a friend from contacts
     *
     * @param friendUserId    friend userId
     * @param friendLabelCode friend labelCode
     */
    public void deleteFriend(String friendUserId, String friendLabelCode) {
        if (mCoreService != null) {
            mCoreService.deleteFriend(friendUserId, friendLabelCode);
        }
    }

    /**
     * Update contact information
     *
     * @param contact new contact information
     */
    public void updateContact(UserContact contact) {
        if (mCoreService != null) {
            mCoreService.updateContact(contact);
        }
    }

    /**
     * Request to create a tmp group
     *
     * @param labels  group label
     * @param members members in group
     */
    public void tmpGroupCreateGroupRequest(BaseLabel[] labels, String[] members) {
        if (mCoreService != null) {
            mCoreService.tmpGroupCreateGroupRequest(labels, members);
        }
    }

    /**
     * Request to dismiss the group by group creator
     *
     * @param groupId groupId
     * @param reason  dismiss reason
     */
    public void tmpGroupDismissGroupRequest(String groupId, String reason) {
        if (mCoreService != null) {
            mCoreService.tmpGroupDismissGroupRequest(groupId, reason);
        }
    }

    /**
     * Request to get group information from server
     *
     * @param groupId groupId
     */
    public void tmpGroupQueryGroupInfo(String groupId) {
        if (mCoreService != null) {
            mCoreService.tmpGroupQueryGroupInfo(groupId);
        }
    }

    /**
     * Group member request quit the group
     *
     * @param groupId groupId of which group to quit
     */
    public void tmpGroupQuitGroup(String groupId) {
        if (mCoreService != null) {
            mCoreService.tmpGroupQuitGroup(groupId);
        }
    }

    /**
     * Get system time of group the synchronize the group expire time
     *
     * @param groupId groupId
     */
    public void tmpGroupQueryGroupSystemTime(String groupId) {
        if (mCoreService != null) {
            mCoreService.tmpGroupQueryGroupSystemTime(groupId);
        }
    }

    /**
     * Query group information from local database
     *
     * @param groupId groupId
     * @return group information
     */
    public TmpGroup tmpGroupQueryGroup(String groupId) {
        if (mCoreService != null) {
            return mCoreService.tmpGroupQueryGroup(groupId);
        } else {
            return null;
        }
    }

    /**
     * Query all group ids from local database
     *
     * @return all group id array
     */
    public String[] tmpGroupQueryAllGroupId() {
        if (mCoreService != null) {
            return mCoreService.tmpGroupQueryAllGroupId();
        } else {
            return null;
        }
    }

    /**
     * Query group members from local database
     *
     * @param groupId groupId
     * @return group member array
     */
    public Stranger[] tmpGroupQueryGroupMembers(String groupId) {
        if (mCoreService != null) {
            return mCoreService.tmpGroupQueryGroupMembers(groupId);
        } else {
            return null;
        }
    }

    /**
     * Add or update stranger detail information to StrangerStore
     *
     * @param stranger new stranger detail information
     */
    public void addStranger(Stranger stranger) {
        if (mCoreService != null) {
            mCoreService.addStranger(stranger);
        }
    }

    /**
     * Get stranger detail information from StrangerStore
     *
     * @param userId stranger userId
     * @return stranger detail information
     */
    public Stranger getStranger(String userId) {
        if (mCoreService != null) {
            return mCoreService.getStranger(userId);
        } else {
            return null;
        }
    }

    /**
     * Delete stranger from StrangerStore
     *
     * @param userId stranger userId
     */
    public void deleteStranger(String userId) {
        if (mCoreService != null) {
            mCoreService.deleteStranger(userId);
        }
    }

    /**
     * Add or update lite stranger detail information to LiteStrangerStore
     *
     * @param stranger new stranger detail information
     */
    public void addLiteStranger(LiteStranger stranger) {
        if (mCoreService != null) {
            mCoreService.addLiteStranger(stranger);
        }
    }

    /**
     * Get lite stranger detail information from LiteStrangerStore
     *
     * @param userId stranger userId
     * @return stranger detail information
     */
    public LiteStranger getLiteStranger(String userId) {
        if (mCoreService != null) {
            return mCoreService.getLiteStranger(userId);
        } else {
            return null;
        }
    }

    /**
     * Delete lite stranger from LiteStrangerStore
     *
     * @param userId stranger userId
     */
    public void deleteLiteStranger(String userId) {
        if (mCoreService != null) {
            mCoreService.deleteLiteStranger(userId);
        }
    }

    /**
     * Join label chat room
     *
     * @param labelId label ID
     */
    public void joinLabelChatRoom(String labelId) {
        if (mCoreService != null) {
            mCoreService.joinLabelChatRoom(labelId);
        }
    }

    /**
     * Join label chat room
     *
     * @param labelId label ID
     */
    public void quitLabelChatRoom(String labelId) {
        if (mCoreService != null) {
            mCoreService.quitLabelChatRoom(labelId);
        }
    }

    /**
     * Join normal chat room
     *
     * @param chatRoomId normal chat room id
     */
    public void joinNormalChatRoom(String chatRoomId) {
        if (mCoreService != null) {
            mCoreService.joinNormalChatRoom(chatRoomId);
        }
    }

    /**
     * Join normal chat room
     *
     * @param chatRoomId normal chat room id
     */
    public void quitNormalChatRoom(String chatRoomId) {
        if (mCoreService != null) {
            mCoreService.quitNormalChatRoom(chatRoomId);
        }
    }

    public UserTag[] tagGetUserTags() {
        return (mCoreService != null) ? mCoreService.tagGetUserTags() : null;
    }

    public void tagSetUserTags(UserTag[] tags) {
        if (mCoreService != null) {
            mCoreService.tagSetUserTags(tags);
        }
    }

    public void tagSetUserTags(UserTag[] tags, String[] userIds, String tagId) {
        if (mCoreService != null) {
            mCoreService.tagSetUserTagsUser(tags, userIds, tagId);
        }
    }

    public FollowUser getFollowingUser(String userId) {
        return mCoreService != null ? mCoreService.getFollowingUser(userId) : null;
    }

    public FollowUser[] getAllFollowingUser() {
        return mCoreService != null ? mCoreService.getAllFollowingUser() : null;
    }

    public void addFollowingUser(FollowUser followUser) {
        if (mCoreService != null) {
            mCoreService.addFollowingUser(followUser);
        }
    }

    public void deleteFollowingUser(String userId) {
        if (mCoreService != null) {
            mCoreService.deleteFollowingUser(userId);
        }
    }

    public FollowUser getFollowerUser(String userId) {
        return mCoreService != null ? mCoreService.getFollowerUser(userId) : null;
    }

    public FollowUser[] batchQueryFollowerUser(String[] userIds) {

        return mCoreService != null ? mCoreService.batchQueryFollowerUser(userIds) : null;
    }

    public FollowUser[] getAllFollowerUser() {
        return mCoreService != null ? mCoreService.getAllFollowerUser() : null;
    }

    public void addFollowerUser(FollowUser followUser) {
        if (mCoreService != null) {
            mCoreService.addFollowerUser(followUser);
        }
    }

    public void deleteFollowerUser(String userId) {
        if (mCoreService != null) {
            mCoreService.deleteFollowerUser(userId);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        unbindService();
    }

    private static class ContactDefriendedMeNotifier implements ListenerNotifier {

        private final String mFriendUserId;

        public ContactDefriendedMeNotifier(String friendUserId) {
            mFriendUserId = friendUserId;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onContactDefriendedMe(mFriendUserId);
        }
    }

    private static class CreateTmpGroupRequestResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final BaseLabel[] mLabels;
        private final TmpGroup mGroup;

        public CreateTmpGroupRequestResultNotifier(int result, BaseLabel[] labels, TmpGroup group) {
            mResult = result;
            mLabels = labels;
            mGroup = group;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onCreateTmpGroupRequestResult(mResult, mLabels, mGroup);
        }
    }

    private static class DismissTmpGroupRequestResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public DismissTmpGroupRequestResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onDismissTmpGroupRequestResult(mResult, mGroupId);
        }
    }

    private static class QueryTmpGroupInfoResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;
        private final TmpGroup mGroup;

        public QueryTmpGroupInfoResultNotifier(int result, String groupId, TmpGroup group) {
            mResult = result;
            mGroupId = groupId;
            mGroup = group;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onQueryTmpGroupInfoResult(mResult, mGroupId, mGroup);
        }
    }

    private static class QuitTmpGroupResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public QuitTmpGroupResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onQuitTmpGroupResult(mResult, mGroupId);
        }
    }

    private static class QueryTmpGroupSystemTimeResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;
        private final TmpGroupTime mGroupTime;

        public QueryTmpGroupSystemTimeResultNotifier(int result, String groupId,
                                                     TmpGroupTime groupTime) {
            mResult = result;
            mGroupId = groupId;
            mGroupTime = groupTime;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onQueryTmpGroupSystemTimeResult(mResult, mGroupId, mGroupTime);
        }
    }

    private static class TmpGroupDismissRemindNotifier implements ListenerNotifier {

        private final String mGroupId;
        private final long mTimeRemaining;

        public TmpGroupDismissRemindNotifier(String groupId, long timeRemaining) {
            mGroupId = groupId;
            mTimeRemaining = timeRemaining;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onTmpGroupDismissRemind(mGroupId, mTimeRemaining);
        }
    }

    private static class JoinLabelChatRoomResultNotifier implements ListenerNotifier {

        private final String labelId;
        private final int result;

        public JoinLabelChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onJoinLabelChatRoomResult(labelId, result);
        }
    }

    private static class QuitLabelChatRoomResultNotifier implements ListenerNotifier {

        private final String labelId;
        private final int result;

        public QuitLabelChatRoomResultNotifier(String labelId, int result) {
            this.labelId = labelId;
            this.result = result;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onQuitLabelChatRoomResult(labelId, result);
        }
    }

    private static class JoinNormalChatRoomResultNotifier implements ListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public JoinNormalChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onJoinNormalChatRoomResult(chatRoomId, result);
        }
    }

    private static class QuitNormalChatRoomResultNotifier implements ListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public QuitNormalChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(ICoreServiceNotifier notifier) {
            notifier.onQuitNormalChatRoomResult(chatRoomId, result);
        }
    }
}
