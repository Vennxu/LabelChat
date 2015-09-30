
package com.ekuater.labelchat.coreservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.coreservice.account.AccountManagerCore;
import com.ekuater.labelchat.coreservice.account.IAccountListener;
import com.ekuater.labelchat.coreservice.chatmessage.ChatMessageStore;
import com.ekuater.labelchat.coreservice.chatmessage.IChatMessageStoreListener;
import com.ekuater.labelchat.coreservice.command.CommandProcessor;
import com.ekuater.labelchat.coreservice.command.ICommandProcessListener;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.command.InternalCommandProcessor;
import com.ekuater.labelchat.coreservice.contacts.ContactsStore;
import com.ekuater.labelchat.coreservice.contacts.IContactsListener;
import com.ekuater.labelchat.coreservice.following.FollowUserStore;
import com.ekuater.labelchat.coreservice.immediator.BaseIMMediator;
import com.ekuater.labelchat.coreservice.immediator.IIMListener;
import com.ekuater.labelchat.coreservice.immediator.IMMediatorHelper;
import com.ekuater.labelchat.coreservice.litestrangers.LiteStrangerStore;
import com.ekuater.labelchat.coreservice.location.LocationSender;
import com.ekuater.labelchat.coreservice.pushmediator.BasePushMediator;
import com.ekuater.labelchat.coreservice.pushmediator.PushMediatorHelper;
import com.ekuater.labelchat.coreservice.strangers.StrangerStore;
import com.ekuater.labelchat.coreservice.systempush.ISystemPushListener;
import com.ekuater.labelchat.coreservice.systempush.SystemPushStore;
import com.ekuater.labelchat.coreservice.tags.ITagsListener;
import com.ekuater.labelchat.coreservice.tags.TagsStore;
import com.ekuater.labelchat.coreservice.tmpgroup.ITmpGroupListener;
import com.ekuater.labelchat.coreservice.tmpgroup.TmpGroupMediator;
import com.ekuater.labelchat.coreservice.utils.TaskExecutor;
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
import com.ekuater.labelchat.guard.GuardConst;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Core service, most of business will be process here.
 *
 * @author LinYong
 */
public class CoreService extends Service {

    private static final String TAG = "service.CoreService";
    private static final String EMPTY_STRING = "";

    // Handler message
    // Request message
    private static final int MSG_REQUEST_TEST_TEXT = 1;
    private static final int MSG_REQUEST_SEND_CHAT_MESSAGE = 2;
    private static final int MSG_REQUEST_RESEND_CHAT_MESSAGE = 3;

    // Notify message from communication layer
    private static final int MSG_NOTIFY_NEW_MESSAGE = 101;
    private static final int MSG_NOTIFY_MESSAGE_SEND_RESULT = 102;

    private final class CoreServiceBinderListener implements IBinder.DeathRecipient {

        final public ICoreServiceListener listener;

        public CoreServiceBinderListener(ICoreServiceListener listener) {
            this.listener = listener;
        }

        @Override
        public void binderDied() {
            synchronized (mListeners) {
                mListeners.remove(this);
                listener.asBinder().unlinkToDeath(this, 0);
            }
        }
    }

    private interface ListenerNotifier {
        public void notify(CoreServiceBinderListener listener) throws RemoteException;
    }

    private static final class ProcessHandler extends Handler {

        private CoreService mCoreService;

