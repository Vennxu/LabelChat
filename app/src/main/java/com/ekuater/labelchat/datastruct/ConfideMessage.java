package com.ekuater.labelchat.datastruct;


import com.ekuater.labelchat.command.confide.ConfideCmdUtils;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.delegate.ConfideManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/4/17.
 *
 * @author FanChong
 */
public class ConfideMessage {

    public static final String TYPE_OPERATE_PRAISE = "praise";
    public static final String TYPE_OPERATE_COMMENT = "comment";
    public static final String TYPE_MESSAGE_BOX = "1";
    public static final String TYPE_MESSAGE_RED_DOT = "2";

    private String operateType;
    private String messagePlace;
    private long time;
    private Stranger stranger;
    private Confide confide;
    private String commentId;
    private String confideId;
    private String commentContent;
    private String virtualAvatar;
    private int floor;
    private String parentCommentId;
    private String replyCommentContent;
    private int replyFloor;

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getMessagePlace() {
        return messagePlace;
    }

    public void setMessagePlace(String messagePlace) {
        this.messagePlace = messagePlace;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Confide getConfide() {
        return confide;
    }

    public void setConfide(Confide confide) {
        this.confide = confide;
    }

    public String getConfideId() {
        return confideId;
    }

    public void setConfideId(String confideId) {
        this.confideId = confideId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getVirtualAvatar() {
        return virtualAvatar;
    }

    public void setVirtualAvatar(String virtualAvatar) {
        this.virtualAvatar = virtualAvatar;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getReplyCommentContent() {
        return replyCommentContent;
    }

    public void setReplyCommentContent(String replyCommentContent) {
        this.replyCommentContent = replyCommentContent;
    }

    public int getReplyFloor() {
        return replyFloor;
    }

    public void setReplyFloor(int replyFloor) {
        this.replyFloor = replyFloor;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public static ConfideMessage build(JSONObject json) throws JSONException {
        ConfideMessage newMessage = null;
        if (json != null) {
            newMessage = new ConfideMessage();
            newMessage.setOperateType(json.optString(SystemPushFields.FIELD_CONFIDE_OPERATE));
            newMessage.setMessagePlace(json.optString(SystemPushFields.FIELD_CONFIDE_MESSAGE_PLACE));
            switch (newMessage.getOperateType()) {
                case TYPE_OPERATE_PRAISE:
                    newMessage.setStranger(ContactCmdUtils.toStranger(json.optJSONObject(SystemPushFields.FIELD_CONFIDE_USER_VO)));
                    newMessage.setConfide(ConfideCmdUtils.toConfide(json.optJSONObject(SystemPushFields.FILED_CONFIDE_CONFIDE_VO)));
                    break;
                case TYPE_OPERATE_COMMENT:
                    JSONObject comment = new JSONObject(json.optString(SystemPushFields.FILED_CONFIDE_COMMENT_VO));

                    newMessage.setCommentId(comment.optString(SystemPushFields.FILED_CONFIDE_COMMENT_ID));
                    newMessage.setConfideId(comment.optString(SystemPushFields.FILED_CONFIDE_ID));
                    newMessage.setCommentContent(comment.optString(SystemPushFields.FILED_CONFIDE_COMMENT));
                    newMessage.setVirtualAvatar(comment.optString(SystemPushFields.FILED_CONFIDE_VIRTUAL_AVATAR));
                    newMessage.setFloor(comment.optInt(SystemPushFields.FILED_CONFIDE_FLOOR));
                    newMessage.setParentCommentId(comment.optString(SystemPushFields.FILED_CONFIDE_PARENT_COMMENT_ID));
                    newMessage.setReplyCommentContent(comment.optString(SystemPushFields.FILED_CONFIDE_REPLY_COMMENT));
                    newMessage.setReplyFloor(comment.optInt(SystemPushFields.FILED_CONFIDE_REPLY_FLOOR));
                    newMessage.setStranger(ContactCmdUtils.toStranger(comment.optJSONObject(SystemPushFields.FIELD_CONFIDE_USER_VO)));
                    newMessage.setConfide(ConfideCmdUtils.toConfide(comment.optJSONObject(SystemPushFields.FILED_CONFIDE_CONFIDE_VO)));
                    break;
                default:
                    newMessage.setConfide(ConfideCmdUtils.toConfide(json.optJSONObject(SystemPushFields.FILED_CONFIDE_CONFIDE_VO)));
                    break;
            }
        }
        return newMessage;
    }

    public static ConfideMessage build(SystemPush systemPush) {
        ConfideMessage newMessage = null;
        try {
            newMessage = ConfideMessage.build(new JSONObject(systemPush.getContent()));
            newMessage.setTime(systemPush.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessage;
    }
}
