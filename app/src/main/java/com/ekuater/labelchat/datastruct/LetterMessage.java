package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.util.L;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Label on 2015/3/13.
 */
public class LetterMessage {

    public static final String TAG = LetterMessage.class.getSimpleName();

    private long messageId;
    private String message;
    private long time;
    private int tag;
    private int state;

    private Stranger stranger;

    public long getMessageId() {
        return messageId;
    }
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime(){
        return time;
    }
    public void setTime(long time){
        this.time = time;
    }

    public int getTag(){
        return tag;
    }
    public void setTag(int tag){
        this.tag = tag;
    }

    public void setState(int state){
        this.state = state;
    }
    public int getState(){
        return state;
    }


    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }


    public static LetterMessage build(JSONObject json) throws JSONException {
        LetterMessage newMessage = null;

        if (json != null) {
            String message = json.optString(SystemPushFields.FIELD_LETTER_MESSAGE);
            String userVO = json.optString(SystemPushFields.FIELD_STORY_USER_VO);
            int tag = json.optInt(SystemPushFields.FIELD_LETTER_TAG);
            Stranger stranger = null;
            if (userVO != null) {
                L.w(TAG, userVO);
                stranger = ContactCmdUtils.toStranger(new JSONObject(userVO));
            }
            newMessage = new LetterMessage();
            newMessage.setMessage(message);
            newMessage.setStranger(stranger);
            newMessage.setTag(tag);
        }
        return newMessage;
    }

    public static LetterMessage build(SystemPush push) {
        LetterMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_PRIVATE_LETTER) {
            try {
                L.w(TAG, push.getContent());
                JSONObject json=new JSONObject(push.getContent());
                if (json!=null) {
                    newMessage = build(json);
                }
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}
