package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.chatroom.JoinChatRoomCommand;
import com.ekuater.labelchat.command.chatroom.ListChatRoomCommand;
import com.ekuater.labelchat.command.chatroom.MemberCountCommand;
import com.ekuater.labelchat.command.chatroom.QuitChatRoomCommand;
import com.ekuater.labelchat.command.chatroom.RequestMembersCommand;
import com.ekuater.labelchat.datastruct.ChatRoom;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Leo on 2015/3/5.
 *
 * @author LinYong
 */
public class NormalChatRoomManager extends BaseManager {

    private static final String TAG = NormalChatRoomManager.class.getSimpleName();

    // List result enum
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int RESULT_QUERY_FAILURE = 2;
    public static final int RESULT_RESPONSE_DATA_ERROR = 3;

    public interface IListener extends BaseManager.IListener {

        /**
         * Notify join normal chat room result
         *
         * @param chatRoomId normal chat room id
         * @param result     result 0 success or other failed
         */
        public void onJoinChatRoomResult(String chatRoomId, int result);

        /**
         * Notify quit normal chat room result
         *
         * @param chatRoomId normal chat room id
         * @param result     result 0 success or other failed
         */
        public void onQuitChatRoomResult(String chatRoomId, int result);
    }

    public interface ChatRoomListObserver {
        public void onListResult(int result, ChatRoom[] chatRooms);
    }

    public interface QueryMembersObserver {
        public void onQueryResult(int result, LiteStranger[] members);
    }

    public interface QueryMemberCountObserver {
        public void onQueryResult(int result, int count);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onJoinChatRoomResult(String chatRoomId, int result) {
        }

        @Override
        public void onQuitChatRoomResult(String chatRoomId, int result) {
        }
    }

    private interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static NormalChatRoomManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new NormalChatRoomManager(context.getApplicationContext());
        }
    }

    public static NormalChatRoomManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final List<WeakReference<IListener>> mListeners
            = new ArrayList<WeakReference<IListener>>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onJoinNormalChatRoomResult(String chatRoomId, int result) {
            notifyListeners(new JoinChatRoomResultNotifier(chatRoomId, result));
            if (result != 0) {
                clearChatRoom(chatRoomId);
            }
        }

        @Override
        public void onQuitNormalChatRoomResult(String chatRoomId, int result) {
            notifyListeners(new QuitChatRoomResultNotifier(chatRoomId, result));
        }
    };
    private final Map<String, AtomicInteger> mEnterMap = new HashMap<String, AtomicInteger>();
    private final Handler mHandler;

    private NormalChatRoomManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
        mHandler = new Handler(context.getMainLooper());
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

    public void joinChatRoom(final String chatRoomId) {
        if (enterChatRoom(chatRoomId)) {
            mCoreService.joinNormalChatRoom(chatRoomId);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyListeners(new JoinChatRoomResultNotifier(chatRoomId, 0));
                }
            });
        }
    }

    public void quitChatRoom(final String chatRoomId) {
        if (exitChatRoom(chatRoomId)) {
            mCoreService.quitNormalChatRoom(chatRoomId);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyListeners(new QuitChatRoomResultNotifier(chatRoomId, 0));
                }
            });
        }
    }

    private boolean enterChatRoom(String chatRoomId) {
        synchronized (mEnterMap) {
            AtomicInteger count = mEnterMap.get(chatRoomId);

            if (count == null) {
                count = new AtomicInteger(1);
                mEnterMap.put(chatRoomId, count);
                return true;
            } else {
                count.incrementAndGet();
                return false;
            }
        }
    }

    private boolean exitChatRoom(String chatRoomId) {
        synchronized (mEnterMap) {
            AtomicInteger count = mEnterMap.get(chatRoomId);

            if (count != null && count.decrementAndGet() <= 0) {
                mEnterMap.remove(chatRoomId);
                return true;
            } else {
                return false;
            }
        }
    }

    private void clearChatRoom(String chatRoomId) {
        synchronized (mEnterMap) {
            mEnterMap.remove(chatRoomId);
        }
    }

    public void sendJoinChatRoomMessage(String chatRoomId) {
        sendJoinChatRoomMessage(chatRoomId, null);
    }

    public void sendJoinChatRoomMessage(String chatRoomId, FunctionCallListener listener) {
        JoinChatRoomCommand command = new JoinChatRoomCommand(getSession(), getUserId());
        command.putParamChatRoomId(chatRoomId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void sendQuitChatRoomMessage(String chatRoomId) {
        sendQuitChatRoomMessage(chatRoomId, null);
    }

    public void sendQuitChatRoomMessage(String chatRoomId, FunctionCallListener listener) {
        QuitChatRoomCommand command = new QuitChatRoomCommand(getSession(), getUserId());
        command.putParamChatRoomId(chatRoomId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void listChatRooms(ChatRoomListObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        ListChatRoomCommand command = new ListChatRoomCommand(getSession());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ChatRoomListObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                ChatRoomListObserver observer = (ChatRoomListObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onListResult(RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ListChatRoomCommand.CommandResponse cmdResp
                            = new ListChatRoomCommand.CommandResponse(response);
                    ChatRoom[] chatRooms = null;
                    int _ret = RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        chatRooms = cmdResp.getChatRooms();
                        _ret = RESULT_SUCCESS;
                    }

                    observer.onListResult(_ret, chatRooms);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onListResult(RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryChatRoomMembers(String chatRoomId, QueryMembersObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(chatRoomId)) {
            observer.onQueryResult(RESULT_ILLEGAL_ARGUMENTS, null);
        }

        RequestMembersCommand command = new RequestMembersCommand(getSession());
        command.putParamChatRoomId(chatRoomId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof QueryMembersObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                QueryMembersObserver observer = (QueryMembersObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    RequestMembersCommand.CommandResponse cmdResp
                            = new RequestMembersCommand.CommandResponse(response);
                    LiteStranger[] members = null;
                    int _ret = RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        members = cmdResp.getMembers();
                        _ret = RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, members);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryChatRoomMemberCount(String chatRoomId, QueryMemberCountObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(chatRoomId)) {
            observer.onQueryResult(RESULT_ILLEGAL_ARGUMENTS, -1);
        }

        MemberCountCommand command = new MemberCountCommand(getSession());
        command.putParamChatRoomId(chatRoomId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof QueryMemberCountObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                QueryMemberCountObserver observer = (QueryMemberCountObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, -1);
                    return;
                }

                try {
                    MemberCountCommand.CommandResponse cmdResp
                            = new MemberCountCommand.CommandResponse(response);
                    int count = -1;
                    int _ret = RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        count = cmdResp.getMemberCount();
                        _ret = RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, count);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, -1);
            }
        };
        executeCommand(command, handler);
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

    private static class JoinChatRoomResultNotifier implements ListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public JoinChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(IListener listener) {
            listener.onJoinChatRoomResult(chatRoomId, result);
        }
    }

    private static class QuitChatRoomResultNotifier implements ListenerNotifier {

        private final String chatRoomId;
        private final int result;

        public QuitChatRoomResultNotifier(String chatRoomId, int result) {
            this.chatRoomId = chatRoomId;
            this.result = result;
        }

        @Override
        public void notify(IListener listener) {
            listener.onQuitChatRoomResult(chatRoomId, result);
        }
    }
}
