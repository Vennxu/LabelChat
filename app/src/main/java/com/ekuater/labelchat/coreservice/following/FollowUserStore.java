package com.ekuater.labelchat.coreservice.following;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.following.FollowerListCommand;
import com.ekuater.labelchat.command.following.FollowingListCommand;
import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.event.FollowUserDataChangedEvent;
import com.ekuater.labelchat.coreservice.event.NewSystemPushEvent;
import com.ekuater.labelchat.coreservice.following.dao.DBFollowerUser;
import com.ekuater.labelchat.coreservice.following.dao.DBFollowingUser;
import com.ekuater.labelchat.coreservice.following.dao.FollowUserDBHelper;
import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/3/27.
 *
 * @author LinYong
 */
public class FollowUserStore implements Handler.Callback {

    private static final String TAG = FollowUserStore.class.getSimpleName();

    private static final int MSG_SYNC_ALL = 101;
    private static final int MSG_SYNC_FOLLOWING_USERS = 102;
    private static final int MSG_SYNC_FOLLOWER_USERS = 103;

    private final EventBus mCoreEventBus;
    private ICoreServiceCallback mCallback;
    private FollowUserDBHelper mDbHelper;
    private Handler mHandler;

    private int mSyncFollowingRetryTime;
    private int mSyncFollowerRetryTime;

