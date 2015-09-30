
package com.ekuater.labelchat.coreservice.chatmessage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.chatmessage.dao.PendingChat;
import com.ekuater.labelchat.coreservice.chatmessage.dao.PendingChatDBHelper;
import com.ekuater.labelchat.coreservice.event.ChatUserEvent;
import com.ekuater.labelchat.coreservice.event.ChatUserGotEvent;
import com.ekuater.labelchat.data.DataConstants.Chat;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ChatMessageUtils;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.util.FileUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author LinYong
 */
public final class ChatMessageStore {

    private static final String TAG = ChatMessageStore.class.getSimpleName();
    private static final Uri BASE_URI = Chat.CONTENT_URI;

    private static void saveToDatabase(Context context, ChatMessage chatMessage) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = ChatMessageUtils.buildChatMessageValue(chatMessage);
        Uri uri = cr.insert(BASE_URI, values);
        long msgId = ContentUris.parseId(uri);

        if (msgId != -1L) {
            chatMessage.setId(msgId);
        }
    }

    private static void clearDatabase(Context context, String userId) {
        final String where = Chat.TARGET_ID + "=?";
        final String[] selectionArgs = {
                userId
        };
        context.getContentResolver().delete(BASE_URI, where, selectionArgs);
    }

    private static void clearDatabase(Context context) {
        context.getContentResolver().delete(BASE_URI, null, null);
    }

    private static boolean containsChatMessage(Context context, long messageId) {
        boolean ret = false;

        if (messageId >= 0) {
            final ContentResolver cr = context.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(BASE_URI, messageId);
            final String[] projection = new String[]{
                    Chat._ID,
            };
            final Cursor cursor = cr.query(uri, projection, null, null, null);
            ret = (cursor.getCount() > 0);
            cursor.close();
        }

        return ret;
    }

    private static void updateChatMessage(Context context, long messageId, ContentValues values) {
        if (containsChatMessage(context, messageId)) {
            final ContentResolver cr = context.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(BASE_URI, messageId);

            cr.update(uri, values, null, null);
        }
    }

    private static void updateChatMessageState(Context context, long messageId, int state) {
        final ContentValues values = new ContentValues();

        values.put(Chat.STATE, state);
        updateChatMessage(context, messageId, values);
    }

    private static ChatMessage getChatMessage(Context context, long messageId) {
        ChatMessage chatMsg = null;

        if (messageId >= 0) {
            final ContentResolver cr = context.getContentResolver();
            final Uri uri = ContentUris.withAppendedId(BASE_URI, messageId);
            final Cursor cursor = cr.query(uri, Chat.ALL_COLUMNS, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                chatMsg = ChatMessageUtils.buildChatMessage(cursor);
            }
            cursor.close();
        }

        return chatMsg;
    }

    private static final int MSG_LOAD_PENDING_CHATS_IN_DB = 101;

    private final Context mContext;
    private final ICoreServiceCallback mCallback;
    private final List<WeakReference<IChatMessageStoreListener>> mListeners
            = new ArrayList<WeakReference<IChatMessageStoreListener>>();
    private final LongSparseArray<String> mSendingMessageMap = new LongSparseArray<String>();
    private final PendingChatDBHelper pendingDBHelper;
    private final EventBus chatEventBus;
    private final Handler handler;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_PENDING_CHATS_IN_DB:
                    loadPendingChatsInDB();
                    break;
                default:
                    break;
            }
        }
    }

    public ChatMessageStore(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCallback = callback;
        pendingDBHelper = new PendingChatDBHelper(context);
        chatEventBus = EventBusHub.getChatEventBus();
        chatEventBus.register(this);
        handler = new ProcessHandler(callback.getProcessLooper());
        postLoadPendingChatsInDB();
    }

    public void deInit() {
        chatEventBus.unregister(this);
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ChatUserGotEvent event) {
        onChatUserGot(event);
    }

    public void registerListener(final IChatMessageStoreListener listener) {
        for (WeakReference<IChatMessageStoreListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<IChatMessageStoreListener>(listener));
        unregisterListener(null);
    }

    public void unregisterListener(final IChatMessageStoreListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    public void onNewMessageReceived(ChatMessage chatMessage) {
        switch (chatMessage.getConversationType()) {
            case ChatMessage.CONVERSATION_PRIVATE: {
                String userId = chatMessage.getTargetId();
                ChatUserEvent event = new ChatUserEvent(userId,
                        ChatUserEvent.ChatType.PRIVATE);
                postChatUserEvent(event, chatMessage);
                break;
            }
            case ChatMessage.CONVERSATION_LABEL_CHAT_ROOM: {
                String userId = chatMessage.getSenderId();
                ChatUserEvent event = new ChatUserEvent(userId,
                        ChatUserEvent.ChatType.LABEL_CHAT_ROOM);
                postChatUserEvent(event, chatMessage);
                break;
            }
            case ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM: {
                String userId = chatMessage.getSenderId();
                ChatUserEvent event = new ChatUserEvent(userId,
                        ChatUserEvent.ChatType.NORMAL_CHAT_ROOM);
                postChatUserEvent(event, chatMessage);
                break;
            }
            case ChatMessage.CONVERSATION_GROUP:
            default:
                onNewMessageReceivedInternal(chatMessage);
                break;
        }
    }

    private void postChatUserEvent(ChatUserEvent event, ChatMessage chatMessage) {
        chatEventBus.post(event);
        ChatUserGotEvent gotEvent = event.getSyncGotEvent();
        if (gotEvent != null) {
            onChatUserGot(gotEvent);
            onNewMessageReceivedInternal(chatMessage);
        } else {
            pendingDBHelper.addPendingChat(PendingChatDBHelper
                    .toPendingChat(chatMessage));
        }
    }

    private void onNewMessageReceivedInternal(ChatMessage chatMessage) {
        saveToDatabase(mContext, chatMessage);
        notifyNewMessageReceived(chatMessage);
    }

    public void sendNewMessage(String messageSession, ChatMessage chatMessage) {
        if (chatMessage == null) {
            return;
        }

        chatMessage.setState(ChatMessage.STATE_SENDING);
        if (TextUtils.isEmpty(chatMessage.getSenderId())) {
            chatMessage.setSenderId(getUserId());
        }
        saveToDatabase(mContext, chatMessage);
        sendNewMessageInternal(chatMessage);
        notifyNewMessageSending(messageSession, chatMessage);
        addSendingMessageMap(chatMessage.getId(), messageSession);
    }

    public void resendMessage(String messageSession, long messageId) {
        final ChatMessage chatMessage = getChatMessage(mContext, messageId);

        if (chatMessage != null) {
            chatMessage.setState(ChatMessage.STATE_SENDING);
            sendNewMessageInternal(chatMessage);
            updateChatMessageState(mContext, messageId, ChatMessage.STATE_SENDING);
            notifyNewMessageSending(messageSession, chatMessage);
            addSendingMessageMap(chatMessage.getId(), messageSession);
        } else {
            notifyMessageSentResult(messageSession, messageId,
                    ConstantCode.SEND_RESULT_EMPTY_MESSAGE);
        }
    }

    public void onNewContactAdded(UserContact contact) {
        if (contact != null) {
            final ChatMessage chatMessage = new ChatMessage();
            chatMessage.setTargetId(contact.getUserId());
            chatMessage.setTime(System.currentTimeMillis());
            chatMessage.setMessageId(String.valueOf(chatMessage.getTime()));
            chatMessage.setDirection(ChatMessage.DIRECTION_RECV);
            chatMessage.setState(ChatMessage.STATE_UNREAD);
            chatMessage.setType(ChatMessage.TYPE_TEXT);
            chatMessage.setContent(mContext.getString(
                    R.string.new_contact_prompt_message));
            onNewMessageReceived(chatMessage);
        }
    }

    public boolean onMessageSendResult(ChatMessage chatMessage, int result) {
        final int state = (ConstantCode.SEND_RESULT_SUCCESS == result)
                ? ChatMessage.STATE_SEND_SUCCESS : ChatMessage.STATE_SEND_FAILED;
        final long messageId = chatMessage.getId();
        String messageSession = removeSendingMessageMap(messageId);
        boolean ret = false;

        if (!TextUtils.isEmpty(messageSession)) {
            updateChatMessageState(mContext, messageId, state);
            notifyMessageSentResult(messageSession, messageId, result);
            ret = true;
        }

        return ret;
    }

    public void addNewChatMessage(ChatMessage chatMessage) {
        onNewMessageReceived(chatMessage);
    }

    private static boolean renameDir(File src, File dest, int retry) {
        boolean _ret = false;

        for (int i = 0; i < retry; ++i) {
            if (src.renameTo(dest)) {
                _ret = true;
                break;
            }
        }

        return _ret;
    }

    private static void deleteFile(File file) {
        if (!file.delete()) {
            L.w(TAG, "deleteFile(), delete file failed:" + file);
        }
    }

    public void deleteByMessageId(long messageId) {
        final ChatMessage chatMsg = getChatMessage(mContext, messageId);

        if (chatMsg != null) {
            Uri uri = ContentUris.withAppendedId(BASE_URI, messageId);
            mContext.getContentResolver().delete(uri, null, null);

            switch (chatMsg.getType()) {
                case ChatMessage.TYPE_VOICE: {
                    File file = new File(EnvConfig.getVoiceChatMsgDirectory(
                            chatMsg.getTargetId()), chatMsg.getContent());
                    deleteFile(file);
                    break;
                }
                case ChatMessage.TYPE_IMAGE: {
                    File file = new File(EnvConfig.getImageChatMsgDirectory(
                            chatMsg.getTargetId()), chatMsg.getContent());
                    deleteFile(file);
                    file = new File(EnvConfig.getImageChatMsgThumbnailDirectory(
                            chatMsg.getTargetId()), chatMsg.getPreview());
                    deleteFile(file);
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void clear(final String userId) {
        // clear database first
        clearDatabase(mContext, userId);
        pendingDBHelper.deleteTarget(userId);
        // clear voice and image message from local.
        Runnable clearTask = new Runnable() {
            @Override
            public void run() {
                File voiceMsgDir = EnvConfig.getVoiceChatMsgDirectory(userId).getAbsoluteFile();
                File imageMsgDir = EnvConfig.getImageChatMsgDirectory(userId).getAbsoluteFile();
                File delVoiceMsgDir = new File(voiceMsgDir.getParentFile(),
                        UniqueFileName.getUniqueFileName());
                File delImageMsgDir = new File(imageMsgDir.getParentFile(),
                        UniqueFileName.getUniqueFileName());

                renameDir(voiceMsgDir, delVoiceMsgDir, 3);
                renameDir(imageMsgDir, delImageMsgDir, 3);
                FileUtils.deleteFolder(delVoiceMsgDir);
                FileUtils.deleteFolder(delImageMsgDir);
            }
        };
        mCallback.execute(clearTask);
    }

    public void clear() {
        // clear database first
        clearDatabase(mContext);
        pendingDBHelper.deleteAll();
        // clear voice and image message from local.
        Runnable clearTask = new Runnable() {
            @Override
            public void run() {
                File chatMsgDir = EnvConfig.CHAT_MSG_DIR.getAbsoluteFile();
                File delChatMsgDir = new File(chatMsgDir.getParentFile(),
                        UniqueFileName.getUniqueFileName());

                renameDir(chatMsgDir, delChatMsgDir, 3);
                if (!chatMsgDir.mkdirs()) {
                    L.w(TAG, "clear(), make chat msg dir failed.");
                }
                FileUtils.deleteFolder(delChatMsgDir);
            }
        };
        mCallback.execute(clearTask);
    }

    private void addSendingMessageMap(long messageId, String messageSession) {
        synchronized (mSendingMessageMap) {
            mSendingMessageMap.put(messageId, messageSession);
        }
    }

    private String removeSendingMessageMap(long messageId) {
        synchronized (mSendingMessageMap) {
            String messageSession = mSendingMessageMap.get(messageId);
            mSendingMessageMap.delete(messageId);
            return messageSession;
        }
    }

    private void onChatUserGot(ChatUserGotEvent event) {
        String userId = event.getUserId();
        int[] conversationTypes = getSupportedConversationTypes(event);
        List<PendingChat> pendingChats = pendingDBHelper.getSenderPendingChats(
                userId, conversationTypes);

        pendingDBHelper.deletePendingChats(pendingChats);
        for (PendingChat pendingChat : pendingChats) {
            onNewMessageReceivedInternal(PendingChatDBHelper.toChatMessage(pendingChat));
        }
    }

    private void postLoadPendingChatsInDB() {
        handler.sendEmptyMessageDelayed(MSG_LOAD_PENDING_CHATS_IN_DB, 500);
    }

    private void loadPendingChatsInDB() {
        List<PendingChat> pendingChats = pendingDBHelper.getEveryTargetLastPendingChat();

        if (pendingChats != null && pendingChats.size() > 0) {
            for (PendingChat pendingChat : pendingChats) {
                String userId = getPendingChatSenderId(pendingChat);
                ChatUserEvent event = new ChatUserEvent(userId,
                        toChatType(pendingChat.getConversationType()));

                chatEventBus.post(event);
                ChatUserGotEvent gotEvent = event.getSyncGotEvent();
                if (gotEvent != null) {
                    onChatUserGot(gotEvent);
                }
            }
        }
    }

    private String getPendingChatSenderId(PendingChat pendingChat) {
        String userId;

        switch (pendingChat.getConversationType()) {
            case ChatMessage.CONVERSATION_GROUP:
            case ChatMessage.CONVERSATION_LABEL_CHAT_ROOM:
                userId = pendingChat.getSenderId();
                break;
            case ChatMessage.CONVERSATION_PRIVATE:
            default:
                userId = pendingChat.getTargetId();
                break;
        }

        return userId;
    }

    private String getUserId() {
        return mCallback.getAccountUserId();
    }

    private void notifyNewMessageSending(String messageSession, ChatMessage chatMsg) {
        final long messageId = chatMsg.getId();

        for (WeakReference<IChatMessageStoreListener> ref : mListeners) {
            IChatMessageStoreListener listener = ref.get();
            if (listener != null) {
                try {
                    listener.onNewMessageSending(messageSession, messageId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifyNewMessageReceived(ChatMessage chatMsg) {
        for (WeakReference<IChatMessageStoreListener> ref : mListeners) {
            IChatMessageStoreListener listener = ref.get();
            if (listener != null) {
                try {
                    listener.onNewMessageReceived(chatMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifyMessageSentResult(String messageSession, long messageId, int result) {
        for (WeakReference<IChatMessageStoreListener> ref : mListeners) {
            IChatMessageStoreListener listener = ref.get();
            if (listener != null) {
                try {
                    listener.onMessageSendResult(messageSession, messageId, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNewMessageInternal(ChatMessage chatMessage) {
        try {
            mCallback.sendNewChatMessage(chatMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChatUserEvent.ChatType toChatType(int conversationType) {
        ChatUserEvent.ChatType chatType;

        switch (conversationType) {
            case ChatMessage.CONVERSATION_PRIVATE:
                chatType = ChatUserEvent.ChatType.PRIVATE;
                break;
            case ChatMessage.CONVERSATION_LABEL_CHAT_ROOM:
                chatType = ChatUserEvent.ChatType.LABEL_CHAT_ROOM;
                break;
            case ChatMessage.CONVERSATION_GROUP:
                chatType = ChatUserEvent.ChatType.GROUP;
                break;
            case ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM:
                chatType = ChatUserEvent.ChatType.NORMAL_CHAT_ROOM;
                break;
            default:
                chatType = ChatUserEvent.ChatType.PRIVATE;
                break;
        }

        return chatType;
    }

    private int[] getSupportedConversationTypes(ChatUserGotEvent event) {
        int[] types;

        switch (event.getUserType()) {
            case STRANGER:
                types = new int[]{
                        ChatMessage.CONVERSATION_PRIVATE
                };
                break;
            case LITE_STRANGER:
                types = new int[]{
                        ChatMessage.CONVERSATION_LABEL_CHAT_ROOM,
                        ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM,
                };
                break;
            case CONTACT:
            default:
                types = new int[]{
                        ChatMessage.CONVERSATION_PRIVATE,
                        ChatMessage.CONVERSATION_GROUP,
                        ChatMessage.CONVERSATION_LABEL_CHAT_ROOM,
                        ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM,
                };
                break;
        }

        return types;
    }
}
