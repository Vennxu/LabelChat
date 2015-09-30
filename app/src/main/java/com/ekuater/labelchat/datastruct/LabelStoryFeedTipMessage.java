package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Xu wenxiang
 */
public class LabelStoryFeedTipMessage implements Parcelable{

    public static final String TAG = LabelStoryFeedTipMessage.class.getSimpleName();
    public static final String DYNAMIC="1";
    public static final String ADD_LABEL="2";

    private String mFreidUserId;
    private String mFeedType;
    private String mLabelId;
    private String mLabelName;
    private String mUserId;

    @Override
    public String toString() {
        return "LabelStoryFeedTipMessage{" +
                "mFreidUserId='" + mFreidUserId + '\'' +
                ", mFeedType='" + mFeedType + '\'' +
                ", mLabelId='" + mLabelId + '\'' +
                ", mLabelName='" + mLabelName + '\'' +
                ", mUserId='" + mUserId + '\'' +
                '}';
    }

    public LabelStoryFeedTipMessage() {
    }

    public String getmFeedType() {
        return mFeedType;
    }

    public void setmFeedType(String mFeedType) {
        this.mFeedType = mFeedType;
    }

    public String getmFreidUserId() {
        return mFreidUserId;
    }

    public void setmFreidUserId(String mFreidUserId) {
        this.mFreidUserId = mFreidUserId;
    }

    public String getmLabelId() {
        return mLabelId;
    }

    public void setmLabelId(String mLabelId) {
        this.mLabelId = mLabelId;
    }

    public String getmLabelName() {
        return mLabelName;
    }

    public void setmLabelName(String mLabelName) {
        this.mLabelName = mLabelName;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public static LabelStoryFeedTipMessage build(JSONObject json) throws JSONException {
        LabelStoryFeedTipMessage newMessage = null;
        if (json != null) {
            String friendUserId= json.optString(SystemPushFields.FIELD_FRIEND_USER_ID);
            String feedVo= json.optString(SystemPushFields.FIELD_STORY_FEED_VO);
            String feedType =null;
            String labelId = null;
            String labelName = null;
            String userId =null;

            if (feedVo!=null){
                JSONObject jsonObject=new JSONObject(feedVo);
                 feedType = jsonObject.optString(SystemPushFields.FIELD_STORY_FEED_TYPE);
                 labelId = jsonObject.optString(SystemPushFields.FIELD_STORY_LABEL_ID);
                 labelName = jsonObject.optString(SystemPushFields.FIELD_STORY_LABEL_NAME);
                 userId = jsonObject.optString(SystemPushFields.FIELD_STORY_USER_ID);
            }
            if (!TextUtils.isEmpty(friendUserId)) {
                newMessage = new LabelStoryFeedTipMessage();
                newMessage.setmFreidUserId(friendUserId);
                newMessage.setmLabelId(labelId);
                newMessage.setmLabelName(labelName);
                newMessage.setmUserId(userId);
                newMessage.setmFeedType(feedType);
            }
        }

        return newMessage;
    }

    public static LabelStoryFeedTipMessage build(SystemPush push) {
        LabelStoryFeedTipMessage newMessage = null;
        if (push.getType() == SystemPushType.TYPE_LABEL_STORY_TIP) {
            try {
                L.w(TAG, push.getContent());
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }
        return newMessage;
    }
    public static ArrayList<LabelStoryFeedTipMessage> build(SystemPush[] systemPushs) {
        ArrayList<LabelStoryFeedTipMessage> listFeedTip =new ArrayList<LabelStoryFeedTipMessage>();
        for (SystemPush systemPush:systemPushs){
            LabelStoryFeedTipMessage newMessage = new LabelStoryFeedTipMessage();
                try {
                    L.w(TAG, systemPush.getContent());
                    newMessage = build(new JSONObject(systemPush.getContent()));
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
            listFeedTip.add(newMessage);
        }
        return listFeedTip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mFeedType);
        dest.writeString(mFreidUserId);
        dest.writeString(mLabelId);
        dest.writeString(mLabelName);
        dest.writeString(mUserId);
    }

    public static final Parcelable.Creator<LabelStoryFeedTipMessage> CREATOR = new Parcelable.Creator<LabelStoryFeedTipMessage>() {

        @Override
        public LabelStoryFeedTipMessage createFromParcel(Parcel source) {
            LabelStoryFeedTipMessage message = new LabelStoryFeedTipMessage();

            message.mFeedType = source.readString();
            message.mFreidUserId = source.readString();
            message.mLabelId = source.readString();
            message.mLabelName = source.readString();
            message.mUserId = source.readString();

            return message;
        }

        @Override
        public LabelStoryFeedTipMessage[] newArray(int size) {
            return new LabelStoryFeedTipMessage[size];
        }
    };
}
