package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/2/7.
 *
 * @author LinYong
 */
public class StrangerRecommendLabelMessage {

    private static final String TAG = StrangerRecommendLabelMessage.class.getSimpleName();

    private SystemLabel[] recommendLabels;
    private Stranger stranger;

    public StrangerRecommendLabelMessage() {
    }

    public SystemLabel[] getRecommendLabels() {
        return recommendLabels;
    }

    public void setRecommendLabels(SystemLabel[] recommendLabels) {
        this.recommendLabels = recommendLabels;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public static StrangerRecommendLabelMessage build(JSONObject json) throws JSONException {
        StrangerRecommendLabelMessage newMessage = null;

        if (json != null) {
            SystemLabel[] labels = LabelCmdUtils.toSystemLabelArray(json.getJSONArray(
                    SystemPushFields.FIELD_SYSTEM_LABELS));
            Stranger stranger = ContactCmdUtils.toStranger(
                    json.getJSONObject(SystemPushFields.FIELD_STRANGER));

            newMessage = new StrangerRecommendLabelMessage();
            newMessage.setRecommendLabels(labels);
            newMessage.setStranger(stranger);
        }

        return newMessage;
    }

    public static StrangerRecommendLabelMessage build(SystemPush push) {
        StrangerRecommendLabelMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_STRANGER_RECOMMEND_LABEL) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}