    public FollowUserStore(Context context, ICoreServiceCallback callback) {
        mCallback = callback;
        mCoreEventBus = EventBusHub.getCoreEventBus();
        mDbHelper = new FollowUserDBHelper(context);
        mHandler = new Handler(callback.getProcessLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_SYNC_ALL:
                handleSyncAll();
                break;
            case MSG_SYNC_FOLLOWING_USERS:
                handleSyncFollowingUsers(msg.arg1, (String) msg.obj);
                break;
            case MSG_SYNC_FOLLOWER_USERS:
                handleSyncFollowerUsers(msg.arg1, (String) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    public void init() {
        mCoreEventBus.register(this, 100);
    }

    public void deInit() {
        mCoreEventBus.unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(NewSystemPushEvent pushEvent) {
        SystemPush systemPush = pushEvent.getSystemPush();

        switch (systemPush.getType()) {
            case SystemPushType.TYPE_BEEN_FOLLOWED:
                onBeenFollowed(systemPush);
                break;
            default:
                break;
        }
    }

    public void sync() {
        mHandler.removeMessages(MSG_SYNC_ALL);
        mHandler.sendEmptyMessage(MSG_SYNC_ALL);
    }

    public void clear() {
        mDbHelper.deleteAllFollowingUser();
        mDbHelper.deleteAllFollowerUser();
    }

    public FollowUser getFollowingUser(String userId) {
        DBFollowingUser followingUser = mDbHelper.getFollowingUser(userId);
        return followingUser != null ? Utils.toFollowUser(followingUser) : null;
    }

    public FollowUser[] getAllFollowingUser() {
        final List<DBFollowingUser> followingUsers = mDbHelper.getAllFollowingUser();
        final int count = followingUsers.size();
        final FollowUser[] followUsers = new FollowUser[count];

        for (int i = 0; i < count; ++i) {
            followUsers[i] = Utils.toFollowUser(followingUsers.get(i));
        }
        return followUsers;
    }

    public void addFollowingUser(FollowUser followUser) {
        mDbHelper.addFollowingUser(Utils.toFollowingUser(followUser));
        mDbHelper.updateFollowerUser(Utils.toFollowerUser(followUser));
        notifyFollowUserDataChanged();
    }

    public void deleteFollowingUser(String userId) {
        mDbHelper.deleteFollowingUser(userId);
        notifyFollowUserDataChanged();
    }

    public FollowUser getFollowerUser(String userId) {
        DBFollowerUser followerUser = mDbHelper.getFollowerUser(userId);
        return followerUser != null ? Utils.toFollowUser(followerUser) : null;
    }

    public FollowUser[] batchQueryFollowerUser(String[] userIds){
        return toFollowUser(mDbHelper.batchQueryFollowerUser(userIds));
    }

    public FollowUser[] getAllFollowerUser() {
        return toFollowUser(mDbHelper.getAllFollowerUser());
    }

    private FollowUser[] toFollowUser(List<DBFollowerUser> followerUsers) {
        final int count = followerUsers.size();
        final FollowUser[] followUsers = new FollowUser[count];

        for (int i = 0; i < count; ++i) {
            followUsers[i] = Utils.toFollowUser(followerUsers.get(i));
        }
        return followUsers;
    }

    public void addFollowerUser(FollowUser followUser) {
        mDbHelper.addFollowerUser(Utils.toFollowerUser(followUser));
        mDbHelper.updateFollowingUser(Utils.toFollowingUser(followUser));
        notifyFollowUserDataChanged();
    }

    public void deleteFollowerUser(String userId) {
        mDbHelper.deleteFollowerUser(userId);
        notifyFollowUserDataChanged();
    }

    private void handleSyncAll() {
        mSyncFollowingRetryTime = 3;
        syncFollowingUsers();
        mSyncFollowerRetryTime = 3;
        syncFollowerUsers();
    }

    private void syncFollowingUsers() {
        if (mSyncFollowingRetryTime > 0) {
            mSyncFollowingRetryTime--;

            FollowingListCommand command = new FollowingListCommand();
            ICommandResponseHandler handler = new ICommandResponseHandler() {
                @Override
                public void onResponse(RequestCommand command, int result, String response) {
                    mHandler.obtainMessage(MSG_SYNC_FOLLOWING_USERS, result, 0, response)
                            .sendToTarget();
                }
            };
            executeCommand(command, handler);
        }
    }

    private void handleSyncFollowingUsers(int result, String response) {
        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                mSyncFollowingRetryTime = 0;
                try {
                    FollowingListCommand.CommandResponse cmdResp
                            = new FollowingListCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        FollowUser[] followUsers = cmdResp.getFollowingUsers();
                        mDbHelper.deleteAllFollowingUser();
                        if (followUsers != null) {
                            for (FollowUser followUser : followUsers) {
                                mDbHelper.addFollowingUser(Utils.toFollowingUser(followUser));
                            }
                        }
                        notifyFollowUserDataChanged();
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                break;
            case ConstantCode.EXECUTE_RESULT_NETWORK_ERROR:
                syncFollowingUsers();
                break;
            default:
                break;
        }
    }

    private void syncFollowerUsers() {
        if (mSyncFollowerRetryTime > 0) {
            mSyncFollowerRetryTime--;

            FollowerListCommand command = new FollowerListCommand();
            ICommandResponseHandler handler = new ICommandResponseHandler() {
                @Override
                public void onResponse(RequestCommand command, int result, String response) {
                    mHandler.obtainMessage(MSG_SYNC_FOLLOWER_USERS, result, 0, response)
                            .sendToTarget();
                }
            };
            executeCommand(command, handler);
        }
    }

    private void handleSyncFollowerUsers(int result, String response) {
        switch (result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                mSyncFollowerRetryTime = 0;
                try {
                    FollowerListCommand.CommandResponse cmdResp
                            = new FollowerListCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        FollowUser[] followUsers = cmdResp.getFollowerUsers();
                        mDbHelper.deleteAllFollowerUser();
                        if (followUsers != null) {
                            for (FollowUser followUser : followUsers) {
                                mDbHelper.addFollowerUser(Utils.toFollowerUser(followUser));
                            }
                        }
                        notifyFollowUserDataChanged();
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                break;
            case ConstantCode.EXECUTE_RESULT_NETWORK_ERROR:
                syncFollowerUsers();
                break;
            default:
                break;
        }
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCallback.preTreatCommand(command);
        mCallback.executeCommand(command.toRequestCommand(), handler);
    }

    private void onBeenFollowed(SystemPush systemPush) {
        BeenFollowedMessage message = BeenFollowedMessage.build(systemPush);
        FollowUser followUser = message != null ? message.getFollowUser() : null;

        if (followUser != null) {
            switch (message.getFollowType()) {
                case BeenFollowedMessage.TYPE_FOLLOWED:
                    addFollowerUser(followUser);
                    break;
                case BeenFollowedMessage.TYPE_CANCEL_FOLLOW:
                    deleteFollowerUser(followUser.getUserId());
                    break;
                default:
                    break;
            }
        }
    }

    private void notifyFollowUserDataChanged() {
        mCoreEventBus.post(new FollowUserDataChangedEvent());
    }
}
