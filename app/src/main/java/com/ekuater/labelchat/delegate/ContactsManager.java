
package com.ekuater.labelchat.delegate;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.contact.AddFriendRequestCommand;
import com.ekuater.labelchat.command.contact.BubblingCommand;
import com.ekuater.labelchat.command.contact.ExactSearchCommand;
import com.ekuater.labelchat.command.contact.LabelQueryUserCommand;
import com.ekuater.labelchat.command.contact.NewUserCommand;
import com.ekuater.labelchat.command.contact.OneLabelQueryUserCommand;
import com.ekuater.labelchat.command.contact.QueryFriendInfoCommand;
import com.ekuater.labelchat.command.contact.QueryNearbyUserCommand;
import com.ekuater.labelchat.command.contact.QueryUserInfoCommand;
import com.ekuater.labelchat.command.contact.RandUsersCommand;
import com.ekuater.labelchat.command.contact.TodayRecommendedCommand;
import com.ekuater.labelchat.command.contact.ValidFriendRequestCommand;
import com.ekuater.labelchat.command.contact.WeeklyStarConfirmCommand;
import com.ekuater.labelchat.data.DataConstants;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.ui.fragment.LookFrendsFragment;
import com.ekuater.labelchat.ui.util.FileUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.SystemAccount;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author LinYong
 */
public class ContactsManager extends BaseManager {

    private static final String TAG = ContactsManager.class.getSimpleName();

