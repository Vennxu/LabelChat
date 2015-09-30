package com.ekuater.labelchat.delegate;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.ekuater.labelchat.data.DataConstants;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ChatMessageUtils;
import com.ekuater.labelchat.util.UUIDGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LinYong
 */
public class ChatManager extends BaseManager {

    public interface IListener extends BaseManager.IListener {
        public void onNewChatMessageReceived(ChatMessage chatMsg);

        public void onChatMessageDataChanged();
    }

    public static class AbsListener implements IListener {

        @Override
        public void onNewChatMessageReceived(ChatMessage chatMsg) {
        }

        @Override
        public void onChatMessageDataChanged() {
        }

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }
    }

    public interface ChatMessageObserver {
        public void onSending(String messageSession, long messageId);

        public void onSendResult(String messageSession, long messageId, int result);
    }

    private static ChatManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ChatManager(context.getApplicationContext());
        }
    }

    public static ChatManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private final List<WeakReference<IListener>> mListeners
            = new ArrayList<WeakReference<IListener>>();
    private final Map<String, WeakReference<ChatMessageObserver>> mMessageObserverMap
            = new HashMap<String, WeakReference<ChatMessageObserver>>();
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
        public void onNewChatMessageReceived(ChatMessage chatMsg) {
            notifyNewChatMessageReceived(chatMsg);
        }

        @Override
        public void onChatMessageSendResult(String messageSession, long messageId, int result) {
            notifyChatMessageSendResult(messageSession, messageId, result);
        }

        @Override
        public void onNewChatMessageSending(String messageSession, long messageId) {
            notifyNewChatMessageSending(messageSession, messageId);
        }
    };

    private final ContentObserver mContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            notifyChatMessageDataChanged();
        }
    };

    // Use getInstance()
    private ChatManager(Context context) {
        super(context);
        mContext = context;
        mCoreService.registerNotifier(mNotifier);
        mContext.getContentResolver().registerContentObserver(DataConstants.Chat.CONTENT_URI,
                true, mContentObserver);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private void notifyNewChatMessageReceived(ChatMessage chatMsg) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onNewChatMessageReceived(chatMsg);
            } else {
                mListeners.remove(i);
            }
        }
    }

    private void notifyChatMessageDataChanged() {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                listener.onChatMessageDataChanged();
            } else {
                mListeners.remove(i);
            }
        }
    }

    private void notifyChatMessageSendResult(String messageSession, long messageId, int result) {
        ChatMessageObserver observer = removeMessageObserver(messageSession);
        if (observer != null) {
            observer.onSendResult(messageSession, messageId, result);
        }
    }

    private void notifyNewChatMessageSending(String messageSession, long messageId) {
        ChatMessageObserver observer = getMessageObserver(messageSession);
        if (observer != null) {
            observer.onSending(messageSession, messageId);
        }
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

    public void sendMessage(ChatMessage chatMsg, ChatMessageObserver observer) {
        if (isInGuestMode()) {
            return;
        }

        String session = genMessageSession();
        mCoreService.requestSendChatMessage(session, chatMsg);
        putMessageObserver(session, observer);
    }

    public void resendMessage(ChatMessage chatMsg, ChatMessageObserver observer) {
        resendMessage(chatMsg.getId(), observer);
    }

    public void resendMessage(long messageId, ChatMessageObserver observer) {
        String session = genMessageSession();
        mCoreService.requestReSendChatMessage(session, messageId);
        putMessageObserver(session, observer);
    }

    public void deleteMessage(long messageId) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.deleteChatMessage(messageId);
    }

    public void clearTargetMessage(String target) {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.clearFriendChatMessage(target);
    }

    public void clearAllMessage() {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.clearAllChatMessage();
    }

    public ChatMessage[] getEveryTargetLastChatMessage() {
        if (isInGuestMode()) {
            return null;
        }

        final String lastTime = "last_time";
        final ContentResolver cr = mContext.getContentResolver();
        final int length = DataConstants.Chat.ALL_COLUMNS.length;
        final String[] projection = new String[length + 1];
        final String selection = DataConstants.Chat.TARGET_ID + ">0"
                + ") GROUP BY (" + DataConstants.Chat.TARGET_ID;
        //final String orderBy = lastTime + " DESC";
        ChatMessage[] chatMessages = null;

        System.arraycopy(DataConstants.Chat.ALL_COLUMNS, 0, projection, 0, length);
        projection[length] = "MAX(" + DataConstants.Chat.DATETIME + ") AS " + lastTime;

        final Cursor cursor = cr.query(DataConstants.Chat.CONTENT_URI, projection,
                selection, null, null/*orderBy*/);

        if (cursor.getCount() > 0) {
            final ChatMessage[] tmpChatMessages = new ChatMessage[cursor.getCount()];
            final ChatMessageUtils.ColumnsMap columnsMap = new ChatMessageUtils.ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpChatMessages[idx++] = buildChatMessage(cursor, columnsMap);
            } while (cursor.moveToNext());

            chatMessages = tmpChatMessages;
        }
        cursor.close();

        return chatMessages;
    }

    public ChatMessage getChatMessage(long messageId) {
        if (isInGuestMode()) {
            return null;
        }

        ChatMessage chatMessage = null;

        if (messageId >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(DataConstants.Chat.CONTENT_URI, messageId);
            final String[] projection = DataConstants.Chat.ALL_COLUMNS;
            final Cursor cursor = cr.query(uri, projection, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                chatMessage = buildChatMessage(cursor);
            }
            cursor.close();
        }

        return chatMessage;
    }

    public ChatMessage[] getTargetChatMessages(String targetId) {
        if (isInGuestMode()) {
            return null;
        }

        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Chat.CONTENT_URI;
        final String[] projection = DataConstants.Chat.ALL_COLUMNS;
        final String selection = DataConstants.Chat.TARGET_ID + "=?";
        final String[] selectionArgs = new String[]{
                targetId,
        };
        final String orderBy = DataConstants.Chat.DATETIME + " ASC";
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, orderBy);
        ChatMessage[] chatMessages = null;

        if (cursor.getCount() > 0) {
            final ChatMessage[] tmpChatMessages = new ChatMessage[cursor.getCount()];
            final ChatMessageUtils.ColumnsMap columnsMap = new ChatMessageUtils.ColumnsMap(cursor);
            int idx = 0;

            cursor.moveToFirst();
            do {
                tmpChatMessages[idx++] = buildChatMessage(cursor, columnsMap);
            } while (cursor.moveToNext());

            chatMessages = tmpChatMessages;
        }
        cursor.close();

        return chatMessages;
    }

    public int getTargetUnreadChatMessageCount(String targetId) {
        if (isInGuestMode()) {
            return 0;
        }

        final String totalCount = "total_count";
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Chat.CONTENT_URI;
        final String[] projection = {
                "COUNT(*) AS " + totalCount,
        };
        final String selection = DataConstants.Chat.TARGET_ID + "=? AND "
                + DataConstants.Chat.STATE + "=?";
        final String[] selectionArgs = new String[]{
                targetId,
                String.valueOf(ChatMessage.STATE_UNREAD),
        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(cursor.getColumnIndex(totalCount));
        }
        cursor.close();

        return count;
    }

    public int getTargetChatMessageDirectionCount(String targetId, int direction) {
        if (isInGuestMode()) {
            return 0;
        }

        final String totalCount = "total_count";
        final ContentResolver cr = mContext.getContentResolver();
        final Uri uri = DataConstants.Chat.CONTENT_URI;
        final String[] projection = {
                "COUNT(*) AS " + totalCount,
        };
        final String selection = DataConstants.Chat.TARGET_ID + "=? AND "
                + DataConstants.Chat.DIRECTION + "=? AND "
                + DataConstants.Chat.STATE + "<>?";
        final String[] selectionArgs = new String[]{
                targetId,
                String.valueOf(direction),
                String.valueOf(ChatMessage.STATE_SEND_FAILED),
        };
        final Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        int count = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(cursor.getColumnIndex(totalCount));
        }
        cursor.close();

        return count;
    }

    public void updateChatMessageState(long messageId, int state) {
        if (isInGuestMode()) {
            return;
        }

        if (messageId >= 0) {
            final ContentResolver cr = mContext.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(DataConstants.Chat.CONTENT_URI, messageId);
            final ContentValues values = new ContentValues();

            values.put(DataConstants.Chat.STATE, state);
            cr.update(uri, values, null, null);
        }
    }

    private ChatMessage buildChatMessage(Cursor cursor) {
        return ChatMessageUtils.buildChatMessage(cursor);
    }

    private ChatMessage buildChatMessage(Cursor cursor, ChatMessageUtils.ColumnsMap columnsMap) {
        return ChatMessageUtils.buildChatMessage(cursor, columnsMap);
    }

    private String genMessageSession() {
        return UUIDGenerator.generate();
    }

    private void putMessageObserver(String session, ChatMessageObserver observer) {
        if (!TextUtils.isEmpty(session) && observer != null) {
            synchronized (mMessageObserverMap) {
                mMessageObserverMap.put(session, new WeakReference<ChatMessageObserver>(observer));
            }
        }
    }

    private ChatMessageObserver getMessageObserver(String session) {
        synchronized (mMessageObserverMap) {
            WeakReference<ChatMessageObserver> ref = mMessageObserverMap.get(session);
            return (ref != null) ? ref.get() : null;
        }
    }

    private ChatMessageObserver removeMessageObserver(String session) {
        synchronized (mMessageObserverMap) {
            WeakReference<ChatMessageObserver> ref = mMessageObserverMap.remove(session);
            return (ref != null) ? ref.get() : null;
        }
    }
}
