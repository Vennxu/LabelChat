package com.ekuater.labelchat.datastruct;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.util.ColorUtils;

/**
 * Created by Administrator on 2015/4/8.
 *
 * @author XuWenxiang
 */
public class Confide implements Parcelable {

    private static final int DEFAULT_TAG_SELECTED_COLOR = Color.LTGRAY;
    private static final int DEFAULT_TAG_SELECTED_BG = R.drawable.confide_bg_1;

    private String confideId;
    private String confideUserId;
    private String confideAvatar;
    private String confideSex;
    private String confideRole;
    private String confidePosition;
    private String confideBgColor;
    private String confideBgImg;
    private String confideContent;
    private int confidePraiseNum;
    private int confideCommentNum;
    private String confideIsPraise;
    private long confideCreateDate;
    private ConfideComment[] confideComments;
    private Stranger[] confideStranger;

    public Confide() {
    }

    private Confide(Parcel in) {
        this.confideId = in.readString();
        this.confideUserId = in.readString();
        this.confideSex = in.readString();
        this.confideRole = in.readString();
        this.confidePosition = in.readString();
        this.confideBgColor = in.readString();
        this.confideBgImg = in.readString();
        this.confideContent = in.readString();
        this.confidePraiseNum = in.readInt();
        this.confideCommentNum = in.readInt();
        this.confideIsPraise = in.readString();
        this.confideCreateDate = in.readLong();
        this.confideAvatar = in.readString();
        this.confideComments = in.createTypedArray(ConfideComment.CREATOR);
        this.confideStranger = in.createTypedArray(Stranger.CREATOR);
    }

    public String getConfideId() {
        return confideId;
    }

    public void setConfideId(String confideId) {
        this.confideId = confideId;
    }

    public String getConfideUserId() {
        return confideUserId;
    }

    public void setConfideUserId(String confideUserId) {
        this.confideUserId = confideUserId;
    }

    public String getConfideSex() {
        return confideSex;
    }

    public void setConfideSex(String confideSex) {
        this.confideSex = confideSex;
    }

    public String getConfideRole() {
        return confideRole;
    }

    public void setConfideRole(String confideRole) {
        this.confideRole = confideRole;
    }

    public String getConfidePosition() {
        return confidePosition;
    }

    public void setConfidePosition(String confidePosition) {
        this.confidePosition = confidePosition;
    }

    public String getConfideBgColor() {
        return confideBgColor;
    }

    public void setConfideBgColor(String confideBgColor) {
        this.confideBgColor = confideBgColor;
    }

    public String getConfideBgImg() {
        return confideBgImg;
    }

    public void setConfideBgImg(String confideBgImg) {
        this.confideBgImg = confideBgImg;
    }

    public String getConfideContent() {
        return confideContent;
    }

    public void setConfideContent(String confideContent) {
        this.confideContent = confideContent;
    }

    public int getConfidePraiseNum() {
        return confidePraiseNum;
    }

    public void setConfidePraiseNum(int confidePraiseNum) {
        this.confidePraiseNum = confidePraiseNum;
    }

    public int getConfideCommentNum() {
        return confideCommentNum;
    }

    public void setConfideCommentNum(int confideCommentNum) {
        this.confideCommentNum = confideCommentNum;
    }

    public String getConfideIsPraise() {
        return confideIsPraise;
    }

    public void setConfideIsPraise(String confideIsPraise) {
        this.confideIsPraise = confideIsPraise;
    }

    public long getConfideCreateDate() {
        return confideCreateDate;
    }

    public void setConfideCreateDate(long confideCreateDate) {
        this.confideCreateDate = confideCreateDate;
    }

    public String getConfideAvatar() {
        return confideAvatar;
    }

    public void setConfideAvatar(String confideAvatar) {
        this.confideAvatar = confideAvatar;
    }

    public ConfideComment[] getConfideComments() {
        return confideComments;
    }

    public void setConfideComments(ConfideComment[] confideComments) {
        this.confideComments = confideComments;
    }

    public Stranger[] getConfideStranger() {
        return confideStranger;
    }

    public void setConfideStranger(Stranger[] confideStranger) {
        this.confideStranger = confideStranger;
    }

    public int parseBgColor() {
        return parseColor(this.confideBgColor, DEFAULT_TAG_SELECTED_COLOR);
    }

    private int parseColor(String colorString, int defaultColor) {
        if (!TextUtils.isEmpty(colorString)) {
            try {
                return ColorUtils.parseColor(colorString);
            } catch (Exception e) {
                return defaultColor;
            }
        } else {
            return defaultColor;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(confideId);
        dest.writeString(confideUserId);
        dest.writeString(confideSex);
        dest.writeString(confideRole);
        dest.writeString(confidePosition);
        dest.writeString(confideBgColor);
        dest.writeString(confideBgImg);
        dest.writeString(confideContent);
        dest.writeInt(confidePraiseNum);
        dest.writeInt(confideCommentNum);
        dest.writeString(confideIsPraise);
        dest.writeLong(confideCreateDate);
        dest.writeString(confideAvatar);
        dest.writeTypedArray(confideComments, flags);
        dest.writeTypedArray(confideStranger, flags);
    }

    public static final Creator<Confide> CREATOR = new Creator<Confide>() {
        @Override
        public Confide createFromParcel(Parcel source) {
            return new Confide(source);
        }

        @Override
        public Confide[] newArray(int size) {
            return new Confide[size];
        }
    };
}
