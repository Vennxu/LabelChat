
package com.ekuater.labelchat.coreservice;

import com.ekuater.labelchat.coreservice.ICoreServiceListener;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PersonalUpdateInfo;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTag;

interface ICoreService {

    /**
     * register listener to CoreService
     *
     * @param listener
     */
    void registerListener(ICoreServiceListener listener);

    /**
     * unregister listener from CoreService
     *
     * @param listener
     */
    void unregisterListener(ICoreServiceListener listener);

    /**
     * for test
     */
    void requestTestText();

    // For account operations
    /**
     * account login
     *
     * @param user login user text
     * @param passwd password
     */
    void accountLogin(String user, String passwd);

    /**
     * automatically login on current active account
     */
    void accountAutomaticLogin();

    /**
     * register a new account
     *
     * @param mobile mobile number
     * @param verifyCode verify code from server
     * @param passwd password
     * @param nickname   initialize nickname
     * @param gender     user gender
     */
    void accountRegister(String mobile, String verifyCode, String passwd,
                         String nickname, int gender);

    /**
     * update current account's settings to server
     *
     * @param newInfo new personal information
     */
    void accountUpdatePersonalInfo(in PersonalUpdateInfo newInfo);

    /**
     * Third platform OAuth login
     *
     * @param platform    third platform
     * @param openId      open id
     * @param accessToken third platform access token
     * @param tokenExpire token expire time
     * @param userInfo    user information get from third platform
     */
    void accountOAuthLogin(String platform, String openId, String accessToken,
                           String tokenExpire, in PersonalUpdateInfo userInfo);

    /**
     * Convert third platform user to our own user by mobile
     *
     * @param mobile      mobile number
     * @param verifyCode  verify code from server
     * @param newPassword new password
     */
    void accountOAuthBindAccount(String mobile, String verifyCode, String newPassword);

    /**
     * Logout current account
     */
    void accountLogout();

    /**
     * Get current account logon session
     *
     * @return current account logon session
     */
    String accountGetSession();

    /**
     * Get current account user id
     *
     * @return current account user id
     */
    String accountGetUserId();

    /**
     * Get current account label code
     *
     * @return current account labelCode
     */
    String accountGetLabelCode();

    /**
     * Is now account login or not
     *
     * @return login or not
     */
    boolean accountIsLogin();

    /**
     * Is now account im server connected or not
     *
     * @return connected or not
     */
    boolean accountIsImConnected();

    // End of account operations

    /**
     * Get current location information
     * 
     * @return current location
     */
    LocationInfo getCurrentLocationInfo();

    /**
     * request to send new chat message
     *
     * @param messageSession message session to get the chat message id back while sending
     * @param chatMessage
     */
    void requestSendChatMessage(String messageSession, in ChatMessage chatMessage);

    /**
     * request to re-send new chat message
     *
     * @param messageSession message session to get the chat message id back while sending
     * @param messageId chat message id to be re-send
     */
    void requestReSendChatMessage(String messageSession, long messageId);

    /**
     * request delete a chat message by id
     *
     * @param messageId chat message id to be delete
     */
    void deleteChatMessage(long messageId);

    /**
     * request delete friend's chat message history
     *
     * @param userId friend's userId
     */
    void clearFriendChatMessage(String userId);

    /**
     * request to clear all chat message history
     */
    void clearAllChatMessage();

    /**
     * Execute a command
     *
     * @param command
     */
    void executeCommand(in RequestCommand command);

    /**
     * Add user labels for current account
     *
     * @param labels label names to add
     */
    void labelAddUserLabels(in BaseLabel[] labels);

    /**
     * Delete user labels from current account
     *
     * @param labels {@link UserLabel} to be deleted
     */
    void labelDeleteUserLabels(in UserLabel[] labels);

    /**
     * Get all user labels of current account.
     *
     * @return all user labels
     */
    UserLabel[] labelGetAllUserLabels();

    /**
     * Force get user labels from server
     */
    void labelForceRefreshUserLabels();

    /**
     * Is now network available or not
     *
     * @return available or not
     */
    boolean isNetworkAvailable();

     /**
         * Delete push messages by type
         *
         * @param type push message type
         * @param type push message tag
         */
     void deleteLikeSystemPushByType(int type, String tag);

    /**
     * Delete push messages by type
     *
     * @param type push message type
     */
    void deleteSystemPushByType(int type);