        public ProcessHandler(Looper looper, CoreService coreService) {
            super(looper);
            mCoreService = coreService;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REQUEST_TEST_TEXT:
                    mCoreService.handleTestText();
                    break;
                case MSG_REQUEST_SEND_CHAT_MESSAGE:
                    mCoreService.handleSendChatMessage(msg.obj);
                    break;
                case MSG_REQUEST_RESEND_CHAT_MESSAGE:
                    mCoreService.handleReSendChatMessage(msg.obj);
                    break;
                case MSG_NOTIFY_NEW_MESSAGE:
                    mCoreService.handleNewChatMessageReceived(msg.obj);
                    break;
                case MSG_NOTIFY_MESSAGE_SEND_RESULT:
                    mCoreService.handleChatMessageSendResult(msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final ICoreService.Stub mBinder = new ICoreService.Stub() {

        @Override
        public void registerListener(ICoreServiceListener listener) throws RemoteException {
            synchronized (mListeners) {
                CoreServiceBinderListener bl = new CoreServiceBinderListener(listener);
                try {
                    listener.asBinder().linkToDeath(bl, 0);
                    mListeners.add(bl);
                } catch (RemoteException rex) {
                    L.e(TAG, "Failed to link to listener death");
                }
            }
        }

        @Override
        public void unregisterListener(ICoreServiceListener listener) throws RemoteException {
            synchronized (mListeners) {
                for (CoreServiceBinderListener bl : mListeners) {
                    if (bl.listener.asBinder().equals(listener.asBinder())) {
                        mListeners.remove(mListeners.indexOf(bl));
                        listener.asBinder().unlinkToDeath(bl, 0);
                    }
                }
            }
        }

        @Override
        public void requestTestText() throws RemoteException {
            onRequestTestText();
        }

        @Override
        public void accountLogin(String user, String password) throws RemoteException {
            mAccountManager.login(user, password);
        }

        @Override
        public void accountAutomaticLogin() throws RemoteException {
            mAccountManager.automaticLogin();
        }

        @Override
        public void accountRegister(String mobile, String verifyCode, String password,
                                    String nickname, int gender)
                throws RemoteException {
            mAccountManager.register(mobile, verifyCode, password, nickname, gender);
        }

        @Override
        public void accountUpdatePersonalInfo(PersonalUpdateInfo newInfo)
                throws RemoteException {
            mAccountManager.updatePersonalInfo(newInfo);
        }

        @Override
        public void accountOAuthLogin(String platform, String openId, String accessToken,
                                      String tokenExpire, PersonalUpdateInfo userInfo)
                throws RemoteException {
            mAccountManager.oAuthLogin(platform, openId, accessToken, tokenExpire, userInfo);
        }

        @Override
        public void accountOAuthBindAccount(String mobile, String verifyCode, String newPassword)
                throws RemoteException {
            mAccountManager.oAuthBindAccount(mobile, verifyCode, newPassword);
        }

        @Override
        public void accountLogout() throws RemoteException {
            mAccountManager.logout();
        }

        @Override
        public String accountGetSession() throws RemoteException {
            return mAccountManager.getSession();
        }

        @Override
        public String accountGetUserId() throws RemoteException {
            return mAccountManager.getUserId();
        }

        @Override
        public String accountGetLabelCode() throws RemoteException {
            return mAccountManager.getLabelCode();
        }

        @Override
        public boolean accountIsLogin() throws RemoteException {
            return mAccountManager.isLogin();
        }

        @Override
        public boolean accountIsImConnected() throws RemoteException {
            return (mImConnectResult == ConstantCode.IM_CONNECT_SUCCESS);
        }

        @Override
        public LocationInfo getCurrentLocationInfo() throws RemoteException {
            return mLocationSender.getLocation();
        }

        @Override
        public void requestSendChatMessage(String messageSession, ChatMessage chatMessage)
                throws RemoteException {
            onRequestSendChatMessage(messageSession, chatMessage);
        }

        @Override
        public void requestReSendChatMessage(String messageSession, long messageId)
                throws RemoteException {
            onRequestReSendChatMessage(messageSession, messageId);
        }

        @Override
        public void deleteChatMessage(long messageId) throws RemoteException {
            mChatMessageStore.deleteByMessageId(messageId);
        }

        @Override
        public void clearFriendChatMessage(String userId) throws RemoteException {
            mChatMessageStore.clear(userId);
        }

        @Override
        public void clearAllChatMessage() throws RemoteException {
            mChatMessageStore.clear();
        }

        @Override
        public void executeCommand(RequestCommand command) throws RemoteException {
            final String commandSession = command.getSession();

            if (TextUtils.isEmpty(command.getUrl())) {
                notifyOnCommandExecuteResponse(commandSession,
                        ConstantCode.EXECUTE_RESULT_EMPTY_CMD, EMPTY_STRING);
            } else if (TextUtils.isEmpty(command.getParam())) {
                notifyOnCommandExecuteResponse(commandSession,
                        ConstantCode.EXECUTE_RESULT_EMPTY_PARAM, EMPTY_STRING);
            } else {
                mCommandProcessor.executeCommand(command);
            }
        }

        @Override
        public void labelAddUserLabels(BaseLabel[] labels) throws RemoteException {
        }

        @Override
        public void labelDeleteUserLabels(UserLabel[] labels) throws RemoteException {
        }

        @Override
        public UserLabel[] labelGetAllUserLabels() throws RemoteException {
            return null;
        }

        @Override
        public void labelForceRefreshUserLabels() throws RemoteException {
        }

        @Override
        public boolean isNetworkAvailable() throws RemoteException {
            return mNetworkMonitor.isNetworkAvailable();
        }

        @Override
        public void deleteSystemPushByType(int type) {
            mSystemPushStore.deleteSystemPushByType(type);
        }

        @Override
        public void deleteLikeSystemPushByType(int type, String tag) {
            mSystemPushStore.deleteLikeSystemPushByType(type, tag);
        }

        @Override
        public void deleteSystemPushByTypes(int[] types) throws RemoteException {
            mSystemPushStore.deleteSystemPushByTypes(types);
        }

        @Override
        public void deleteSystemPushByFlag(String flag) {
            mSystemPushStore.deleteSystemPushByFlag(flag);
        }

        @Override
        public void deleteSystemPush(long messageId) throws RemoteException {
            mSystemPushStore.deleteSystemPush(messageId);
        }

        @Override
        public void modifyFriendRemark(String friendUserId, String friendRemark)
                throws RemoteException {
            mContactsStore.modifyFriendRemark(friendUserId, friendRemark);
        }

        @Override
        public void deleteFriend(String friendUserId, String friendLabelCode)
                throws RemoteException {
            onDeleteFriend(friendUserId, friendLabelCode);
        }

        @Override
        public void updateContact(UserContact contact) throws RemoteException {
            mContactsStore.updateContact(contact);
        }

        @Override
        public void tmpGroupCreateGroupRequest(BaseLabel[] labels, String[] members)
                throws RemoteException {
            mTmpGroupMediator.createGroupRequest(labels, members);
        }

        @Override
        public void tmpGroupDismissGroupRequest(String groupId, String reason) throws RemoteException {
            mTmpGroupMediator.dismissGroupRequest(groupId, reason);
        }

        @Override
        public void tmpGroupQueryGroupInfo(String groupId) throws RemoteException {
            mTmpGroupMediator.queryGroupInfo(groupId);
        }

        @Override
        public void tmpGroupQuitGroup(String groupId) throws RemoteException {
            mTmpGroupMediator.quitGroup(groupId);
        }

        @Override
        public void tmpGroupQueryGroupSystemTime(String groupId) throws RemoteException {
            mTmpGroupMediator.queryGroupSystemTime(groupId);
        }

        @Override
        public TmpGroup tmpGroupQueryGroup(String groupId) throws RemoteException {
            return mTmpGroupMediator.queryGroup(groupId);
        }

        @Override
        public String[] tmpGroupQueryAllGroupId() throws RemoteException {
            return mTmpGroupMediator.queryAllGroupId();
        }

        @Override
        public Stranger[] tmpGroupQueryGroupMembers(String groupId) throws RemoteException {
            return mTmpGroupMediator.queryGroupMembers(groupId);
        }

        @Override
        public void addStranger(Stranger stranger) throws RemoteException {
            mStrangerStore.addStranger(stranger);
        }

        @Override
        public Stranger getStranger(String userId) throws RemoteException {
            return mStrangerStore.getStranger(userId);
        }

        @Override
        public void deleteStranger(String userId) throws RemoteException {
            mStrangerStore.deleteStranger(userId);
        }

        @Override
        public void addLiteStranger(LiteStranger stranger) throws RemoteException {
            mLiteStrangerStore.addStranger(stranger);
        }

        @Override
        public LiteStranger getLiteStranger(String userId) throws RemoteException {
            return mLiteStrangerStore.getStranger(userId);
        }

        @Override
        public void deleteLiteStranger(String userId) throws RemoteException {
            mLiteStrangerStore.deleteStranger(userId);
        }

        @Override
        public void joinLabelChatRoom(String labelId) throws RemoteException {
            mIMMediator.joinLabelChatRoom(labelId);
        }

        @Override
        public void quitLabelChatRoom(String labelId) throws RemoteException {
            mIMMediator.quitLabelChatRoom(labelId);
            mChatMessageStore.clear(labelId);
        }

        @Override
        public void joinNormalChatRoom(String chatRoomId) throws RemoteException {
            mIMMediator.joinNormalChatRoom(chatRoomId);
            mChatMessageStore.clear(chatRoomId);
        }

        @Override
        public void quitNormalChatRoom(String chatRoomId) throws RemoteException {
            mIMMediator.quitNormalChatRoom(chatRoomId);
            mChatMessageStore.clear(chatRoomId);
        }

        @Override
        public UserTag[] tagGetUserTags() throws RemoteException {
            return mTagsStore.query();
        }

        @Override
        public void tagSetUserTags(UserTag[] tags) throws RemoteException {
            mTagsStore.setTags(tags);
        }

        @Override
        public void tagSetUserTagsUser(UserTag[] tags, String[] userIds, String tagId) throws RemoteException {
            mTagsStore.setTags(tags, userIds, tagId);
        }

        @Override
        public FollowUser getFollowingUser(String userId) throws RemoteException {
            return mFollowUserStore.getFollowingUser(userId);
        }

        @Override
        public FollowUser[] getAllFollowingUser() throws RemoteException {
            return mFollowUserStore.getAllFollowingUser();
        }

        @Override
        public void addFollowingUser(FollowUser followUser) throws RemoteException {
            mFollowUserStore.addFollowingUser(followUser);
        }

        @Override
        public void deleteFollowingUser(String userId) throws RemoteException {
            mFollowUserStore.deleteFollowingUser(userId);
        }

        @Override
        public FollowUser getFollowerUser(String userId) throws RemoteException {
            return mFollowUserStore.getFollowerUser(userId);
        }

        @Override
        public FollowUser[] batchQueryFollowerUser(String[] userIds) throws RemoteException {
            return mFollowUserStore.batchQueryFollowerUser(userIds);
        }

        @Override
        public FollowUser[] getAllFollowerUser() throws RemoteException {
            return mFollowUserStore.getAllFollowerUser();
        }

        @Override
        public void addFollowerUser(FollowUser followUser) throws RemoteException {
            mFollowUserStore.addFollowerUser(followUser);
        }

        @Override
        public void deleteFollowerUser(String userId) throws RemoteException {
            mFollowUserStore.deleteFollowerUser(userId);
        }

        @Override
        public void clearAllData() throws RemoteException {
            mContactsStore.clear();
            mChatMessageStore.clear();
            mSystemPushStore.clear();
            mTagsStore.clear();
            mFollowUserStore.clear();
            mStrangerStore.clear();
            mListeners.clear();
        }
    };

    private final List<CoreServiceBinderListener> mListeners = new ArrayList<>();

    private EventBus mCoreEventBus = EventBusHub.getCoreEventBus();
    private CoreEventHandler mCoreEventHandler;
    private HandlerThread mProcessThread;
    private Handler mProcessHandler;
    private Handler mMainHandler;
    private SettingHelper mSettingHelper;
    private LocationSender mLocationSender;
    private BaseIMMediator mIMMediator;
    private ChatMessageStore mChatMessageStore;
    private SystemPushStore mSystemPushStore;
    private ContactsStore mContactsStore;
    private StrangerStore mStrangerStore;
    private LiteStrangerStore mLiteStrangerStore;
    private CommandProcessor mCommandProcessor;
    private InternalCommandProcessor mInternalCommandProcessor;
    private AccountManagerCore mAccountManager;
    private TaskExecutor mTaskExecutor;
    private NetworkMonitor mNetworkMonitor;
    private BasePushMediator mPushMediator;
    private TmpGroupMediator mTmpGroupMediator;
    private TagsStore mTagsStore;
    private FollowUserStore mFollowUserStore;

    private int mImConnectResult;

    private final IIMListener mIMListener = new IIMListener() {

        @Override
        public void onConnectResult(int result) {
            switch (result) {
                case ConstantCode.IM_CONNECT_SUCCESS:
                    break;
                case ConstantCode.IM_CONNECT_NETWORK_ERROR:
                    break;
                case ConstantCode.IM_CONNECT_AUTHENTICATE_FAILED:
                    break;
                default:
                    break;
            }
            notifyOnConnectResult(result);
        }

        @Override
        public void onChatMessageSendResult(ChatMessage chatMessage, int result) {
            notifyChatMessageSendResult(chatMessage, result);
        }

        @Override
        public void onNewChatMessageReceived(ChatMessage chatMessage) {
            notifyNewChatMessageReceived(chatMessage);
        }

        @Override
        public void onJoinLabelChatRoomResult(String labelId, int result) {
            notifyListeners(new JoinLabelChatRoomResultNotifier(labelId, result));
        }

        @Override
        public void onQuitLabelChatRoomResult(String labelId, int result) {
            notifyListeners(new QuitLabelChatRoomResultNotifier(labelId, result));
        }

        @Override
        public void onJoinNormalChatRoomResult(String chatRoomId, int result) {
            notifyListeners(new JoinNormalChatRoomResultNotifier(chatRoomId, result));
        }

        @Override
        public void onQuitNormalChatRoomResult(String chatRoomId, int result) {
            notifyListeners(new QuitNormalChatRoomResultNotifier(chatRoomId, result));
        }
    };

    private void notifyOnConnectResult(int result) {
        mImConnectResult = result;

        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onImConnected(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private final IChatMessageStoreListener mChatMessageStoreListener = new IChatMessageStoreListener() {

        @Override
        public void onNewMessageReceived(ChatMessage chatMsg) {
            notifyOnNewChatMessageReceived(chatMsg);
        }

        @Override
        public void onNewMessageSending(String messageSession, long messageId) {
            notifyOnNewChatMessageSending(messageSession, messageId);
        }

        @Override
        public void onMessageSendResult(String messageSession, long messageId, int result) {
            notifyOnChatMessageSendResult(messageSession, messageId, result);
        }
    };

    private final ISystemPushListener mSystemPushListener = new ISystemPushListener() {

        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            notifyOnNewSystemPushReceived(systemPush);
        }

        @Override
        public void onNewAccountPushReceived(SystemPush systemPush) {
            mAccountManager.onNewPushMessage(systemPush);
        }

        @Override
        public void onNewContactPushReceived(SystemPush systemPush) {
            mContactsStore.onNewPushMessage(systemPush);
        }

        @Override
        public void onNewTmpGroupPushReceived(SystemPush systemPush) {
            mTmpGroupMediator.onNewPushMessage(systemPush);
        }
    };

    private final IContactsListener mContactsListener = new IContactsListener() {
        @Override
        public void onNewContactAdded(UserContact contact) {
            notifyNewContactAdded(contact);
        }

        @Override
        public void onContactUpdated(UserContact contact) {
            notifyContactUpdated(contact);
        }

        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId,
                                               String friendRemark) {
            notifyModifyFriendRemarkResult(result, friendUserId, friendRemark);
        }

        @Override
        public void onDeleteFriendResult(int result, String friendUserId,
                                         String friendLabelCode) {
            notifyDeleteFriendResult(result, friendUserId, friendLabelCode);
        }

        @Override
        public void onContactDefriendedMe(String friendUserId) {
            notifyContactDefriendedMe(friendUserId);
        }
    };

    private final ICommandProcessListener mCommandProcessListener = new ICommandProcessListener() {

        @Override
        public void onSuccess(String cmdSession, String response) {
            notifyOnCommandExecuteResponse(cmdSession, ConstantCode.EXECUTE_RESULT_SUCCESS,
                    response);
        }

        @Override
        public void onFailure(String cmdSession, String response, int errorCode) {
            notifyOnCommandExecuteResponse(cmdSession,
                    ConstantCode.EXECUTE_RESULT_NETWORK_ERROR, response);
        }

        @Override
        public void onSessionInvalid(String cmdSession, String response) {
            mAccountManager.forceAutomaticLogin();
        }
    };

    private final IAccountListener mAccountListener = new IAccountListener() {

        @Override
        public void onLoginResult(int result, boolean accountChanged, boolean infoUpdated) {
            onAccountLoginResult(result, accountChanged, infoUpdated);
        }

        @Override
        public void onLogoutResult(int result) {
            onAccountLogoutResult(result);
        }

        @Override
        public void onRegisterResult(int result) {
            onAccountRegisterResult(result);
        }

        @Override
        public void onPersonalInfoUpdatedResult(int result) {
            onAccountPersonalInfoUpdatedResult(result);
        }

        @Override
        public void onLoginInOtherClient() {
            onAccountLoginInOtherClient();
        }

        @Override
        public void onOAuthBindAccountResult(int result) {
            onAccountOAuthBindAccountResult(result);
        }
    };

    private final INetworkListener mNetworkListener = new INetworkListener() {

        @Override
        public void networkAvailableChanged(boolean networkAvailable) {
            onNetworkAvailableChanged(networkAvailable);
        }
    };

    private final ITmpGroupListener mTmpGroupListener = new ITmpGroupListener() {
        @Override
        public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
            notifyListeners(new CreateTmpGroupRequestResultNotifier(result, labels, group));
        }

        @Override
        public void onDismissGroupRequestResult(int result, String groupId) {
            notifyListeners(new DismissTmpGroupRequestResultNotifier(result, groupId));
        }

        @Override
        public void onQueryGroupInfoResult(int result, String groupId, TmpGroup group) {
            notifyListeners(new QueryTmpGroupInfoResultNotifier(result, groupId, group));
        }

        @Override
        public void onQuitGroupResult(int result, String groupId) {
            notifyListeners(new QuitTmpGroupResultNotifier(result, groupId));
        }

        @Override
        public void onQueryGroupSystemTimeResult(int result, String groupId,
                                                 TmpGroupTime groupTime) {
            notifyListeners(new QueryTmpGroupSystemTimeResultNotifier(result,
                    groupId, groupTime));
        }

        @Override
        public void onGroupDismissRemind(String groupId, long timeRemaining) {
            notifyListeners(new TmpGroupDismissRemindNotifier(groupId, timeRemaining));
        }
    };

    private final ITagsListener mTagsListener = new ITagsListener() {
        @Override
        public void onTagUpdated() {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(CoreServiceBinderListener listener)
                        throws RemoteException {
                    listener.listener.onUserTagUpdated();
                }
            });
        }

