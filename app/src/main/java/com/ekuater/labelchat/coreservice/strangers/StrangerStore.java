package com.ekuater.labelchat.coreservice.strangers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.contact.QueryUserInfoCommand;
import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.coreservice.event.ChatUserEvent;
import com.ekuater.labelchat.coreservice.event.ChatUserGotEvent;
import com.ekuater.labelchat.coreservice.strangers.dao.DBStranger;
import com.ekuater.labelchat.coreservice.strangers.dao.StrangerDBHelper;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/1/30.
 *
 * @author LinYong
 */
public class StrangerStore {

    private static final String TAG = StrangerStore.class.getSimpleName();

    private static final int MSG_ADD_TO_PENDING_LIST = 101;
    private static final int MSG_QUERY_STRANGER_RESULT = 102;

    private final ICoreServiceCallback callback;
    private final StrangerDBHelper dbHelper;
    private final Handler handler;

    private final List<String> pendingList;
    private final List<String> queryingList;

    private final List<String> userIdCache;
    private final EventBus chatEventBus;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_TO_PENDING_LIST:
                    addToPendingListAndQuery((String) msg.obj);
                    break;
                case MSG_QUERY_STRANGER_RESULT:
                    handleQueryStrangerResult((StrangerQueryResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private static class StrangerQueryResult {

        public final String userId;
        public final int result;
        public final String response;

        public StrangerQueryResult(String userId, int result, String response) {
            this.userId = userId;
            this.result = result;
            this.response = response;
        }
    }

    private class QueryStrangerHandler implements ICommandResponseHandler {

        private final String userId;

        public QueryStrangerHandler(String userId) {
            this.userId = userId;
        }

        @Override
        public void onResponse(RequestCommand command, int result, String response) {
            handler.sendMessage(handler.obtainMessage(MSG_QUERY_STRANGER_RESULT,
                    new StrangerQueryResult(userId, result, response)));
        }
    }

    public StrangerStore(Context context, ICoreServiceCallback callback) {
        this.callback = callback;
        dbHelper = new StrangerDBHelper(context);
        handler = new ProcessHandler(callback.getProcessLooper());
        pendingList = new ArrayList<String>();
        queryingList = new ArrayList<String>();
        userIdCache = new ArrayList<String>();
        chatEventBus = EventBusHub.getChatEventBus();
        chatEventBus.register(this);
    }

    public void deInit() {
        chatEventBus.unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ChatUserEvent event) {
        switch (event.getChatType()) {
            case PRIVATE:
                break;
            default:
                return;
        }

        String userId = event.getUserId();

        if (inUserIdCache(userId) || getStranger(userId) != null) {
            event.setSyncGotEvent(new ChatUserGotEvent(userId,
                    ChatUserGotEvent.UserType.STRANGER));
            chatEventBus.cancelEventDelivery(event);
            addToUserIdCache(userId);
        } else {
            addToPendingList(userId);
        }
    }

    public void addStranger(Stranger stranger) {
        dbHelper.addDBStranger(StrangerDBHelper.toDBStranger(stranger));
        addToUserIdCache(stranger.getUserId());
    }

    public Stranger getStranger(String userId) {
        DBStranger dbStranger = dbHelper.getDBStranger(userId);

        if (dbStranger != null) {
            addToUserIdCache(dbStranger.getUserId());
            return StrangerDBHelper.toStranger(dbStranger);
        } else {
            return null;
        }
    }

    public void deleteStranger(String userId) {
        deleteFromUserIdCache(userId);
        dbHelper.deleteDBStranger(userId);
    }

    public void clear() {
        dbHelper.deleteAll();
    }

    private void addToUserIdCache(String userId) {
        synchronized (userIdCache) {
            if (!TextUtils.isEmpty(userId) && !inUserIdCache(userId)) {
                int length = userIdCache.size();
                if (length > 20) {
                    userIdCache.remove(length - 1);
                }
                userIdCache.add(0, userId);
            }
        }
    }

    private void deleteFromUserIdCache(String userId) {
        synchronized (userIdCache) {
            Iterator<String> iterator = userIdCache.iterator();

            while (iterator.hasNext()) {
                if (iterator.next().equals(userId)) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean inUserIdCache(String userId) {
        synchronized (userIdCache) {
            for (String tmpUserId : userIdCache) {
                if (tmpUserId.equals(userId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean inQueryingList(String userId) {
        for (String tmpUserId : queryingList) {
            if (tmpUserId.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private boolean addToQueryingList(String userId) {
        if (!inQueryingList(userId)) {
            queryingList.add(userId);
            return true;
        } else {
            return false;
        }
    }

    private void removeFromQueryingList(String userId) {
        Iterator<String> iterator = queryingList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(userId)) {
                iterator.remove();
            }
        }
    }

    private boolean inPendingList(String userId) {
        for (String tmpUserId : pendingList) {
            if (tmpUserId.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private void addToPendingList(String userId) {
        handler.sendMessage(handler.obtainMessage(MSG_ADD_TO_PENDING_LIST, userId));
    }

    private void addToPendingListAndQuery(String userId) {
        if (!inPendingList(userId)) {
            pendingList.add(userId);
        }
        queryPendingStranger();
    }

    private void queryPendingStranger() {
        Iterator<String> iterator = pendingList.iterator();
        while (iterator.hasNext()) {
            String userId = iterator.next();

            if (addToQueryingList(userId)) {
                QueryUserInfoCommand command = new QueryUserInfoCommand();
                command.putParamStrangerUserId(userId);
                executeCommand(command, new QueryStrangerHandler(userId));
            }
            iterator.remove();
        }
    }

    private void handleQueryStrangerResult(StrangerQueryResult result) {
        removeFromQueryingList(result.userId);

        switch (result.result) {
            case ConstantCode.EXECUTE_RESULT_SUCCESS:
                try {
                    QueryUserInfoCommand.CommandResponse cmdResp
                            = new QueryUserInfoCommand.CommandResponse(result.response);
                    if (cmdResp.requestSuccess()) {
                        Stranger stranger = cmdResp.getUserInfo();
                        if (stranger != null) {
                            addStranger(stranger);
                            chatEventBus.post(new ChatUserGotEvent(stranger.getUserId(),
                                    ChatUserGotEvent.UserType.STRANGER));
                        }
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                break;
            default:
                addToPendingListAndQuery(result.userId);
                break;
        }
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        callback.preTreatCommand(command);
        callback.executeCommand(command.toRequestCommand(), handler);
    }
}
