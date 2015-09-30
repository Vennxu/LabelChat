
package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Chat message class
 *
 * @author LinYong
 */
public class ChatMessage implements Parcelable, Comparable<ChatMessage> {

    public static final int CONVERSATION_PRIVATE = 0;
    public static final int CONVERSATION_GROUP = 1;
    public static final int CONVERSATION_LABEL_CHAT_ROOM = 2;
    public static final int CONVERSATION_NORMAL_CHAT_ROOM = 3;

    public static final int DIRECTION_ILLEGAL = -1; // illegal direction
    public static final int DIRECTION_RECV = 0;
    public static final int DIRECTION_SEND = 1;
    public static final int DIRECTION_COUNT = 2;

    public static final int TYPE_ILLEGAL = -1; // illegal type
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_VOICE = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_COUNT = 3;

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_READ = 101;
    public static final int STATE_UNREAD = 102;
    public static final int STATE_SENDING = 201;
    public static final int STATE_SEND_SUCCESS = 202;
    public static final int STATE_SEND_FAILED = 203;

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private static final String EMPTY_STRING = "";

    private long mId; // message id in database
    private int mType; // text, voice, image, or others
    private int mConversationType;
    private int mState;
    private String mContent;
    private String mPreview;
    private long mTime;

    private String mTargetId; // friend UserId, or GroupId
    private String mSenderId; // for group chat
    private String mMessageId;
    private int mDirection; // received or sent

    public ChatMessage() {
        mId = -1L;
        mType = TYPE_ILLEGAL;
        mConversationType = CONVERSATION_PRIVATE;
        mState = STATE_UNKNOWN;
        mDirection = DIRECTION_ILLEGAL;
        mPreview = null;
        mTime = getCurrentTime();
        mContent = EMPTY_STRING;
        mSenderId = EMPTY_STRING;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getConversationType() {
        return mConversationType;
    }

    public void setConversationType(int conversationType) {
        mConversationType = conversationType;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getPreview() {
        return mPreview;
    }

    public void setPreview(String preview) {
        mPreview = preview;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getTargetId() {
        return mTargetId;
    }

    public void setTargetId(String targetId) {
        mTargetId = targetId;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setDirection(int direction) {
        mDirection = direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mMessageId);
        dest.writeInt(mType);
        dest.writeInt(mConversationType);
        dest.writeInt(mState);
        dest.writeString(mContent);
        dest.writeString(mPreview);
        dest.writeLong(mTime);
        dest.writeString(mTargetId);
        dest.writeString(mSenderId);
        dest.writeInt(mDirection);
    }

    public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {

        @Override
        public ChatMessage createFromParcel(Parcel source) {
            ChatMessage msg = new ChatMessage();

            msg.mId = source.readLong();
            msg.mMessageId = source.readString();
            msg.mType = source.readInt();
            msg.mConversationType = source.readInt();
            msg.mState = source.readInt();
            msg.mContent = source.readString();
            msg.mPreview = source.readString();
            msg.mTime = source.readLong();
            msg.mTargetId = source.readString();
            msg.mSenderId = source.readString();
            msg.mDirection = source.readInt();

            return msg;
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    @Override
    public int compareTo(@NonNull ChatMessage another) {
        return (int) (mTime - another.mTime);
    }

    @Override
    public String toString() {
        return ("ChatMessage: "
                + (", targetId=" + mTargetId)
                + (", id=" + mId)
                + (", messageId=" + mMessageId)
                + (", type=" + mType)
                + (", state=" + mState)
                + (", content=" + mContent)
                + (", time=" + mTime)
                + (", direction=" + mDirection)
        );
    }
}