    // Query result enum
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public interface IListener extends BaseManager.IListener {
        void onContactDataChanged();

        /**
         * Notify new contact added
         *
         * @param contact new contact
         */
        void onNewContactAdded(UserContact contact);

        /**
         * Notify contact information updated
         *
         * @param contact updated contact
         */
        void onContactUpdated(UserContact contact);

        /**
         * notify modify friend remark result
         *
         * @param result       result
         * @param friendUserId friend userId
         * @param friendRemark new remark
         */
        void onModifyFriendRemarkResult(int result, String friendUserId,
                                        String friendRemark);

        /**
         * notify delete friend result
         *
         * @param result          result
         * @param friendUserId    friend userId
         * @param friendLabelCode friend labelCode
         */
        void onDeleteFriendResult(int result, String friendUserId,
                                  String friendLabelCode);

        /**
         * notify someone defriend you
         *
         * @param friendUserId friend userId
         */
        void onContactDefriendedMe(String friendUserId);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onContactDataChanged() {
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
    }

    public interface FriendsQueryObserver {
        void onQueryResult(int result, Stranger[] users, boolean remaining);
    }

    public interface UserQueryObserver {
        void onQueryResult(int result, Stranger user);
    }

    public interface ContactQueryObserver {
        void onQueryResult(int result, UserContact contact);
    }

    public interface PraiseStrangerObserver {
        void onQueryResult(int result, PraiseStranger[] strangers, boolean remaining);
    }

    public interface NewUserObserver {
        void onQueryResult(int result, Stranger[] strangers, boolean remaining);
    }

    private interface ListenerNotifier {
        void notify(IListener listener);
    }

    private static class ColumnsMap {

        public final int mId;
        public final int mUserId;
        public final int mLabelCode;
        public final int mNickname;
        public final int mRemarksName;
        public final int mMobile;
        public final int mSex;
        public final int mBirthday;
        public final int mAge;
        public final int mConstellation;
        public final int mProvince;
        public final int mCity;
        public final int mSchool;
        public final int mSignature;
        public final int mAvatar;
        public final int mAvatarThumb;
        public final int mLabels;
        public final int mAppearanceFace;
        public final int mTheme;

        private static int getColumnIndex(Cursor cursor, String columnName) {
            return cursor.getColumnIndex(columnName);
        }

        public ColumnsMap(Cursor cursor) {
            mId = getColumnIndex(cursor, DataConstants.Contact._ID);
            mUserId = getColumnIndex(cursor, DataConstants.Contact.USER_ID);
            mLabelCode = getColumnIndex(cursor, DataConstants.Contact.LABEL_CODE);
            mNickname = getColumnIndex(cursor, DataConstants.Contact.NICKNAME);
            mRemarksName = getColumnIndex(cursor, DataConstants.Contact.REMARKS_NAME);
            mMobile = getColumnIndex(cursor, DataConstants.Contact.MOBILE);
            mSex = getColumnIndex(cursor, DataConstants.Contact.SEX);
            mBirthday = getColumnIndex(cursor, DataConstants.Contact.BIRTHDAY);
            mAge = getColumnIndex(cursor, DataConstants.Contact.AGE);
            mConstellation = getColumnIndex(cursor, DataConstants.Contact.CONSTELLATION);
            mProvince = getColumnIndex(cursor, DataConstants.Contact.PROVINCE);
            mCity = getColumnIndex(cursor, DataConstants.Contact.CITY);
            mSchool = getColumnIndex(cursor, DataConstants.Contact.SCHOOL);
            mSignature = getColumnIndex(cursor, DataConstants.Contact.SIGNATURE);
            mAvatar = getColumnIndex(cursor, DataConstants.Contact.AVATAR);
            mAvatarThumb = getColumnIndex(cursor, DataConstants.Contact.AVATAR_THUMB);
            mLabels = getColumnIndex(cursor, DataConstants.Contact.LABELS);
            mAppearanceFace = getColumnIndex(cursor, DataConstants.Contact.APPEARANCE_FACE);
            mTheme = getColumnIndex(cursor, DataConstants.Contact.THEME);
        }
    }

    private static ContactsManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ContactsManager(context.getApplicationContext());
        }
    }

    public static ContactsManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private final AccountManager mAccountManager;
    private final List<WeakReference<IListener>> mListeners = new ArrayList<>();
    private final int mMobileLength;
    private final int mLabelCodeMinLength;
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
        public void onContactUpdated(UserContact contact) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onContactUpdated(contact);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onNewContactAdded(UserContact contact) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onNewContactAdded(contact);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onModifyFriendRemarkResult(int result, String friendUserId,
                                               String friendRemark) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onModifyFriendRemarkResult(result, friendUserId, friendRemark);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onDeleteFriendResult(int result, String friendUserId,
                                         String friendLabelCode) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onDeleteFriendResult(result, friendUserId, friendLabelCode);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onContactDefriendedMe(String friendUserId) {
            notifyListeners(new ContactDefriendedMeNotifier(friendUserId));
        }
    };

    private final ContentObserver mContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            notifyContactDataChanged();
        }
    };

    private SystemAccount mSystemAccount;

    // Use as geInstance()
    private ContactsManager(Context context) {
        super(context);
        final Resources res = context.getResources();
        mContext = context;
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mLabelCodeMinLength = res.getInteger(R.integer.label_code_min_length);
        mAccountManager = AccountManager.getInstance(context);
        mCoreService.registerNotifier(mNotifier);
        mContext.getContentResolver().registerContentObserver(DataConstants.Contact.CONTENT_URI,
                true, mContentObserver);
        mSystemAccount = SystemAccount.getInstance(context);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
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

    public void requestAddFriend(String userId, String labelCode,
                                 String verifyMsg, FunctionCallListener listener) {
        requestAddFriend(userId, labelCode, "", verifyMsg, listener);
    }

    public void requestAddFriend(String userId, String labelCode, String nickname,
                                 String verifyMsg, FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(labelCode)) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_FAILED,
                        CommandErrorCode.EXECUTE_FAILED, null);
            }
            return;
        }

        if (verifyMsg == null) {
            verifyMsg = "";
        }

        AddFriendRequestCommand command = new AddFriendRequestCommand(getSession(),
                getUserId(), getLabelCode());
        command.putParamRequestUserId(userId);
        command.putParamRequestLabelCode(labelCode);
        command.putParamRequestNickname(nickname);
        command.putParamVerifyMsg(verifyMsg);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void requestBubbling(FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        BubblingCommand command = new BubblingCommand(getSession(),
                getUserId(), getLabelCode());
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void requestTodayRecommended(FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        TodayRecommendedCommand command = new TodayRecommendedCommand(getSession(),
                getUserId(), getLabelCode());
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void queryFriendByLabels(BaseLabel[] labels, int requestTime,
                                    FriendsQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (labels == null || labels.length <= 0) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null, false);
            return;
        }

        LabelQueryUserCommand command = new LabelQueryUserCommand(getSession(),
                getUserId(), getLabelCode());
        command.putParamLabels(labels);
        command.putParamRequestTime(requestTime);
        command.putParamLocation(getLocation());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FriendsQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FriendsQueryObserver observer = (FriendsQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    LabelQueryUserCommand.CommandResponse cmdResp
                            = new LabelQueryUserCommand.CommandResponse(response);
                    Stranger[] users = null;
                    boolean remaining = false;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        users = cmdResp.getQueryUsers();
                        remaining = (users != null) && (users.length >= 20);
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, users, remaining);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void queryNewUser(final Context context, NewUserObserver observer) {
        if (observer == null) {
            return;
        }
        NewUserCommand command = new NewUserCommand(getUserId(), getSession());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof NewUserObserver)) {
                    return;
                }
                NewUserObserver observer = (NewUserObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }
                try {
                    NewUserCommand.CommandResponse cmdResp = new NewUserCommand.CommandResponse(response);
                    Stranger[] user = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    boolean remaining = false;
                    if (cmdResp.requestSuccess()) {
                        keepCategory(response, LookFrendsFragment.FILE_NEW_USER_NAME);
                        user = cmdResp.getNewUsers();
                        _ret = QUERY_RESULT_SUCCESS;
                        remaining = (user != null);
                    }
                    observer.onQueryResult(_ret, user, remaining);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }

        };
        executeCommand(command, handler);
    }

    public void keepCategory(String fileString, String fileName) {
        String category = FileUtils.readFileData(fileName, mContext);
        if (!TextUtils.isEmpty(category)) {
            FileUtils.deletFileData(fileName);
        }
        FileUtils.writeFileData(fileName, fileString, mContext);
    }

    public void queryNearbyFriend(int requestTime, FriendsQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        QueryNearbyUserCommand command = new QueryNearbyUserCommand(getSession(), getUserId());
        command.putParamPosition(getPosition());
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FriendsQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FriendsQueryObserver observer = (FriendsQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    QueryNearbyUserCommand.CommandResponse cmdResp
                            = new QueryNearbyUserCommand.CommandResponse(response);
                    Stranger[] users = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    boolean remaining = false;

                    if (cmdResp.requestSuccess()) {
                        users = cmdResp.getQueryUsers();
                        _ret = QUERY_RESULT_SUCCESS;
                        remaining = (users != null) && (users.length >= 20);
                    }

                    observer.onQueryResult(_ret, users, remaining);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void getRandUsers(int randCount, FriendsQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        RandUsersCommand command = new RandUsersCommand(getSession(), getUserId());
        command.putParamRandCount(randCount);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FriendsQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FriendsQueryObserver observer = (FriendsQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    RandUsersCommand.CommandResponse cmdResp
                            = new RandUsersCommand.CommandResponse(response);
                    Stranger[] users = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        users = cmdResp.getRandUsers();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, users, false);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void queryUserInfo(String userId, UserQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null);
            return;
        }

        QueryUserInfoCommand command = new QueryUserInfoCommand(getSession(), getUserId());
        command.putParamStrangerUserId(userId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof UserQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                UserQueryObserver observer = (UserQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    QueryUserInfoCommand.CommandResponse cmdResp
                            = new QueryUserInfoCommand.CommandResponse(response);
                    Stranger user = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        user = cmdResp.getUserInfo();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, user);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void exactSearchUser(String searchWord, UserQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (searchWord == null || (searchWord.length() < Math.min(mMobileLength,
                mLabelCodeMinLength))) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null);
            return;
        }

        ExactSearchCommand command = new ExactSearchCommand(getSession());
        command.putParamSearchWord(searchWord);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof UserQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                UserQueryObserver observer = (UserQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ExactSearchCommand.CommandResponse cmdResp
                            = new ExactSearchCommand.CommandResponse(response);
                    Stranger user = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        user = cmdResp.getUserInfo();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, user);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void acceptWeeklyStarInvitation(String weeklyStarSession, boolean accept,
                                           FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        WeeklyStarConfirmCommand command = new WeeklyStarConfirmCommand(getSession(), getUserId());
        command.setParamSession(weeklyStarSession);
        command.setParamAccept(accept);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void acceptAddFriendInvitation(String friendUserId, String friendLabelCode,
                                          String friendRemark,
                                          FunctionCallListener listener) {
        validAddFriendInvitation(friendUserId, friendLabelCode, true, friendRemark, "", listener);
    }

    public void rejectAddFriendInvitation(String friendUserId, String friendLabelCode,
                                          String rejectMessage, FunctionCallListener listener) {
        validAddFriendInvitation(friendUserId, friendLabelCode, false, "", rejectMessage, listener);
    }

    public void validAddFriendInvitation(String friendUserId, String friendLabelCode,
                                         boolean accept, String friendRemark,
                                         String rejectMessage,
                                         FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        ValidFriendRequestCommand command = new ValidFriendRequestCommand(getSession(),
                getUserId(), getLabelCode());
        command.putParamFriendUserId(friendUserId);
        command.putParamFriendLabelCode(friendLabelCode);
        command.putParamAccept(accept);
        command.putParamFriendRemark(friendRemark);
        command.putParamRejectMsg(rejectMessage);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void queryContactInfo(String userId, ContactQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null);
            return;
        }

        QueryFriendInfoCommand command = new QueryFriendInfoCommand(getSession(), getUserId());
        command.putParamFriendUserId(userId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ContactQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                ContactQueryObserver observer = (ContactQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    QueryFriendInfoCommand.CommandResponse cmdResp
                            = new QueryFriendInfoCommand.CommandResponse(response);
                    UserContact contact = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        contact = cmdResp.getContact();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, contact);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryLabelPraiseStranger(String labelId, int requestTime,
                                         PraiseStrangerObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(labelId)) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null, false);
            return;
        }

        OneLabelQueryUserCommand command = new OneLabelQueryUserCommand(getSession(), getUserId());
        command.putParamLabelId(labelId);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof PraiseStrangerObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                PraiseStrangerObserver observer = (PraiseStrangerObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    OneLabelQueryUserCommand.CommandResponse cmdResp
                            = new OneLabelQueryUserCommand.CommandResponse(response);
                    PraiseStranger[] strangers = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    boolean remaining = false;

                    if (cmdResp.requestSuccess()) {
                        strangers = cmdResp.getPraiseStrangers();
                        remaining = (strangers != null && strangers.length >= 20);
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, strangers, remaining);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void modifyFriendRemark(String friendUserId, String friendRemark) {
        if (isInGuestMode()) {
            mNotifier.onModifyFriendRemarkResult(ConstantCode.CONTACT_OPERATION_SUCCESS,
                    friendUserId, friendRemark);
            return;
        }

        mCoreService.modifyFriendRemark(friendUserId, friendRemark);
    }

    public void deleteFriend(String friendUserId, String friendLabelCode) {
        if (isInGuestMode()) {
            mNotifier.onDeleteFriendResult(ConstantCode.CONTACT_OPERATION_SUCCESS,
                    friendUserId, friendLabelCode);
            return;
        }

        mCoreService.deleteFriend(friendUserId, friendLabelCode);
    }

    public void updateContact(UserContact contact) {
        if (isInGuestMode()) {
            return;
        }

        if (contact != null && contact.getId() >= 0) {
            mCoreService.updateContact(contact);
        }
    }

    public UserContact getUserContactByUserId(String userId) {
        if (isInGuestMode()) {
            return null;
        }

        UserContact systemContact = mSystemAccount.getAccount(userId);
        if (systemContact != null) {
            return systemContact;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Contact.CONTENT_URI;
        final String[] projection = DataConstants.Contact.ALL_COLUMNS;
        final String selection = DataConstants.Contact.USER_ID + "=?";
        final String[] selectionArgs = new String[]{
                userId,
        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        UserContact contact = null;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            contact = buildUserContact(cursor);
        }
        cursor.close();

        return contact;
    }

    public UserContact[] batchQueryUserContact(String[] userIds) {
        if (isInGuestMode()) {
            return null;
        }

        if (userIds == null || userIds.length <= 0) {
            return null;
        }

        ContentResolver cr = mContext.getContentResolver();
        Uri uri = DataConstants.Contact.CONTENT_URI;
        String[] projection = DataConstants.Contact.ALL_COLUMNS;
        StringBuilder selectionBuilder = new StringBuilder(
                DataConstants.Contact.USER_ID + " in(");
        final int length = userIds.length;
        for (int i = 0; i < length; ++i) {
            if (i != length - 1) {
                selectionBuilder.append("?,");
            } else {
                selectionBuilder.append('?');
            }
        }
        selectionBuilder.append(')');
        String selection = selectionBuilder.toString();
        Cursor cursor = cr.query(uri, projection, selection, userIds, null);
        UserContact[] contacts = null;

        if (cursor.getCount() > 0) {
            final UserContact[] tmpContacts = new UserContact[cursor.getCount()];
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpContacts[idx++] = buildUserContact(cursor, columnsMap);
            } while (cursor.moveToNext());

            contacts = tmpContacts;
        }
        cursor.close();

        return contacts;
    }

    public UserContact[] getAllUserContact() {
        if (isInGuestMode()) {
            return null;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Contact.CONTENT_URI;
        final String[] projection = DataConstants.Contact.ALL_COLUMNS;
        final Cursor cursor = cr.query(uri, projection, null, null, null);
        UserContact[] contacts = null;

        if (cursor.getCount() > 0) {
            final UserContact[] tmpContacts = new UserContact[cursor.getCount()];
            final ColumnsMap columnsMap = new ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpContacts[idx++] = buildUserContact(cursor, columnsMap);
            } while (cursor.moveToNext());

            contacts = tmpContacts;
        }
        cursor.close();

        return contacts;
    }

    public List<String> getUserIds(Context context) {
        FollowingManager followingManager = FollowingManager.getInstance(context);
        UserContact[] contacts = getAllUserContact();
        FollowUser[] followUsers = followingManager.getAllFollowerUser();
        HashSet<String> set = new HashSet<>();

        if (contacts != null) {
            for (UserContact contact : contacts) {
                if (contact != null) {
                    set.add(contact.getUserId());
                }
            }
        }
        if (followUsers != null) {
            for (FollowUser followUser : followUsers) {
                if (followUser != null) {
                    set.add(followUser.getUserId());
                }
            }
        }
        return new ArrayList<>(set);
    }

    private UserContact buildUserContact(Cursor cursor) {
        return buildUserContact(cursor, new ColumnsMap(cursor));
    }

    private UserContact buildUserContact(Cursor cursor, ColumnsMap columnsMap) {
        final long id = cursor.getLong(columnsMap.mId);
        final String userId = cursor.getString(columnsMap.mUserId);
        final String labelCode = cursor.getString(columnsMap.mLabelCode);
        final String nickname = cursor.getString(columnsMap.mNickname);
        final String remarksName = cursor.getString(columnsMap.mRemarksName);
        final String mobile = cursor.getString(columnsMap.mMobile);
        final int sex = cursor.getInt(columnsMap.mSex);
        final long birthday = cursor.getLong(columnsMap.mBirthday);
        final int age = cursor.getInt(columnsMap.mAge);
        final int constellation = cursor.getInt(columnsMap.mConstellation);
        final String province = cursor.getString(columnsMap.mProvince);
        final String city = cursor.getString(columnsMap.mCity);
        final String school = cursor.getString(columnsMap.mSchool);
        final String signature = cursor.getString(columnsMap.mSignature);
        final String avatar = cursor.getString(columnsMap.mAvatar);
        final String avatarThumb = cursor.getString(columnsMap.mAvatarThumb);
        final String labels = cursor.getString(columnsMap.mLabels);
        final String appearanceFace = cursor.getString(columnsMap.mAppearanceFace);
        final String theme = cursor.getString(columnsMap.mTheme);
        final UserContact contact = new UserContact();

        contact.setId(id);
        contact.setUserId(userId);
        contact.setLabelCode(labelCode);
        contact.setNickname(nickname);
        contact.setRemarkName(remarksName);
        contact.setMobile(mobile);
        contact.setSex(sex);
        contact.setBirthday(birthday);
        contact.setAge(age);
        contact.setConstellation(constellation);
        contact.setProvince(province);
        contact.setCity(city);
        contact.setSchool(school);
        contact.setSignature(signature);
        contact.setAvatar(avatar);
        contact.setAvatarThumb(avatarThumb);
        contact.setLabelsByString(labels);
        contact.setAppearanceFace(appearanceFace);
        contact.setTheme(TextUtils.isEmpty(theme) ? null : UserTheme.fromThemeName(theme));

        return contact;
    }

    private void notifyContactDataChanged() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onContactDataChanged();
            } else {
                mListeners.remove(i);
            }
        }
    }

    private LocationInfo getPosition() {
        return mAccountManager.getLocation();
    }

    private LocationInfo getLocation() {
        return mCoreService.getCurrentLocationInfo();
    }

    private static class ContactDefriendedMeNotifier implements ListenerNotifier {

        private final String mFriendUserId;

        public ContactDefriendedMeNotifier(String friendUserId) {
            mFriendUserId = friendUserId;
        }

        @Override
        public void notify(IListener listener) {
            listener.onContactDefriendedMe(mFriendUserId);
        }
    }
}
