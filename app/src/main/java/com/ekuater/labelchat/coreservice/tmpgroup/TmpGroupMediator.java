package com.ekuater.labelchat.coreservice.tmpgroup;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.tmpgroup.CreateGroupCommand;
import com.ekuater.labelchat.command.tmpgroup.DismissGroupCommand;
import com.ekuater.labelchat.command.tmpgroup.GroupInfoCommand;
import com.ekuater.labelchat.command.tmpgroup.GroupTimeCommand;
import com.ekuater.labelchat.command.tmpgroup.QuitGroupCommand;
import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.event.NewSystemPushEvent;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocalTmpGroupDismissedMessage;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupCreateMessage;
import com.ekuater.labelchat.datastruct.TmpGroupDismissMessage;
import com.ekuater.labelchat.datastruct.TmpGroupDismissRemindMessage;
import com.ekuater.labelchat.datastruct.TmpGroupMemberQuitMessage;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author LinYong
 */
public class TmpGroupMediator {

    private static final String TAG = TmpGroupMediator.class.getSimpleName();

    private static final int MSG_HANDLE_CREATE_GROUP_REQUEST_RESULT = 101;
    private static final int MSG_HANDLE_DISMISS_GROUP_REQUEST_RESULT = 102;
    private static final int MSG_HANDLE_QUERY_GROUP_INFO_RESULT = 103;
    private static final int MSG_HANDLE_QUIT_GROUP_RESULT = 104;
    private static final int MSG_HANDLE_QUERY_GROUP_SYSTEM_TIME_RESULT = 105;

    private interface ITmpGroupListenerNotifier {
        public void notify(ITmpGroupListener listener);
    }

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

