
package com.ekuater.labelchat.delegate;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.datastruct.UserContact;

/**
 * Interface of notifier call by CoreService
 *
 * @author LinYong
 */
public interface ICoreServiceNotifier {

    /**
     * Call when CoreService connected
     */
    public void onCoreServiceConnected();

    /**
     * Call when CoreService died
     */
    public void onCoreServiceDied();

    /**
     * CoreService show test text
     *
     * @param text text return from CoreService
     */
    public void onTestText(String text);

    /**
     * notify the im connection result
     *
     * @param result im connect result, success or error code
     */
    public void onImConnected(int result);

    /**
     * notify that a new chat message has been received.
     *
     * @param chatMsg received chat message
     */
    public void onNewChatMessageReceived(ChatMessage chatMsg);

    /**
     * notify chat message send success or not.
     *
     * @param messageSession message session passed when sending the chat
     *                       message to identify the message
     * @param messageId      chat message id
     * @param result         success or error code, see class
     *                       {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onChatMessageSendResult(String messageSession, long messageId, int result);

    /**
     * Notify the chat message is been stored and sending now.
     *
     * @param messageSession message session passed when sending the chat
     *                       message to identify the message
     * @param messageId      the sending chat message's message id
     */
    public void onNewChatMessageSending(String messageSession, long messageId);

    /**
     * Notify account login result
     *
     * @param result success or error code see {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onAccountLogin(int result);

    /**
     * Notify account logout result
     *
     * @param result success or error code see {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onAccountLogout(int result);

    /**
     * Notify new account register result
     *
     * @param result success or error code see {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onAccountRegistered(int result);

    /**
     * Notify update personal information result
     *
     * @param result success or error code see {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onAccountPersonalInfoUpdated(int result);

    /**
     * Notify account login in other client
     */
    public void onAccountLoginInOtherClient();

    /**
     * Notify third platform OAuth user bind account result
     *
     * @param result success or error code see {@link com.ekuater.labelchat.datastruct.ConstantCode}
     */
    public void onAccountOAuthBindAccount(int result);

    /**
     * Notify user label of current account has been updated.
     */
    public void onUserLabelUpdated();

    /**
     * Notify add user label operation has been completed.
     *
     * @param result result of operation
     */
    public void onUserLabelAdded(int result);

    /**
     * Notify delete user label operation has been completed.
     *
     * @param result result of operation
     */
    public void onUserLabelDeleted(int result);

    /**
     * Notify network available state changed.
     *
     * @param networkAvailable network available or not
     */
    public void onNetworkAvailableChanged(boolean networkAvailable);

    /**
     * Notify there is a new SystemPush message has been received.
     *
     * @param systemPush new SystemPush message
     */
    public void onNewSystemPushReceived(SystemPush systemPush);

    /**
     * Notify new contact added
     *
     * @param contact new contact
     */
    public void onNewContactAdded(UserContact contact);

    /**
     * Notify contact information updated
     *
     * @param contact updated contact
     */
    public void onContactUpdated(UserContact contact);

    /**
     * notify modify friend remark result
     *
     * @param result       result
     * @param friendUserId friend userId
     * @param friendRemark new remark
     */
    public void onModifyFriendRemarkResult(int result, String friendUserId,
                                           String friendRemark);

    /**
     * notify delete friend result
     *
     * @param result          result
     * @param friendUserId    friend userId
     * @param friendLabelCode friend labelCode
     */
    public void onDeleteFriendResult(int result, String friendUserId,
                                     String friendLabelCode);

    /**
     * notify someone defriend you
     *
     * @param friendUserId friend userId
     */
    public void onContactDefriendedMe(String friendUserId);

    /**
     * Group create result
     *
     * @param result result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param labels group labels
     * @param group  new create group information
     */
    public void onCreateTmpGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group);

    /**
     * Group dismiss result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    public void onDismissTmpGroupRequestResult(int result, String groupId);

    /**
     * Query group information from server result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     * @param group   group information
     */
    public void onQueryTmpGroupInfoResult(int result, String groupId, TmpGroup group);

    /**
     * Member quit group request result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    public void onQuitTmpGroupResult(int result, String groupId);

    /**
     * Get group system time result
     *
     * @param result    result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId   groupId
     * @param groupTime group time return from server
     */
    public void onQueryTmpGroupSystemTimeResult(int result, String groupId, TmpGroupTime groupTime);

    /**
     * @param groupId       groupId
     * @param timeRemaining group dismiss time remaining
     */
    public void onTmpGroupDismissRemind(String groupId, long timeRemaining);

    /**
     * Notify join label chat room result
     *
     * @param labelId label ID
     * @param result  result
     */
    public void onJoinLabelChatRoomResult(String labelId, int result);

    /**
     * Notify quit label chat room result
     *
     * @param labelId label ID
     * @param result  result
     */
    public void onQuitLabelChatRoomResult(String labelId, int result);

    /**
     * Notify join normal chat room result
     *
     * @param chatRoomId normal chat room id
     * @param result     result
     */
    public void onJoinNormalChatRoomResult(String chatRoomId, int result);

    /**
     * Notify quit normal chat room result
     *
     * @param chatRoomId normal chat room id
     * @param result     result
     */
    public void onQuitNormalChatRoomResult(String chatRoomId, int result);

    public void onUserTagUpdated();

    public void onSetUserTagResult(int result);

    /**
     * Notify FollowUser data changed, include Following user and Follower user
     */
    public void onFollowUserDataChanged();
}
