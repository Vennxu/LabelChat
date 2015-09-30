
package com.ekuater.labelchat.delegate;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.datastruct.UserContact;

/**
 * The ICoreServiceCallback class provides an empty implementation for all
 * methods defined by the {@link ICoreServiceNotifier} interface. This is a
 * convenience class which should be used in case you do not need to implement
 * all methods.
 *
 * @author LinYong
 */
/* package */ class AbstractCoreServiceNotifier implements ICoreServiceNotifier {

    @Override
    public void onCoreServiceConnected() {
    }

    @Override
    public void onCoreServiceDied() {
    }

    @Override
    public void onTestText(String text) {
    }

    @Override
    public void onImConnected(int result) {
    }

    @Override
    public void onNewChatMessageReceived(ChatMessage chatMsg) {
    }

    @Override
    public void onChatMessageSendResult(String messageSession, long messageId, int result) {
    }

    @Override
    public void onNewChatMessageSending(String messageSession, long messageId) {
    }

    @Override
    public void onAccountLogin(int result) {
    }

    @Override
    public void onAccountLogout(int result) {
    }

    @Override
    public void onAccountRegistered(int result) {
    }

    @Override
    public void onAccountPersonalInfoUpdated(int result) {
    }

    @Override
    public void onAccountLoginInOtherClient() {
    }

    @Override
    public void onAccountOAuthBindAccount(int result) {
    }

    @Override
    public void onUserLabelUpdated() {
    }

    @Override
    public void onUserLabelAdded(int result) {
    }

    @Override
    public void onUserLabelDeleted(int result) {
    }

    @Override
    public void onNetworkAvailableChanged(boolean networkAvailable) {
    }

    @Override
    public void onNewSystemPushReceived(SystemPush systemPush) {
    }

    @Override
    public void onNewContactAdded(UserContact contact) {
    }

    @Override
    public void onContactUpdated(UserContact contact) {
    }

    @Override
    public void onModifyFriendRemarkResult(int result, String friendUserId,
                                           String friendRemark) {
    }

    @Override
    public void onDeleteFriendResult(int result, String friendUserId,
                                     String friendLabelCode) {
    }

    @Override
    public void onContactDefriendedMe(String friendUserId) {
    }

    @Override
    public void onCreateTmpGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
    }

    @Override
    public void onDismissTmpGroupRequestResult(int result, String groupId) {
    }

    @Override
    public void onQueryTmpGroupInfoResult(int result, String groupId, TmpGroup group) {
    }

    @Override
    public void onQuitTmpGroupResult(int result, String groupId) {
    }

    @Override
    public void onQueryTmpGroupSystemTimeResult(int result, String groupId, TmpGroupTime groupTime) {
    }

    @Override
    public void onTmpGroupDismissRemind(String groupId, long timeRemaining) {
    }

    @Override
    public void onJoinLabelChatRoomResult(String labelId, int result) {
    }

    @Override
    public void onQuitLabelChatRoomResult(String labelId, int result) {
    }

    @Override
    public void onJoinNormalChatRoomResult(String chatRoomId, int result) {
    }

    @Override
    public void onQuitNormalChatRoomResult(String chatRoomId, int result) {
    }

    @Override
    public void onUserTagUpdated() {
    }

    @Override
    public void onSetUserTagResult(int result) {
    }

    @Override
    public void onFollowUserDataChanged() {
    }
}
