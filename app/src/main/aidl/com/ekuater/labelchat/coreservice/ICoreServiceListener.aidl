
package com.ekuater.labelchat.coreservice;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;

interface ICoreServiceListener {

    // for test
    void onTestText(String text);

    // for chat message
    /**
     * notify the im connection result
     *
     * @param result im connect result, success or error code
     */
    void onImConnected(int result);

    /**
     * notify that a new chat message has been received.
     *
     * @param chatMsg received chat message
     */
    void onNewChatMessageReceived(in ChatMessage chatMsg);

    /**
     * notify chat message send success or not.
     *
     * @param messageSession message session passed when sending the chat
     *            message to identify the message
     * @param messageId the sending chat message's message id
     * @param result success or error code
     */
    void onChatMessageSendResult(String messageSession, long messageId, int result);

    /**
     * Notify the chat message is been stored and sending now.
     *
     * @param messageSession message session passed when sending the chat
     *            message to identify the message
     * @param messageId the sending chat message's message id
     */
    void onNewChatMessageSending(String messageSession, long messageId);

    /**
     * Notify account login result
     *
     * @param result success or error code see {@link ConstantCode}
     */
    void onAccountLogin(int result);

    /**
     * Notify account logout result
     *
     * @param result success or error code see {@link ConstantCode}
     */
    void onAccountLogout(int result);

    /**
     * Notify new account register result
     *
     * @param result success or error code see {@link ConstantCode}
     */
    void onAccountRegistered(int result);

    /**
     * Notify update personal information result
     *
     * @param result success or error code see {@link ConstantCode}
     */
    void onAccountPersonalInfoUpdated(int result);

    /**
     * Notify account login in other client
     */
    void onAccountLoginInOtherClient();

    /**
     * Notify third platform OAuth user bind account result
     *
     * @param result success or error code see {@link ConstantCode}
     */
    void onAccountOAuthBindAccount(int result);

    /**
     * Notify the result of command executed with command session
     *
     * @param commandSession command session
     * @param result execute result, success or error code
     * @param response command response data
     */
    void onCommandExecuteResponse(String commandSession, int result, String response);

    /**
     * Notify user label of current account has been updated.
     */
    void onUserLabelUpdated();

    /**
     * Notify add user label operation has been completed.
     *
     * @param result result of operation
     */
    void onUserLabelAdded(int result);

    /**
     * Notify delete user label operation has been completed.
     *
     * @param result result of operation
     */
    void onUserLabelDeleted(int result);

    /**
     * Notify network available state changed.
     *
     * @param networkAvailable network available or not
     */
    void onNetworkAvailableChanged(boolean networkAvailable);

    /**
     * Notify there is a new SystemPush message has been received.
     *
     * @param systemPush new SystemPush message
     */
    void onNewSystemPushReceived(in SystemPush systemPush);

    /**
     * Notify new contact added
     *
     * @param contact new contact
     */
    void onNewContactAdded(in UserContact contact);

    /**
     * Notify contact information updated
     *
     * @param contact updated contact
     */
    void onContactUpdated(in UserContact contact);

    /**
     * notify modify friend remark result
     *
     * @param result       result
     * @param friendUserId friend userId
     * @param friendRemark new remark
     */
    void onModifyFriendRemarkResult(int result, String friendUserId, String friendRemark);

    /**
     * notify delete friend result
     *
     * @param result          result
     * @param friendUserId    friend userId
     * @param friendLabelCode friend labelCode
     */
    void onDeleteFriendResult(int result, String friendUserId, String friendLabelCode);

    /**
     * notify someone defriend me
     *
     * @param friendUserId friend userId
     */
    void onContactDefriendedMe(String friendUserId);

    /**
     * Group create result
     *
     * @param result result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param labels group labels
     * @param group  new create group information
     */
    void onCreateTmpGroupRequestResult(int result, in BaseLabel[] labels, in TmpGroup group);

    /**
     * Group dismiss result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    void onDismissTmpGroupRequestResult(int result, String groupId);

    /**
     * Query group information from server result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     * @param group   group information
     */
    void onQueryTmpGroupInfoResult(int result, String groupId, in TmpGroup group);

    /**
     * Member quit group request result
     *
     * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId groupId
     */
    void onQuitTmpGroupResult(int result, String groupId);

    /**
     * Get group system time result
     *
     * @param result    result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
     * @param groupId   groupId
     * @param groupTime group time return from server
     */
    void onQueryTmpGroupSystemTimeResult(int result, String groupId, in TmpGroupTime groupTime);

    /**
     * @param groupId       groupId
     * @param timeRemaining group dismiss time remaining
     */
    void onTmpGroupDismissRemind(String groupId, long timeRemaining);

    /**
     * Notify join label chat room result
     *
     * @param labelId label ID
     * @param result  result
     */
    void onJoinLabelChatRoomResult(String labelId, int result);

    /**
     * Notify quit label chat room result
     *
     * @param labelId label ID
     * @param result  result
     */
    void onQuitLabelChatRoomResult(String labelId, int result);

    /**
     * Notify join normal chat room result
     *
     * @param chatRoomId normal chat room id
     * @param result     result
     */
    void onJoinNormalChatRoomResult(String chatRoomId, int result);

    /**
     * Notify quit normal chat room result
     *
     * @param chatRoomId normal chat room id
     * @param result     result
     */
    void onQuitNormalChatRoomResult(String chatRoomId, int result);

    void onUserTagUpdated();

    void onSetUserTagResult(int result);

    /**
     * Notify FollowUser data changed, include Following user and Follower user
     */
    void onFollowUserDataChanged();
}