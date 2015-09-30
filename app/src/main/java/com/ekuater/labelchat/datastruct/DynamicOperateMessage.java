package com.ekuater.labelchat.datastruct;


import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/4/15.
 *
 * @author FanChong
 */
public class DynamicOperateMessage {
    public static final String TAG = DynamicOperateMessage.class.getSimpleName();

    public static final String TYPE_OPERATE_PRAISE = "praise";
    public static final String TYPE_OPERATE_COMMENT = "comment";
    public static final String TYPE_OPERATE_BANKNOTE = "banknote";
    public static final String TYPE_MESSAGE_BOX = "1";
    public static final String TYPE_MESSAGE_RED_DOT = "2";


    private int pushType;
    private long time;
    private long id;
    private int state;
    private String operateType;
    private String messagePlace;
    private Stranger stranger;

    private String dynamicId;
    private String creatorNickname;
    private long dynamicCreateDate;
    private String dynamicContent;
    private String dynamicImg;
    private String dynamicImgThumb;
    private String dynamicType;

    private String dynamicCommentId;
    private String dynamicCommentContent;
    private String replyDynamicCommentContent;
    private long commentDate;
    private String userId;
    private String replyNickname;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public String getDynamicContent() {
        return dynamicContent;
    }

    public void setDynamicContent(String dynamicContent) {
        this.dynamicContent = dynamicContent;
    }

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

    public String getDynamicId() {
        return dynamicId;
    }

    public void setDynamicId(String dynamicId) {
        this.dynamicId = dynamicId;
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public long getDynamicCreateDate() {
        return dynamicCreateDate;
    }

    public void setDynamicCreateDate(long dynamicCreateDate) {
        this.dynamicCreateDate = dynamicCreateDate;
    }

    public String getDynamicImg() {
        return dynamicImg;
    }

    public void setDynamicImg(String dynamicImg) {
        this.dynamicImg = dynamicImg;
    }

    public String getDynamicImgThumb() {
        return dynamicImgThumb;
    }

    public void setDynamicImgThumb(String getDynamicImgThumb) {
        this.dynamicImgThumb = getDynamicImgThumb;
    }

    public String getDynamicType() {
        return dynamicType;
    }

    public void setDynamicType(String dynamicType) {
        this.dynamicType = dynamicType;
    }

    public String getDynamicCommentId() {
        return dynamicCommentId;
    }

    public void setDynamicCommentId(String dynamicCommentId) {
        this.dynamicCommentId = dynamicCommentId;
    }

    public String getDynamicCommentContent() {
        return dynamicCommentContent;
    }

    public void setDynamicCommentContent(String dynamicCommentContent) {
        this.dynamicCommentContent = dynamicCommentContent;
    }

    public long getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(long commentDate) {
        this.commentDate = commentDate;
    }

    public String getReplyDynamicCommentContent() {
        return replyDynamicCommentContent;
    }

    public void setReplyDynamicCommentContent(String replyDynamicCommentContent) {
        this.replyDynamicCommentContent = replyDynamicCommentContent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReplyNickname() {
        return replyNickname;
    }

    public void setReplyNickname(String replyNickname) {
        this.replyNickname = replyNickname;
    }


    public static DynamicOperateMessage build(JSONObject json) throws JSONException {
        DynamicOperateMessage newMessage = null;
        if (json != null) {
            newMessage = new DynamicOperateMessage();
            newMessage.setOperateType(json.optString(SystemPushFields.FIELD_DYNAMIC_OPERATE));
            newMessage.setMessagePlace(json.optString(SystemPushFields.FIELD_DYNAMIC_NOTIFY_MESSAGE_PLACE));
            switch (newMessage.getOperateType()) {
                case TYPE_OPERATE_PRAISE:
                    newMessage.setStranger(ContactCmdUtils.toStranger(json.optJSONObject(SystemPushFields.FIELD_DYNAMIC_USER_INFO_VO)));
                    JSONObject praise = new JSONObject(json.optString(SystemPushFields.FIELD_DYNAMIC_INFO_VO));
                    newMessage.setDynamicId(praise.optString(SystemPushFields.FIELD_DYNAMIC_ID));
                    newMessage.setCreatorNickname(praise.optString(SystemPushFields.FIELD_NICKNAME));
                    newMessage.setDynamicCreateDate(praise.optLong(SystemPushFields.FIELD_DYNAMIC_CREATE_DATE));
                    newMessage.setDynamicContent(praise.optString(SystemPushFields.FIELD_DYNAMIC_CONTENT));
                    newMessage.setDynamicImg(praise.optString(SystemPushFields.FIELD_DYNAMIC_IMG));
                    newMessage.setDynamicImgThumb(praise.optString(SystemPushFields.FIELD_DYNAMIC_IMG_THUMB));
                    newMessage.setDynamicType(praise.optString(SystemPushFields.FIELD_DYNAMIC_TYPE));
                    break;
                case TYPE_OPERATE_COMMENT:
                    JSONObject comment = new JSONObject(json.optString(SystemPushFields.FIELD_DYNAMIC_COMMENT_INFO_VO));
                    newMessage.setDynamicCommentId(comment.optString(SystemPushFields.FIELD_DYNAMIC_COMMENT_ID));
                    newMessage.setDynamicId(comment.optString(SystemPushFields.FIELD_DYNAMIC_ID));
                    newMessage.setCreatorNickname(comment.optString(SystemPushFields.FIELD_NICKNAME));
                    newMessage.setDynamicCommentContent(comment.optString(SystemPushFields.FIELD_DYNAMIC_COMMENT_CONTENT));
                    newMessage.setReplyDynamicCommentContent(comment.optString(SystemPushFields.FIELD_DYNAMIC_REPLY_COMMENT_CONTENT));
                    newMessage.setReplyNickname(comment.optString(SystemPushFields.FIELD_DYNAMIC_REPLY_NICKNAME));
                    newMessage.setDynamicCreateDate(comment.optLong(SystemPushFields.FIELD_DYNAMIC_CREATE_DATE));
                    newMessage.setUserId(comment.optString(SystemPushFields.FIELD_DYNAMIC_USER_ID));
                    JSONObject dynamicContent = new JSONObject(comment.optString(SystemPushFields.FIELD_DYNAMIC_INFO_VO));
                    newMessage.setDynamicContent(dynamicContent.optString(SystemPushFields.FIELD_DYNAMIC_CONTENT));
                    newMessage.setDynamicImgThumb(dynamicContent.optString(SystemPushFields.FIELD_DYNAMIC_IMG_THUMB));
                    newMessage.setDynamicType(dynamicContent.optString(SystemPushFields.FIELD_DYNAMIC_TYPE));
                    newMessage.setStranger(ContactCmdUtils.toStranger(comment.optJSONObject(SystemPushFields.FIELD_DYNAMIC_USER_INFO_VO)));
                    break;

            }
        }
        return newMessage;
    }

    public static DynamicOperateMessage build(SystemPush systemPush) {
        DynamicOperateMessage newMessage = null;
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