    private final class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_CREATE_GROUP_REQUEST_RESULT:
                    handleCreateGroupRequestResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_DISMISS_GROUP_REQUEST_RESULT:
                    handleDismissGroupRequestResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_QUERY_GROUP_INFO_RESULT:
                    handleQueryGroupInfoResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_QUIT_GROUP_RESULT:
                    handleQuitGroupResult((CommandResult) msg.obj);
                    break;
                case MSG_HANDLE_QUERY_GROUP_SYSTEM_TIME_RESULT:
                    handleQueryGroupSystemTimeResult((CommandResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final Context mContext;
    private final GroupDBHelper mDBHelper;
    private final ICoreServiceCallback mCallback;
    private final Handler mHandler;
    private final List<WeakReference<ITmpGroupListener>> mListeners = new ArrayList<>();
    private final EventBus mCoreEventBus;

    public TmpGroupMediator(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCoreEventBus = EventBusHub.getCoreEventBus();
        mDBHelper = GroupDBHelper.getInstance(context);
        mCallback = callback;
        mHandler = new ProcessHandler(callback.getProcessLooper());
    }

    public final void registerListener(final ITmpGroupListener listener) {
        for (WeakReference<ITmpGroupListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<>(listener));
        unregisterListener(null);
    }

    public final void unregisterListener(final ITmpGroupListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    private void notifyListeners(ITmpGroupListenerNotifier notifier) {
        final List<WeakReference<ITmpGroupListener>> listeners = mListeners;

        for (int i = listeners.size() - 1; i >= 0; i--) {
            final ITmpGroupListener listener = listeners.get(i).get();
            if (listener != null) {
                try {
                    notifier.notify(listener);
                } catch (Exception e) {
                    L.w(TAG, e);
                }
            } else {
                listeners.remove(i);
            }
        }
    }

    public void onNewPushMessage(SystemPush systemPush) {
        if (systemPush != null) {
            switch (systemPush.getType()) {
                case SystemPushType.TYPE_TMP_GROUP_CREATE:
                    handleGroupCreated(systemPush);
                    break;
                case SystemPushType.TYPE_TMP_GROUP_DISMISS:
                    handleGroupDismissed(systemPush);
                    break;
                case SystemPushType.TYPE_TMP_GROUP_MEMBER_QUIT:
                    handleGroupMemberQuit(systemPush);
                    break;
                case SystemPushType.TYPE_TMP_GROUP_DISMISS_REMIND:
                    handleGroupDismissRemind(systemPush);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleGroupCreated(SystemPush systemPush) {
        TmpGroupCreateMessage message = TmpGroupCreateMessage.build(systemPush);

        if (message != null) {
            TmpGroup group = message.getGroup();
            addNewGroup(group);
            addGroupCreatedNotifyMessage(group);
        }
    }

    private void addGroupCreatedNotifyMessage(TmpGroup group) {
        ChatMessage chatMessage = new ChatMessage();
        String createUserId = group.getCreateUserId();

        chatMessage.setConversationType(ChatMessage.CONVERSATION_GROUP);
        chatMessage.setTargetId(group.getGroupId());
        chatMessage.setSenderId(createUserId);
        chatMessage.setTime(System.currentTimeMillis());
        chatMessage.setMessageId(String.valueOf(chatMessage.getTime()));
        chatMessage.setType(ChatMessage.TYPE_TEXT);
        chatMessage.setContent(mContext.getString(
                R.string.new_tmp_group_prompt_message));
        if (createUserId.equals(mCallback.getAccountUserId())) {
            chatMessage.setDirection(ChatMessage.DIRECTION_SEND);
            chatMessage.setState(ChatMessage.STATE_SEND_SUCCESS);
        } else {
            chatMessage.setDirection(ChatMessage.DIRECTION_RECV);
            chatMessage.setState(ChatMessage.STATE_UNREAD);
        }
        mCallback.addNewChatMessage(chatMessage);
    }

    private void handleGroupDismissed(SystemPush systemPush) {
        TmpGroupDismissMessage message = TmpGroupDismissMessage.build(systemPush);

        if (message != null) {
            final String groupId = message.getGroupId();
            final TmpGroup group = queryGroup(groupId);

            mCallback.clearChatHistory(groupId);
            deleteGroup(groupId);

            if (group != null) {
                // now add a new group dismiss local push message
                LocalTmpGroupDismissedMessage dismissedMessage
                        = new LocalTmpGroupDismissedMessage();
                dismissedMessage.setGroupId(groupId);
                dismissedMessage.setGroupName(group.getGroupName());
                dismissedMessage.setGroupAvatar(group.getGroupAvatar());
                dismissedMessage.setDismissTime(System.currentTimeMillis());
                dismissedMessage.setDismissMessage(mContext.getString(
                        R.string.tmp_group_dismiss_prompt_message));

                SystemPush groupDismissPush = dismissedMessage.toSystemPush();
                if (groupDismissPush != null) {
                    mCoreEventBus.post(new NewSystemPushEvent(systemPush));
                }
            }
        }
    }

    private void handleGroupMemberQuit(SystemPush systemPush) {
        TmpGroupMemberQuitMessage message = TmpGroupMemberQuitMessage.build(systemPush);

        if (systemPush != null) {
            removeMember(message.getGroupId(), message.getUserId());
        }
    }

    private void handleGroupDismissRemind(SystemPush systemPush) {
        TmpGroupDismissRemindMessage message = TmpGroupDismissRemindMessage.build(systemPush);

        if (message != null) {
            final String groupId = message.getGroupId();
            final long timeRemaining = message.getTimeRemaining();
            final TmpGroup group = queryGroup(groupId);

            if (group != null) {
                // Calibration local group create time using time remaining.
                final long localCreateTime = System.currentTimeMillis() + timeRemaining
                        - (group.getExpireTime() - group.getCreateTime());
                group.setLocalCreateTime(localCreateTime);
                group.setDismissRemindTime(timeRemaining);
                updateGroup(group);
                notifyGroupDismissRemind(groupId, timeRemaining);
            }
        }
    }

    public void createGroupRequest(BaseLabel[] labels, String[] members) {
        CreateGroupCommand command = (CreateGroupCommand) preTreatCommand(
                new CreateGroupCommand());
        command.putParamLabels(labels);
        command.putParamMembers(members);
        command.putParamNickname(SettingHelper.getInstance(mContext).getAccountNickname());
        ICommandResponseHandler handler = new ObjCmdRespHandler(labels) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_CREATE_GROUP_REQUEST_RESULT,
                        new CommandResult(result, response, mObj));
                mHandler.sendMessage(msg);
            }
        };
        executeCommand(command, handler);
    }

    private void handleCreateGroupRequestResult(CommandResult cmdResult) {
        int result = ConstantCode.TMP_GROUP_OPERATION_NETWORK_ERROR;
        TmpGroup group = null;

        switch (cmdResult.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    CreateGroupCommand.CommandResponse cmdResp
                            = new CreateGroupCommand.CommandResponse(cmdResult.response);
                    if (cmdResp.requestSuccess()) {
                        result = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                        group = cmdResp.getGroup();
                        addNewGroup(group);
                        addGroupCreatedNotifyMessage(group);
                    } else {
                        result = cmdToOptCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    result = ConstantCode.TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyCreateGroupRequestResult(result, (BaseLabel[]) cmdResult.extra, group);
    }

    public void dismissGroupRequest(String groupId, String reason) {
        DismissGroupCommand command = (DismissGroupCommand) preTreatCommand(
                new DismissGroupCommand());
        command.putParamGroupId(groupId);
        command.putParamReason(reason);
        ICommandResponseHandler handler = new ObjCmdRespHandler(groupId) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_DISMISS_GROUP_REQUEST_RESULT,
                        new CommandResult(result, response, mObj));
                mHandler.sendMessage(msg);
            }
        };
        executeCommand(command, handler);
    }

    private void handleDismissGroupRequestResult(CommandResult cmdResult) {
        int result = ConstantCode.TMP_GROUP_OPERATION_NETWORK_ERROR;

        switch (cmdResult.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    DismissGroupCommand.CommandResponse cmdResp
                            = new DismissGroupCommand.CommandResponse(cmdResult.response);
                    if (cmdResp.requestSuccess()) {
                        final String groupId = (String) cmdResult.extra;
                        result = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                        mCallback.clearChatHistory(groupId);
                        deleteGroup(groupId);
                    } else {
                        result = cmdToOptCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    result = ConstantCode.TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyDismissGroupRequestResult(result, (String) cmdResult.extra);
    }

    public void queryGroupInfo(String groupId) {
        GroupInfoCommand command = (GroupInfoCommand) preTreatCommand(
                new GroupInfoCommand());
        command.putParamGroupId(groupId);
        ICommandResponseHandler handler = new ObjCmdRespHandler(groupId) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_QUERY_GROUP_INFO_RESULT,
                        new CommandResult(result, response, mObj));
                mHandler.sendMessage(msg);
            }
        };
        executeCommand(command, handler);
    }

    private void handleQueryGroupInfoResult(CommandResult cmdResult) {
        int result = ConstantCode.TMP_GROUP_OPERATION_NETWORK_ERROR;
        TmpGroup group = null;

        switch (cmdResult.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    GroupInfoCommand.CommandResponse cmdResp
                            = new GroupInfoCommand.CommandResponse(cmdResult.response);
                    if (cmdResp.requestSuccess()) {
                        result = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                        group = cmdResp.getGroup();
                        updateGroup(group);
                    } else {
                        result = cmdToOptCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    result = ConstantCode.TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyQueryGroupInfoResult(result, (String) cmdResult.extra, group);
    }

    public void quitGroup(String groupId) {
        QuitGroupCommand command = (QuitGroupCommand) preTreatCommand(
                new QuitGroupCommand());
        command.putParamGroupId(groupId);
        ICommandResponseHandler handler = new ObjCmdRespHandler(groupId) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_QUIT_GROUP_RESULT,
                        new CommandResult(result, response, mObj));
                mHandler.sendMessage(msg);
            }
        };
        executeCommand(command, handler);
    }

