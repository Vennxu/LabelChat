package com.ekuater.labelchat.coreservice.immediator;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.util.L;

import org.json.JSONObject;

import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;

/**
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
@MessageTag(value = "LC:PushMsg", flag = MessageTag.NONE)
public class RongIMPushMessage extends RongIMClient.MessageContent {

    private static final String TAG = RongIMPushMessage.class.getSimpleName();

    public static final String FIELD_PUSH_ID = CommandFields.Normal.PUSH_ID;

    private String mPushId;

    protected RongIMPushMessage() {
        super();
    }

    public RongIMPushMessage(byte[] data) {
        try {
            String content = new String(data, "UTF-8");

            JSONObject json = new JSONObject(content);
            setPushId(json.getString(FIELD_PUSH_ID));
        } catch (Exception e) {
            L.w(TAG, e);
        }
    }

    public RongIMPushMessage(Parcel in) {
        setPushId(in.readString());
    }

    public String getPushId() {
        return mPushId;
    }

    public void setPushId(String pushId) {
        mPushId = pushId;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPushId);
    }

    public static final Parcelable.Creator<RongIMPushMessage> CREATOR
            = new Parcelable.Creator<RongIMPushMessage>() {

        @Override
        public RongIMPushMessage createFromParcel(Parcel source) {
            return new RongIMPushMessage(source);
        }

        @Override
        public RongIMPushMessage[] newArray(int size) {
            return new RongIMPushMessage[size];
        }
    };
}
