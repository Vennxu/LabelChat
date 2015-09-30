package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Administrator on 2015/4/8.
 *
 * @author XuWenxiang
 */
public class ConfideComment implements Parcelable {

    private String confideCommentId;
    private String confideId;
    private String confideUserId;
    private String comment;
    private String floor;
    private String replyComment;
    private String replyFloor;
    private String virtualAvatar;
    private String position;
    private long createDate;
    private LiteStranger stranger;
    private List<String> userIds;

    public ConfideComment() {
    }

    public ConfideComment(Parcel in) {
        this.confideCommentId = in.readString();
        this.confideId = in.readString();
        this.confideUserId = in.readString();
        this.comment = in.readString();
        this.floor = in.readString();
        this.replyComment = in.readString();
        this.replyFloor = in.readString();
        this.virtualAvatar = in.readString();
        this.position = in.readString();
        this.createDate = in.readLong();
        this.stranger = ParcelUtils.createParcelType(in, LiteStranger.CREATOR);
        this.userIds = in.createStringArrayList();
    }

    public ConfideComment(ConfideComment confideComment) {
        this.confideCommentId = confideComment.getConfideCommentId();
        this.confideId = confideComment.getConfideId();
        this.comment = confideComment.getComment();
        this.floor = confideComment.getFloor();
        this.replyComment = confideComment.getReplayComment();
        this.replyFloor = confideComment.getReplyFloor();
        this.virtualAvatar = confideComment.getVirtualAvatar();
        this.position = confideComment.getPosition();
        this.createDate = confideComment.getCreateDate();
        this.userIds = confideComment.getUserIds();

    }

    public String getConfideCommentId() {
        return confideCommentId;
    }

    public void setConfideCommentId(String confideCommentId) {
        this.confideCommentId = confideCommentId;
    }

    public String getConfideUserId() {
        return confideUserId;
    }

    public void setConfideUserId(String confideUserId) {
        this.confideUserId = confideUserId;
    }

    public String getConfideId() {
        return confideId;
    }

    public void setConfideId(String confideId) {
        this.confideId = confideId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getReplayComment() {
        return replyComment;
    }

    public void setReplyComment(String replyComment) {
        this.replyComment = replyComment;
    }

    public String getReplyFloor() {
        return replyFloor;
    }

    public void setReplyFloor(String replyFloor) {
        this.replyFloor = replyFloor;
    }

    public String getVirtualAvatar() {
        return virtualAvatar;
    }

    public void setVirtualAvatar(String virtualAvatar) {
        this.virtualAvatar = virtualAvatar;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public void setStranger(LiteStranger stranger) {
        this.stranger = stranger;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.confideCommentId);
        dest.writeString(this.confideId);
        dest.writeString(this.confideUserId);
        dest.writeString(this.comment);
        dest.writeString(this.floor);
        dest.writeString(this.replyComment);
        dest.writeString(this.replyFloor);
        dest.writeString(this.virtualAvatar);
        dest.writeString(this.position);
        dest.writeLong(this.createDate);
        ParcelUtils.writeParcelType(dest, this.stranger, flags);
        dest.writeStringList(userIds);
    }

    public static final Creator<ConfideComment> CREATOR = new Creator<ConfideComment>() {
        @Override
        public ConfideComment createFromParcel(Parcel source) {
            return new ConfideComment(source);
        }

        @Override
        public ConfideComment[] newArray(int size) {
            return new ConfideComment[size];
        }
    };
}