    private void handleQuitGroupResult(CommandResult cmdResult) {
        int result = ConstantCode.TMP_GROUP_OPERATION_NETWORK_ERROR;

        switch (cmdResult.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    QuitGroupCommand.CommandResponse cmdResp
                            = new QuitGroupCommand.CommandResponse(cmdResult.response);
                    if (cmdResp.requestSuccess()) {
                        final String groupId = (String) cmdResult.extra;
                        result = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                        mCallback.clearChatHistory(groupId);
                        deleteGroup(groupId);
                    } else {
                        result = cmdToOptCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    result = ConstantCode.TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyQuitGroupResult(result, (String) cmdResult.extra);
    }

    public void queryGroupSystemTime(String groupId) {
        GroupTimeCommand command = (GroupTimeCommand) preTreatCommand(
                new GroupTimeCommand());
        command.putParamGroupId(groupId);
        ICommandResponseHandler handler = new ObjCmdRespHandler(groupId) {
            @Override
            public void onResponse(RequestCommand command, int result, String response) {
                Message msg = mHandler.obtainMessage(MSG_HANDLE_QUERY_GROUP_SYSTEM_TIME_RESULT,
                        new CommandResult(result, response, mObj));
                mHandler.sendMessage(msg);
            }
        };
        executeCommand(command, handler);
    }

    private void handleQueryGroupSystemTimeResult(CommandResult cmdResult) {
        int result = ConstantCode.TMP_GROUP_OPERATION_NETWORK_ERROR;
        TmpGroupTime groupTime = null;

        switch (cmdResult.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    GroupTimeCommand.CommandResponse cmdResp
                            = new GroupTimeCommand.CommandResponse(cmdResult.response);
                    if (cmdResp.requestSuccess()) {
                        result = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                        groupTime = cmdResp.getGroupTime();
                    } else {
                        result = cmdToOptCode(cmdResp.getErrorCode());
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    result = ConstantCode.TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR;
                }
                break;
            default:
                break;
        }

        notifyQueryGroupSystemTimeResult(result, (String) cmdResult.extra, groupTime);
    }

    public TmpGroup queryGroup(String groupId) {
        return mDBHelper.queryGroup(groupId);
    }

    public void deleteGroup(String groupId) {
        mDBHelper.deleteGroup(groupId);
    }

    public String[] queryAllGroupId() {
        return mDBHelper.queryAllGroupId();
    }

    public Stranger[] queryGroupMembers(String groupId) {
        return mDBHelper.queryGroupMembers(groupId);
    }

    private void addNewGroup(TmpGroup group) {
        // Set local information
        if (group.getLocalCreateTime() <= 0) {
            group.setLocalCreateTime(System.currentTimeMillis());
            group.setDismissRemindTime(0);
        }
        updateGroupInternal(group);
    }

    private void updateGroup(TmpGroup group) {
        TmpGroup oldGroup = queryGroup(group.getGroupId());

        if (oldGroup == null) {
            addNewGroup(group);
        } else {
            // Restore local information to group
            group.setLocalCreateTime(oldGroup.getLocalCreateTime());
            group.setDismissRemindTime(oldGroup.getDismissRemindTime());
            updateGroupInternal(group);
        }
    }

    private void updateGroupInternal(TmpGroup group) {
        mDBHelper.updateGroup(group);
    }

    private void removeMember(String groupId, String userId) {
        mDBHelper.removeMember(groupId, userId);
    }

    private void executeCommand(RequestCommand command, ICommandResponseHandler handler) {
        mCallback.executeCommand(command, handler);
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        executeCommand(command.toRequestCommand(), handler);
    }

    private BaseCommand preTreatCommand(BaseCommand command) {
        return mCallback.preTreatCommand(command);
    }

    private int cmdToOptCode(int cmdCode) {
        int code;

        switch (cmdCode) {
            case CommandErrorCode.REQUEST_SUCCESS:
                code = ConstantCode.TMP_GROUP_OPERATION_SUCCESS;
                break;
            case CommandErrorCode.SESSION_ID_INVALID:
                code = ConstantCode.TMP_GROUP_OPERATION_SESSION_INVALID;
                break;
            case CommandErrorCode.PARAM_EMPTY:
                code = ConstantCode.TMP_GROUP_OPERATION_EMPTY_PARAM;
                break;
            case CommandErrorCode.DATA_ALREADY_EXIST:
                code = ConstantCode.TMP_GROUP_OPERATION_GROUP_EXIST;
                break;
            case CommandErrorCode.DATA_NOT_EXIST:
                code = ConstantCode.TMP_GROUP_OPERATION_DATA_NOT_EXIST;
                break;
            case CommandErrorCode.GROUP_DISMISSED:
                code = ConstantCode.TMP_GROUP_OPERATION_GROUP_DISMISSED;
                break;
            case CommandErrorCode.SYSTEM_ERROR:
            default:
                code = ConstantCode.TMP_GROUP_OPERATION_SYSTEM_ERROR;
                break;
        }

        return code;
    }

    private void notifyCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
        notifyListeners(new CreateGroupRequestResultNotifier(result, labels, group));
    }

    private void notifyDismissGroupRequestResult(int result, String groupId) {
        notifyListeners(new DismissGroupRequestResultNotifier(result, groupId));
    }

    private void notifyQueryGroupInfoResult(int result, String groupId, TmpGroup group) {
        notifyListeners(new QueryGroupInfoResultNotifier(result, groupId, group));
    }

    private void notifyQuitGroupResult(int result, String groupId) {
        notifyListeners(new QuitGroupResultNotifier(result, groupId));
    }

    private void notifyQueryGroupSystemTimeResult(int result, String groupId,
                                                  TmpGroupTime groupTime) {
        notifyListeners(new QueryGroupSystemTimeResultNotifier(result, groupId, groupTime));
    }

    private void notifyGroupDismissRemind(String groupId, long timeRemaining) {
        notifyListeners(new GroupDismissRemindNotifier(groupId, timeRemaining));
    }

    private static class CreateGroupRequestResultNotifier
            implements ITmpGroupListenerNotifier {

        private final int mResult;
        private final BaseLabel[] mLabels;
        private final TmpGroup mGroup;

        public CreateGroupRequestResultNotifier(int result, BaseLabel[] labels, TmpGroup group) {
            mResult = result;
            mLabels = labels;
            mGroup = group;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onCreateGroupRequestResult(mResult, mLabels, mGroup);
        }
    }

    private static class DismissGroupRequestResultNotifier
            implements ITmpGroupListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public DismissGroupRequestResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onDismissGroupRequestResult(mResult, mGroupId);
        }
    }

    private static class QueryGroupInfoResultNotifier implements ITmpGroupListenerNotifier {

        private final int mResult;
        private final String mGroupId;
        private final TmpGroup mGroup;

        public QueryGroupInfoResultNotifier(int result, String groupId, TmpGroup group) {
            mResult = result;
            mGroupId = groupId;
            mGroup = group;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onQueryGroupInfoResult(mResult, mGroupId, mGroup);
        }
    }

    private static class QuitGroupResultNotifier implements ITmpGroupListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public QuitGroupResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onQuitGroupResult(mResult, mGroupId);
        }
    }

    private static class QueryGroupSystemTimeResultNotifier implements ITmpGroupListenerNotifier {

        private final int mResult;
        private final String mGroupId;
        private final TmpGroupTime mGroupTime;

        public QueryGroupSystemTimeResultNotifier(int result, String groupId,
                                                  TmpGroupTime groupTime) {
            mResult = result;
            mGroupId = groupId;
            mGroupTime = groupTime;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onQueryGroupSystemTimeResult(mResult, mGroupId, mGroupTime);
        }
    }

    private static class GroupDismissRemindNotifier implements ITmpGroupListenerNotifier {

        private final String mGroupId;
        private final long mTimeRemaining;

        public GroupDismissRemindNotifier(String groupId, long timeRemaining) {
            mGroupId = groupId;
            mTimeRemaining = timeRemaining;
        }

        @Override
        public void notify(ITmpGroupListener listener) {
            listener.onGroupDismissRemind(mGroupId, mTimeRemaining);
        }
    }
}
