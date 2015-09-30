package com.ekuater.labelchat.datastruct;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.ekuater.labelchat.data.DataConstants;

/**
 * @author LinYong
 */
public class ChatMessageUtils {

    public static ChatMessage buildChatMessage(Cursor cursor) {
        return buildChatMessage(cursor, new ColumnsMap(cursor));
    }

    public static ChatMessage buildChatMessage(Cursor cursor, ColumnsMap columnsMap) {
        final long id = cursor.getLong(columnsMap.mId);
        final String messageId = cursor.getString(columnsMap.mMessageId);
        final int type = cursor.getInt(columnsMap.mType);
        final int state = cursor.getInt(columnsMap.mState);
        final String content = cursor.getString(columnsMap.mContent);
        final String preview = cursor.getString(columnsMap.mPreview);
        final long time = cursor.getLong(columnsMap.mTime);
        final String userId = cursor.getString(columnsMap.mTargetId);
        final int direction = cursor.getInt(columnsMap.mDirection);
        final int conversation = cursor.getInt(columnsMap.mConversation);
        final String senderId = cursor.getString(columnsMap.mSenderId);

        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setId(id);
        chatMsg.setMessageId(messageId);
        chatMsg.setType(type);
        chatMsg.setState(state);
        chatMsg.setContent(content);
        chatMsg.setPreview(preview);
        chatMsg.setTime(time);
        chatMsg.setTargetId(userId);
        chatMsg.setDirection(direction);
        chatMsg.setConversationType(conversation);
        chatMsg.setSenderId(senderId);

        return chatMsg;
    }

    public static ContentValues buildChatMessageValue(ChatMessage chatMessage) {
        if (chatMessage == null) {
            throw new NullPointerException("Null ChatMessage");
        }

        final ContentValues values = new ContentValues();
        final String messageId = TextUtils.isEmpty(chatMessage.getMessageId())
                ? String.valueOf(chatMessage.getTime()) : chatMessage.getMessageId();

        values.put(DataConstants.Chat.TARGET_ID, chatMessage.getTargetId());
        values.put(DataConstants.Chat.MESSAGE_ID, messageId);
        values.put(DataConstants.Chat.DIRECTION, chatMessage.getDirection());
        values.put(DataConstants.Chat.DATETIME, chatMessage.getTime());
        values.put(DataConstants.Chat.STATE, chatMessage.getState());
        values.put(DataConstants.Chat.TYPE, chatMessage.getType());
        values.put(DataConstants.Chat.CONTENT, chatMessage.getContent());
        values.put(DataConstants.Chat.PREVIEW, chatMessage.getPreview());
        values.put(DataConstants.Chat.CONVERSATION, chatMessage.getConversationType());
        values.put(DataConstants.Chat.SENDER_ID, chatMessage.getSenderId());

        return values;
    }

    public static class ColumnsMap {

        public final int mId;
        public final int mMessageId;
        public final int mTargetId;
        public final int mDirection;
        public final int mTime;
        public final int mState;
        public final int mType;
        public final int mContent;
        public final int mPreview;
        public final int mConversation;
        public final int mSenderId;

        private static int getColumnIndex(Cursor cursor, String columnName) {
            return cursor.getColumnIndex(columnName);
        }

        public ColumnsMap(Cursor cursor) {
            mId = getColumnIndex(cursor, DataConstants.Chat._ID);
            mMessageId = getColumnIndex(cursor, DataConstants.Chat.MESSAGE_ID);
            mTargetId = getColumnIndex(cursor, DataConstants.Chat.TARGET_ID);
            mDirection = getColumnIndex(cursor, DataConstants.Chat.DIRECTION);
            mTime = getColumnIndex(cursor, DataConstants.Chat.DATETIME);
            mState = getColumnIndex(cursor, DataConstants.Chat.STATE);
            mType = getColumnIndex(cursor, DataConstants.Chat.TYPE);
            mContent = getColumnIndex(cursor, DataConstants.Chat.CONTENT);
            mPreview = getColumnIndex(cursor, DataConstants.Chat.PREVIEW);
            mConversation = getColumnIndex(cursor, DataConstants.Chat.CONVERSATION);
            mSenderId = getColumnIndex(cursor, DataConstants.Chat.SENDER_ID);
        }
    }
}
