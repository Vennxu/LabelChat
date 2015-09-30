
package com.ekuater.labelchat.delegate;

import android.os.IBinder;
import android.os.RemoteException;

import com.ekuater.labelchat.coreservice.ICoreService;
import com.ekuater.labelchat.coreservice.ICoreServiceListener;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class CoreServiceWrapper implements ICoreService {

    private static final String TAG = "service.CoreServiceWrapper";

    private ICoreService mService;

    public CoreServiceWrapper(ICoreService service) {
        mService = service;
    }

    @Override
    public IBinder asBinder() {
        return mService.asBinder();
    }

    @Override
    public void registerListener(ICoreServiceListener listener) {
        try {
            mService.registerListener(listener);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void unregisterListener(ICoreServiceListener listener) {
        try {
            mService.unregisterListener(listener);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void requestTestText() {
        try {
            mService.requestTestText();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountLogin(String user, String passwd) {
        try {
            mService.accountLogin(user, passwd);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountAutomaticLogin() {
        try {
            mService.accountAutomaticLogin();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountRegister(String mobile, String verifyCode, String password,
                                String nickname, int gender) {
        try {
            mService.accountRegister(mobile, verifyCode, password, nickname, gender);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountUpdatePersonalInfo(PersonalUpdateInfo newInfo) {
        try {
            mService.accountUpdatePersonalInfo(newInfo);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountOAuthLogin(String platform, String openId, String accessToken,
                                  String tokenExpire, PersonalUpdateInfo userInfo) {
        try {
            mService.accountOAuthLogin(platform, openId, accessToken, tokenExpire, userInfo);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountOAuthBindAccount(String mobile, String verifyCode, String newPassword) {
        try {
            mService.accountOAuthBindAccount(mobile, verifyCode, newPassword);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void accountLogout() {
        try {
            mService.accountLogout();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public String accountGetSession() {
        String value = null;

        try {
            value = mService.accountGetSession();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return value;
    }

    @Override
    public String accountGetUserId() {
        String value = null;

        try {
            value = mService.accountGetUserId();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return value;
    }

    @Override
    public String accountGetLabelCode() {
        String value = null;

        try {
            value = mService.accountGetLabelCode();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return value;
    }

    @Override
    public boolean accountIsLogin() {
        boolean _ret = false;

        try {
            _ret = mService.accountIsLogin();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return _ret;
    }

    @Override
    public boolean accountIsImConnected() {
        boolean _ret = false;

        try {
            _ret = mService.accountIsImConnected();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return _ret;
    }

    @Override
    public LocationInfo getCurrentLocationInfo() {
        try {
            return mService.getCurrentLocationInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override
    public void requestSendChatMessage(String messageSession, ChatMessage chatMessage) {
        try {
            mService.requestSendChatMessage(messageSession, chatMessage);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void requestReSendChatMessage(String messageSession, long messageId) {
        try {
            mService.requestReSendChatMessage(messageSession, messageId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteChatMessage(long messageId) {
        try {
            mService.deleteChatMessage(messageId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void clearFriendChatMessage(String userId) {
        try {
            mService.clearFriendChatMessage(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void clearAllChatMessage() {
        try {
            mService.clearAllChatMessage();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void clearAllData() {
        try {
            mService.clearAllData();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void executeCommand(RequestCommand command) {
        try {
            mService.executeCommand(command);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void labelAddUserLabels(BaseLabel[] labels) {
        try {
            mService.labelAddUserLabels(labels);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void labelDeleteUserLabels(UserLabel[] labels) {
        try {
            mService.labelDeleteUserLabels(labels);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public UserLabel[] labelGetAllUserLabels() {
        UserLabel[] labels = null;

        try {
            labels = mService.labelGetAllUserLabels();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return labels;
    }

    @Override
    public void labelForceRefreshUserLabels() {
        try {
            mService.labelForceRefreshUserLabels();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public boolean isNetworkAvailable() {
        boolean _ret = false;

        try {
            _ret = mService.isNetworkAvailable();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }

        return _ret;
    }

    @Override
    public void deleteSystemPushByType(int type) {
        try {
            mService.deleteSystemPushByType(type);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteLikeSystemPushByType(int type, String tag) {
        try {
            mService.deleteLikeSystemPushByType(type, tag);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteSystemPushByTypes(int[] types) {
        try {
            mService.deleteSystemPushByTypes(types);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteSystemPushByFlag(String flag) {
        try {
            mService.deleteSystemPushByFlag(flag);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteSystemPush(long messageId) {
        try {
            mService.deleteSystemPush(messageId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void modifyFriendRemark(String friendUserId, String friendRemark) {
        try {
            mService.modifyFriendRemark(friendUserId, friendRemark);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteFriend(String friendUserId, String friendLabelCode) {
        try {
            mService.deleteFriend(friendUserId, friendLabelCode);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void updateContact(UserContact contact) {
        try {
            mService.updateContact(contact);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tmpGroupCreateGroupRequest(BaseLabel[] labels, String[] members) {
        try {
            mService.tmpGroupCreateGroupRequest(labels, members);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tmpGroupDismissGroupRequest(String groupId, String reason) {
        try {
            mService.tmpGroupDismissGroupRequest(groupId, reason);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tmpGroupQueryGroupInfo(String groupId) {
        try {
            mService.tmpGroupQueryGroupInfo(groupId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tmpGroupQuitGroup(String groupId) {
        try {
            mService.tmpGroupQuitGroup(groupId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tmpGroupQueryGroupSystemTime(String groupId) {
        try {
            mService.tmpGroupQueryGroupSystemTime(groupId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public TmpGroup tmpGroupQueryGroup(String groupId) {
        try {
            return mService.tmpGroupQueryGroup(groupId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public String[] tmpGroupQueryAllGroupId() {
        try {
            return mService.tmpGroupQueryAllGroupId();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public Stranger[] tmpGroupQueryGroupMembers(String groupId) {
        try {
            return mService.tmpGroupQueryGroupMembers(groupId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void addStranger(Stranger stranger) {
        try {
            mService.addStranger(stranger);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public Stranger getStranger(String userId) {
        try {
            return mService.getStranger(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void deleteStranger(String userId) {
        try {
            mService.deleteStranger(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void addLiteStranger(LiteStranger stranger) {
        try {
            mService.addLiteStranger(stranger);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public LiteStranger getLiteStranger(String userId) {
        try {
            return mService.getLiteStranger(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void deleteLiteStranger(String userId) {
        try {
            mService.deleteLiteStranger(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void joinLabelChatRoom(String labelId) {
        try {
            mService.joinLabelChatRoom(labelId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void quitLabelChatRoom(String labelId) {
        try {
            mService.quitLabelChatRoom(labelId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void joinNormalChatRoom(String chatRoomId) {
        try {
            mService.joinNormalChatRoom(chatRoomId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void quitNormalChatRoom(String chatRoomId) {
        try {
            mService.quitNormalChatRoom(chatRoomId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public UserTag[] tagGetUserTags() {
        try {
            return mService.tagGetUserTags();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void tagSetUserTags(UserTag[] tags) {
        try {
            mService.tagSetUserTags(tags);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void tagSetUserTagsUser(UserTag[] tags, String[] userIds, String tagId)  {
        try {
            mService.tagSetUserTagsUser(tags, userIds, tagId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public FollowUser getFollowingUser(String userId) {
        try {
            return mService.getFollowingUser(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    public FollowUser[] batchQueryFollowerUser(String[] userIds) {
        try {
            return mService.batchQueryFollowerUser(userIds);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
        return null;
    }

    @Override
    public FollowUser[] getAllFollowingUser() {
        try {
            return mService.getAllFollowingUser();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void addFollowingUser(FollowUser followUser) {
        try {
            mService.addFollowingUser(followUser);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteFollowingUser(String userId) {
        try {
            mService.deleteFollowingUser(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public FollowUser getFollowerUser(String userId) {
        try {
            return mService.getFollowerUser(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public FollowUser[] getAllFollowerUser() {
        try {
            return mService.getAllFollowerUser();
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
            return null;
        }
    }

    @Override
    public void addFollowerUser(FollowUser followUser) {
        try {
            mService.addFollowerUser(followUser);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

    @Override
    public void deleteFollowerUser(String userId) {
        try {
            mService.deleteFollowerUser(userId);
        } catch (RemoteException e) {
            L.e(TAG, "Remote Exception", e);
        }
    }

}
