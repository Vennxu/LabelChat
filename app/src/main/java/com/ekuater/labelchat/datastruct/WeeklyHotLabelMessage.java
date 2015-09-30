package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class WeeklyHotLabelMessage {

    private SystemLabel[] mHotLabels;

    public WeeklyHotLabelMessage() {
    }

    public SystemLabel[] getHotLabels() {
        return mHotLabels;
    }

    public void setHotLabels(SystemLabel[] labels) {
        mHotLabels = labels;
    }

    public static WeeklyHotLabelMessage build(JSONObject json) {
        final SystemLabel[] labels = LabelCmdUtils.toSystemLabelArray(
                json.optJSONArray(SystemPushFields.FIELD_SYSTEM_LABELS));
        WeeklyHotLabelMessage newMessage = null;

        if (labels != null && labels.length > 0) {
            newMessage = new WeeklyHotLabelMessage();
            newMessage.setHotLabels(labels);
        }

        return newMessage;
    }

    public static WeeklyHotLabelMessage build(SystemPush push) {
        WeeklyHotLabelMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_WEEKLY_HOT_LABEL) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}