    /**
     * Delete push messages by types
     *
     * @param types push message types
     */
    void deleteSystemPushByTypes(in int[] types);

    /**
     * Delete push messages by flag
     *
     * @param flag push message flag
     */
    void deleteSystemPushByFlag(String flag);

    /**
     * Delete push messages by message id
     *
     * @param messageId push message id
     */
    void deleteSystemPush(long messageId);

    /**
     * Modify friend remark
     *
     * @param friendUserId friend userId
     * @param friendRemark new remark
     */
    void modifyFriendRemark(String friendUserId, String friendRemark);

    /**
     * Delete a friend from contacts
     *
     * @param friendUserId    friend userId
     * @param friendLabelCode friend labelCode
     */
    void deleteFriend(String friendUserId, String friendLabelCode);

    /**
     * Update contact information
     *
     * @param contact new contact information
     */
    void updateContact(in UserContact contact);

    /**
     * Request to create a tmp group
     *
     * @param labels  group labels
     * @param members members in group
     */
    void tmpGroupCreateGroupRequest(in BaseLabel[] labels, in String[] members);

    /**
     * Request to dismiss the group by group creator
     *
     * @param groupId groupId
     * @param reason  dismiss reason
     */
    void tmpGroupDismissGroupRequest(String groupId, String reason);

    /**
     * Request to get group information from server
     *
     * @param groupId groupId
     */
    void tmpGroupQueryGroupInfo(String groupId);

    /**
     * Group member request quit the group
     *
     * @param groupId groupId of which group to quit
     */
    void tmpGroupQuitGroup(String groupId);

    /**
     * Get system time of group the synchronize the group expire time
     *
     * @param groupId groupId
     */
    void tmpGroupQueryGroupSystemTime(String groupId);

    /**
     * Query group information from local database
     *
     * @param groupId groupId
     * @return group information
     */
    TmpGroup tmpGroupQueryGroup(String groupId);

    /**
     * Query all group ids from local database
     *
     * @return all group id array
     */
    String[] tmpGroupQueryAllGroupId();

    /**
     * Query group members from local database
     *
     * @param groupId groupId
     * @return group member array
     */
    Stranger[] tmpGroupQueryGroupMembers(String groupId);

    /**
     * Add or update stranger detail information to StrangerStore
     *
     * @param stranger new stranger detail information
     */
    void addStranger(in Stranger stranger);

    /**
     * Get stranger detail information from StrangerStore
     *
     * @param userId stranger userId
     * @return stranger detail information
     */
    Stranger getStranger(String userId);

    /**
     * Delete stranger from StrangerStore
     *
     * @param userId stranger userId
     */
    void deleteStranger(String userId);

    /**
     * Add or update lite stranger detail information to LiteStrangerStore
     *
     * @param stranger new stranger detail information
     */
    void addLiteStranger(in LiteStranger stranger);

    /**
     * Get lite stranger detail information from LiteStrangerStore
     *
     * @param userId stranger userId
     * @return stranger detail information
     */
    LiteStranger getLiteStranger(String userId);

    /**
     * Delete lite stranger from LiteStrangerStore
     *
     * @param userId stranger userId
     */
    void deleteLiteStranger(String userId);

    /**
     * Join label chat room
     *
     * @param labelId label ID
     */
    void joinLabelChatRoom(String labelId);

    /**
     * Join label chat room
     *
     * @param labelId label ID
     */
    void quitLabelChatRoom(String labelId);

    /**
     * Join normal chat room
     *
     * @param chatRoomId normal chat room id
     */
    void joinNormalChatRoom(String chatRoomId);

    /**
     * Join normal chat room
     *
     * @param chatRoomId normal chat room id
     */
    void quitNormalChatRoom(String chatRoomId);

    UserTag[] tagGetUserTags();

    void tagSetUserTags(in UserTag[] tags);

    void tagSetUserTagsUser(in UserTag[] tags, in String[] userIds, String tagId);

    FollowUser getFollowingUser(String userId);

    FollowUser[] batchQueryFollowerUser(in String[] userIds);

    FollowUser[] getAllFollowingUser();

    void addFollowingUser(in FollowUser followUser);

    void deleteFollowingUser(String userId);

    FollowUser getFollowerUser(String userId);

    FollowUser[] getAllFollowerUser();

    void addFollowerUser(in FollowUser followUser);

    void deleteFollowerUser(String userId);

    void clearAllData();
}