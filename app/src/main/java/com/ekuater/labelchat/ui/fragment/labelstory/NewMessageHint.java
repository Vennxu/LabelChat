package com.ekuater.labelchat.ui.fragment.labelstory;

import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;

/**
 * Created by Administrator on 2015/4/29.
 *
 * @author FanChong
 */
public class NewMessageHint {
    private long id;
    private int type;
    private long time;
    private int state;
    private String avatarImage;
    private DynamicOperateMessage dynamicMessage;
    private ConfideMessage confideMessages;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(String avatarImage) {
        this.avatarImage = avatarImage;
    }

    public DynamicOperateMessage getDynamicMessage() {
        return dynamicMessage;
    }

    public void setDynamicMessage(DynamicOperateMessage dynamicMessage) {
        this.dynamicMessage = dynamicMessage;
    }

    public ConfideMessage getConfideMessages() {
        return confideMessages;
    }

    public void setConfideMessages(ConfideMessage confideMessages) {
        this.confideMessages = confideMessages;
    }

    public NewMessageHint() {
    }
}