        @Override
        public void onSetTagResult(final int result) {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(CoreServiceBinderListener listener)
                        throws RemoteException {
                    listener.listener.onSetUserTagResult(result);
                }
            });
        }
    };

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                notifier.notify(mListeners.get(i));
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void onNetworkAvailableChanged(boolean networkAvailable) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onNetworkAvailableChanged(networkAvailable);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }

        mAccountManager.networkAvailableChanged(networkAvailable);
    }

    private void onAccountLoginResult(int result, boolean accountChanged, boolean infoUpdated) {
        // When account changed, do some clean work
        if (accountChanged) {
            // do some clean work, delete chat message, contact, system push
            mContactsStore.clear();
            mChatMessageStore.clear();
            mTagsStore.clear();
            mFollowUserStore.clear();
        }

        // Account login operation now finished, notify the result.
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountLogin(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }

        // Now connect the IM server
        if (result == ConstantCode.ACCOUNT_OPERATION_SUCCESS) {
            if (infoUpdated) {
                // synchronize data like contacts from server.
                mContactsStore.sync();
                mTagsStore.sync();
                mFollowUserStore.sync();
            }
            mLocationSender.startSending();

            if (!mIMMediator.isConnected() || accountChanged) {
                final String[] connectArgs = new String[]{
                        mSettingHelper.getAccountRongCloudToken(),
                };
                mIMMediator.connect(connectArgs);
            }
            final String[] connectArgs = new String[]{
                    mAccountManager.getLabelCode(),
            };
            mPushMediator.connect(connectArgs);
        }
    }

    private void onAccountLogoutResult(int result) {
        mLocationSender.stopSending();
        mIMMediator.disconnect();
        mPushMediator.disconnect();
        mImConnectResult = -1;

        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountLogout(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void onAccountRegisterResult(int result) {
        // notify register result
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountRegistered(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void onAccountPersonalInfoUpdatedResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountPersonalInfoUpdated(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void onAccountLoginInOtherClient() {
        mContactsStore.clear();
        mChatMessageStore.clear();
        mTagsStore.clear();
        mFollowUserStore.clear();

        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountLoginInOtherClient();
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void onAccountOAuthBindAccountResult(int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onAccountOAuthBindAccount(result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyNewContactAdded(UserContact contact) {
        mChatMessageStore.onNewContactAdded(contact);

        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onNewContactAdded(contact);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyContactUpdated(UserContact contact) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onContactUpdated(contact);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyModifyFriendRemarkResult(int result, String friendUserId,
                                                String friendRemark) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onModifyFriendRemarkResult(result,
                        friendUserId, friendRemark);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyDeleteFriendResult(int result, String friendUserId,
                                          String friendLabelCode) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onDeleteFriendResult(result,
                        friendUserId, friendLabelCode);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyContactDefriendedMe(String friendUserId) {
        notifyListeners(new ContactDefriendedMeNotifier(friendUserId));
    }

    private ICoreServiceCallback mCallback = new ICoreServiceCallback() {

        @Override
        public void sendNewChatMessage(ChatMessage chatMessage) {
            if (!mIMMediator.sendChatMessage(chatMessage)) {
                notifyChatMessageSendResult(chatMessage, ConstantCode.SEND_RESULT_OFFLINE);
            }
        }

        @Override
        public void executeCommand(RequestCommand command, ICommandResponseHandler handler) {
            mInternalCommandProcessor.executeCommand(command, handler);
        }

        @Override
        public BaseCommand preTreatCommand(BaseCommand command) {
            if (command == null) {
                return null;
            }

            if (command instanceof UserCommand) {
                UserCommand cmd = (UserCommand) command;
                cmd.setSession(getAccountSession());
                cmd.setUserId(getAccountUserId());
            } else if (command instanceof SessionCommand) {
                SessionCommand cmd = (SessionCommand) command;
                cmd.setSession(getAccountSession());
            }

            return command;
        }

        @Override
        public String getAccountSession() {
            return mAccountManager.getSession();
        }

        @Override
        public String getAccountUserId() {
            return mAccountManager.getUserId();
        }

        @Override
        public String getAccountLabelCode() {
            return mAccountManager.getLabelCode();
        }

        @Override
        public String getAccountPassword() {
            return mAccountManager.getPassword();
        }

        @Override
        public void execute(Runnable task) {
            mTaskExecutor.execute(task);
        }

        @Override
        public boolean isNetworkAvailable() {
            return mNetworkMonitor.isNetworkAvailable();
        }

        @Override
        public void runDelayed(Runnable r, long delay) {
            mMainHandler.postDelayed(r, delay);
        }

        @Override
        public void runDelayedInProcess(Runnable r, long delay) {
            mProcessHandler.postDelayed(r, delay);
        }

        @Override
        public Looper getProcessLooper() {
            return mProcessThread.getLooper();
        }

        @Override
        public void clearChatHistory(String userId) {
            mChatMessageStore.clear(userId);
        }

        @Override
        public void addNewChatMessage(ChatMessage chatMessage) {
            mChatMessageStore.addNewChatMessage(chatMessage);
        }

        @Override
        public void notifyCoreService(CoreServiceNotifier notifier) {
            onNotifyCoreService(notifier);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initCoreEventHandler();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deInit();

        Intent intent = new Intent(GuardConst.ACTION_SERVICE_DEAD);
        intent.putExtra(GuardConst.EXTRA_SERVICE, GuardConst.SERVICE_CORE);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private void initCoreEventHandler() {
        if (mCoreEventHandler == null) {
            mCoreEventHandler = new CoreEventHandler(mCallback);
        }
    }

    private void init() {
        mCoreEventBus.register(mCoreEventHandler);
        mProcessThread = new HandlerThread("CoreService_ProcessThread");
        mProcessThread.start();
        mProcessHandler = new ProcessHandler(mProcessThread.getLooper(), this);
        mMainHandler = new Handler();
        mSettingHelper = SettingHelper.getInstance(this);
        mTaskExecutor = TaskExecutor.getInstance();
        mCommandProcessor = new CommandProcessor(this);
        mCommandProcessor.registerListener(mCommandProcessListener);
        mInternalCommandProcessor = new InternalCommandProcessor(this, mCommandProcessListener);
        mNetworkMonitor = new NetworkMonitor(this, mNetworkListener);
        mNetworkMonitor.start();
        mLocationSender = new LocationSender(this, mCallback);
        mLocationSender.startLocating();
        mIMMediator = IMMediatorHelper.newIMMediator(this, mCallback);
        mIMMediator.registerListener(mIMListener);
        mIMMediator.initialize();
        mChatMessageStore = new ChatMessageStore(this, mCallback);
        mChatMessageStore.registerListener(mChatMessageStoreListener);
        mSystemPushStore = new SystemPushStore(this);
        mSystemPushStore.init();
        mSystemPushStore.registerListener(mSystemPushListener);
        mContactsStore = new ContactsStore(this, mCallback);
        mContactsStore.registerListener(mContactsListener);
        mStrangerStore = new StrangerStore(this, mCallback);
        mLiteStrangerStore = new LiteStrangerStore(this, mCallback);
        mLiteStrangerStore.init();
        mTagsStore = new TagsStore(this, mCallback);
        mTagsStore.registerListener(mTagsListener);
        mFollowUserStore = new FollowUserStore(this, mCallback);
        mFollowUserStore.init();
        mPushMediator = PushMediatorHelper.newPushMediator(this, mCallback);
        mPushMediator.init();
        mAccountManager = new AccountManagerCore(this, mCallback);
        mAccountManager.registerListener(mAccountListener);
        mTmpGroupMediator = new TmpGroupMediator(this, mCallback);
        mTmpGroupMediator.registerListener(mTmpGroupListener);
        mImConnectResult = -1;
    }

    private void deInit() {
        mCoreEventBus.unregister(mCoreEventHandler);
        mProcessThread.getLooper().quit();
        mProcessThread.quit();
        mLocationSender.stopSending();
        mLocationSender.stopLocating();
        mCommandProcessor.unregisterListener(mCommandProcessListener);
        mLocationSender = null;
        mNetworkMonitor.stop();
        mIMMediator.disconnect();
        mIMMediator.deinitialize();
        mIMMediator.unregisterListener(mIMListener);
        mChatMessageStore.unregisterListener(mChatMessageStoreListener);
        mChatMessageStore.deInit();
        mSystemPushStore.unregisterListener(mSystemPushListener);
        mSystemPushStore.deInit();
        mContactsStore.unregisterListener(mContactsListener);
        mContactsStore.deInit();
        mStrangerStore.deInit();
        mLiteStrangerStore.deInit();
        mTagsStore.unregisterListener(mTagsListener);
        mFollowUserStore.deInit();
        mAccountManager.unregisterListener(mAccountListener);
        mAccountManager.deInit();
        mPushMediator.disconnect();
        mPushMediator.deInit();
        mTmpGroupMediator.unregisterListener(mTmpGroupListener);
    }

    private void onRequestTestText() {
        mProcessHandler.sendEmptyMessageDelayed(MSG_REQUEST_TEST_TEXT, 1000);
    }

    private void handleTestText() {
        final String text = "CoreService:notifyTestText()";

        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onTestText(text);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyOnNewChatMessageReceived(ChatMessage chatMsg) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onNewChatMessageReceived(chatMsg);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyOnChatMessageSendResult(String messageSession, long messageId, int result) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onChatMessageSendResult(messageSession, messageId,
                        result);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyOnNewChatMessageSending(String messageSession, long messageId) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onNewChatMessageSending(messageSession, messageId);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyOnCommandExecuteResponse(String commandSession, int result, String response) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onCommandExecuteResponse(commandSession, result,
                        response);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private void notifyOnNewSystemPushReceived(SystemPush systemPush) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                mListeners.get(i).listener.onNewSystemPushReceived(systemPush);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private static final class ChatMessageSendRequest {
        public final String messageSession;
        public final ChatMessage chatMessage;

        public ChatMessageSendRequest(String messageSession, ChatMessage chatMessage) {
            this.messageSession = messageSession;
            this.chatMessage = chatMessage;
        }
    }

    private void onRequestSendChatMessage(String messageSession, ChatMessage chatMessage) {
        final Message msg = mProcessHandler.obtainMessage(MSG_REQUEST_SEND_CHAT_MESSAGE,
                new ChatMessageSendRequest(messageSession, chatMessage));
        mProcessHandler.sendMessage(msg);
    }

    private void handleSendChatMessage(Object obj) {
        if (obj != null && (obj instanceof ChatMessageSendRequest)) {
            final ChatMessageSendRequest request = (ChatMessageSendRequest) obj;
            mChatMessageStore.sendNewMessage(request.messageSession, request.chatMessage);
        }
    }

    private static final class ChatMessageReSendRequest {
        public final String messageSession;
        public final long messageId;

        public ChatMessageReSendRequest(String messageSession, long messageId) {
            this.messageSession = messageSession;
            this.messageId = messageId;
        }
    }

    private void onRequestReSendChatMessage(String messageSession, long messageId) {
        final Message msg = mProcessHandler.obtainMessage(MSG_REQUEST_RESEND_CHAT_MESSAGE,
                new ChatMessageReSendRequest(messageSession, messageId));
        mProcessHandler.sendMessage(msg);
    }

    private void handleReSendChatMessage(Object obj) {
        if (obj == null || !(obj instanceof ChatMessageReSendRequest)) {
            return;
        }

        final ChatMessageReSendRequest request = (ChatMessageReSendRequest) obj;
        mChatMessageStore.resendMessage(request.messageSession, request.messageId);
    }

    private void notifyNewChatMessageReceived(ChatMessage chatMessage) {
        final Message msg = mProcessHandler.obtainMessage(MSG_NOTIFY_NEW_MESSAGE, chatMessage);
        mProcessHandler.sendMessage(msg);
    }

    private void handleNewChatMessageReceived(Object obj) {
        if (obj != null && (obj instanceof ChatMessage)) {
            final ChatMessage chatMessage = (ChatMessage) obj;
            mChatMessageStore.onNewMessageReceived(chatMessage);
        }
    }

    private static final class ChatMessageSendResult {

        public final ChatMessage chatMessage;
        public final int result;

        public ChatMessageSendResult(ChatMessage chatMessage, int result) {
            this.chatMessage = chatMessage;
            this.result = result;
        }
    }

    private void notifyChatMessageSendResult(ChatMessage chatMessage, int result) {
        final Message msg = mProcessHandler.obtainMessage(MSG_NOTIFY_MESSAGE_SEND_RESULT,
                new ChatMessageSendResult(chatMessage, result)
        );
        mProcessHandler.sendMessage(msg);
    }

    private void handleChatMessageSendResult(Object obj) {
        if (obj != null && (obj instanceof ChatMessageSendResult)) {
            final ChatMessageSendResult writtenResult = (ChatMessageSendResult) obj;
            final ChatMessage chatMessage = writtenResult.chatMessage;
            final int result = writtenResult.result;

            mChatMessageStore.onMessageSendResult(chatMessage, result);
        }
    }

    private void onDeleteFriend(String friendUserId, String friendLabelCode) {
        mContactsStore.deleteFriend(friendUserId, friendLabelCode);
        mChatMessageStore.clear(friendUserId);
    }

    private void onNotifyCoreService(CoreServiceNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            try {
                notifier.notify(mListeners.get(i).listener);
            } catch (RemoteException rex) {
                L.e(TAG, "Listener dead");
                mListeners.remove(i);
            } catch (Exception ex) {
                L.e(TAG, "Listener failed", ex);
            }
        }
    }

    private static class ContactDefriendedMeNotifier implements ListenerNotifier {

        private final String mFriendUserId;

        public ContactDefriendedMeNotifier(String friendUserId) {
            mFriendUserId = friendUserId;
        }

        @Override
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onContactDefriendedMe(mFriendUserId);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onCreateTmpGroupRequestResult(mResult, mLabels, mGroup);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onDismissTmpGroupRequestResult(mResult, mGroupId);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onQueryTmpGroupInfoResult(mResult, mGroupId, mGroup);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onQuitTmpGroupResult(mResult, mGroupId);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onQueryTmpGroupSystemTimeResult(mResult, mGroupId, mGroupTime);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onTmpGroupDismissRemind(mGroupId, mTimeRemaining);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onJoinLabelChatRoomResult(labelId, result);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onQuitLabelChatRoomResult(labelId, result);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onJoinNormalChatRoomResult(chatRoomId, result);
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
        public void notify(CoreServiceBinderListener listener) throws RemoteException {
            listener.listener.onQuitNormalChatRoomResult(chatRoomId, result);
        }
    }
}
