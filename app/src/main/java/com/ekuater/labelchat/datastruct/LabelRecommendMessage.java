package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/1/26.
 *
 * @author LinYong
 */
public class LabelRecommendMessage {

    private static final String TAG = LabelRecommendMessage.class.getSimpleName();

    public static final int STATE_ACCEPTED = 100;
    public static final int STATE_REJECTED = 101;

    private SystemLabel[] recommendLabels;
    private String friendUserId;

    public LabelRecommendMessage() {
    }

    public SystemLabel[] getRecommendLabels() {
        return recommendLabels;
    }

    public void setRecommendLabels(SystemLabel[] recommendLabels) {
        this.recommendLabels = recommendLabels;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public static LabelRecommendMessage build(JSONObject json) throws JSONException {
        LabelRecommendMessage newMessage = null;

        if (json != null) {
            SystemLabel[] labels = LabelCmdUtils.toSystemLabelArray(json.getJSONArray(
                    SystemPushFields.FIELD_SYSTEM_LABELS));
            String friendUserId = json.getString(SystemPushFields.FIELD_FRIEND_USER_ID);

            newMessage = new LabelRecommendMessage();
            newMessage.setRecommendLabels(labels);
            newMessage.setFriendUserId(friendUserId);
        }

        return newMessage;
    }

    public static LabelRecommendMessage build(SystemPush push) {
        LabelRecommendMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_RECOMMEND_LABEL) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}
