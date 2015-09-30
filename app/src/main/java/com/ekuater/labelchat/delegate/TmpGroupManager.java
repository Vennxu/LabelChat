package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.datastruct.UserLabel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Linyong
 */
public class TmpGroupManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {

        /**
         * Group create result
         *
         * @param result result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
         * @param labels group label
         * @param group  new create group information
         */
        public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group);

        /**
         * Group dismiss result
         *
         * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
         * @param groupId groupId
         */
        public void onDismissGroupRequestResult(int result, String groupId);

        /**
         * Query group information from server result
         *
         * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
         * @param groupId groupId
         * @param group   group information
         */
        public void onQueryGroupInfoResult(int result, String groupId, TmpGroup group);

        /**
         * Member quit group request result
         *
         * @param result  result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
         * @param groupId groupId
         */
        public void onQuitGroupResult(int result, String groupId);

        /**
         * Get group system time result
         *
         * @param result    result code, such as ConstantCode.EXECUTE_RESULT_SUCCESS
         * @param groupId   groupId
         * @param groupTime group time return from server
         */
        public void onQueryGroupSystemTimeResult(int result, String groupId,
                                                 TmpGroupTime groupTime);

        /**
         * @param groupId       groupId
         * @param timeRemaining group dismiss time remaining
         */
        public void onGroupDismissRemind(String groupId, long timeRemaining);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
        }

        @Override
        public void onDismissGroupRequestResult(int result, String groupId) {
        }

        @Override
        public void onQueryGroupInfoResult(int result, String groupId, TmpGroup group) {
        }

        @Override
        public void onQuitGroupResult(int result, String groupId) {
        }

        @Override
        public void onQueryGroupSystemTimeResult(int result, String groupId,
                                                 TmpGroupTime groupTime) {
        }

        @Override
        public void onGroupDismissRemind(String groupId, long timeRemaining) {
        }
    }

    private interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static TmpGroupManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new TmpGroupManager(context.getApplicationContext());
        }
    }

    public static TmpGroupManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<WeakReference<IListener>> mListeners
            = new ArrayList<WeakReference<IListener>>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {

        @Override
        public void onCoreServiceConnected() {
            notifyListeners(new CoreServiceConnectedNotifier());
        }

        @Override
        public void onCoreServiceDied() {
            notifyListeners(new CoreServiceDiedNotifier());
        }

        @Override
        public void onCreateTmpGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
            notifyListeners(new CreateGroupRequestResultNotifier(result, labels, group));
        }

        @Override
        public void onDismissTmpGroupRequestResult(int result, String groupId) {
            notifyListeners(new DismissGroupRequestResultNotifier(result, groupId));
        }

        @Override
        public void onQueryTmpGroupInfoResult(int result, String groupId, TmpGroup group) {
            notifyListeners(new QueryGroupInfoResultNotifier(result, groupId, group));
        }

        @Override
        public void onQuitTmpGroupResult(int result, String groupId) {
            notifyListeners(new QuitGroupResultNotifier(result, groupId));
        }

        @Override
        public void onQueryTmpGroupSystemTimeResult(int result, String groupId,
                                                    TmpGroupTime groupTime) {
            notifyListeners(new QueryGroupSystemTimeResultNotifier(result, groupId, groupTime));
        }

        @Override
        public void onTmpGroupDismissRemind(String groupId, long timeRemaining) {
            notifyListeners(new GroupDismissRemindNotifier(groupId, timeRemaining));
        }
    };

    private TmpGroupManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }


    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }
            mListeners.add(new WeakReference<IListener>(listener));
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

    public void createGroupRequest(BaseLabel label, String[] members) {
        createGroupRequest(new BaseLabel[]{label}, members);
    }

    public void createGroupRequest(BaseLabel[] labels, String[] members) {
        mCoreService.tmpGroupCreateGroupRequest(labels, members);
    }

    public void dismissGroupRequest(String groupId, String reason) {
        mCoreService.tmpGroupDismissGroupRequest(groupId, reason);
    }

    public void queryGroupInfo(String groupId) {
        mCoreService.tmpGroupQueryGroupInfo(groupId);
    }

    public void quitGroup(String groupId) {
        mCoreService.tmpGroupQuitGroup(groupId);
    }

    public void queryGroupSystemTime(String groupId) {
        mCoreService.tmpGroupQueryGroupSystemTime(groupId);
    }

    public TmpGroup queryGroup(String groupId) {
        return mCoreService.tmpGroupQueryGroup(groupId);
    }

    public String[] queryAllGroupId() {
        return mCoreService.tmpGroupQueryAllGroupId();
    }

    public Stranger[] queryGroupMembers(String groupId) {
        return mCoreService.tmpGroupQueryGroupMembers(groupId);
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

    private static class CoreServiceConnectedNotifier implements ListenerNotifier {

        public CoreServiceConnectedNotifier() {
        }

        @Override
        public void notify(IListener listener) {
            listener.onCoreServiceConnected();
        }
    }

    private static class CoreServiceDiedNotifier implements ListenerNotifier {

        public CoreServiceDiedNotifier() {
        }

        @Override
        public void notify(IListener listener) {
            listener.onCoreServiceDied();
        }
    }

    private static class CreateGroupRequestResultNotifier
            implements ListenerNotifier {

        private final int mResult;
        private final BaseLabel[] mLabels;
        private final TmpGroup mGroup;

        public CreateGroupRequestResultNotifier(int result, BaseLabel[] labels, TmpGroup group) {
            mResult = result;
            mLabels = labels;
            mGroup = group;
        }

        @Override
        public void notify(IListener listener) {
            listener.onCreateGroupRequestResult(mResult, mLabels, mGroup);
        }
    }

    private static class DismissGroupRequestResultNotifier
            implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public DismissGroupRequestResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(IListener listener) {
            listener.onDismissGroupRequestResult(mResult, mGroupId);
        }
    }

    private static class QueryGroupInfoResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;
        private final TmpGroup mGroup;

        public QueryGroupInfoResultNotifier(int result, String groupId, TmpGroup group) {
            mResult = result;
            mGroupId = groupId;
            mGroup = group;
        }

        @Override
        public void notify(IListener listener) {
            listener.onQueryGroupInfoResult(mResult, mGroupId, mGroup);
        }
    }

    private static class QuitGroupResultNotifier implements ListenerNotifier {

        private final int mResult;
        private final String mGroupId;

        public QuitGroupResultNotifier(int result, String groupId) {
            mResult = result;
            mGroupId = groupId;
        }

        @Override
        public void notify(IListener listener) {
            listener.onQuitGroupResult(mResult, mGroupId);
        }
    }

    private static class QueryGroupSystemTimeResultNotifier implements ListenerNotifier {

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
        public void notify(IListener listener) {
            listener.onQueryGroupSystemTimeResult(mResult, mGroupId, mGroupTime);
        }
    }

    private static class GroupDismissRemindNotifier implements ListenerNotifier {

        private final String mGroupId;
        private final long mTimeRemaining;

        public GroupDismissRemindNotifier(String groupId, long timeRemaining) {
            mGroupId = groupId;
            mTimeRemaining = timeRemaining;
        }

        @Override
        public void notify(IListener listener) {
            listener.onGroupDismissRemind(mGroupId, mTimeRemaining);
        }
    }
}
