package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/5/7.
 */
public class InteractMessage {

    private int pushType;
    private long time;
    private PushInteract interact;

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public PushInteract getInteract() {
        return interact;
    }

    public void setInteract(PushInteract interact) {
        this.interact = interact;
    }

    public static InteractMessage build(JSONObject json) throws JSONException {
        InteractMessage newMessage = null;
        if (json != null) {
            newMessage = new InteractMessage();
            PushInteract interact = new PushInteract();
            interact.setInteractType(json.optString(CommandFields.Interest.INTERACT_TYPE));
            interact.setInteractObject(json.optString(CommandFields.Interest.INTERACT_OBJECT));
            interact.setInteractOperate(json.optString(CommandFields.Interest.INTERACT_OPERATE));
            interact.setObjectType(json.optString(CommandFields.Interest.OBJECT_TYPE));
            interact.setSelectColor(json.optString(CommandFields.Interest.SELECT_COLOR));
            interact.setStranger(ContactCmdUtils.toLiteStranger(new JSONObject(json.optString(CommandFields.User.USER))));
            newMessage.setInteract(interact);
        }
        return newMessage;
    }

    public static InteractMessage build(SystemPush systemPush) {
        InteractMessage newMessage = null;
        try {
            newMessage = build(new JSONObject(systemPush.getContent()));
            newMessage.setPushType(systemPush.getType());
            newMessage.setTime(systemPush.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessage;
    }

}
