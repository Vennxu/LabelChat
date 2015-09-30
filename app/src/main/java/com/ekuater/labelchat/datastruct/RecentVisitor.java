package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/3/26.
 */
public class RecentVisitor implements Parcelable {

    private String recentUserId;
    private String recentUserLabelCode;
    private String recentUserName;
    private String recentUserAvatar;
    private String recentUserAvatarThumb;
    private long recentDate;

    public String getRecentUserId() {
        return recentUserId;
    }

    public void setRecentUserId(String recentUserId) {
        this.recentUserId = recentUserId;
    }

    public String getRecentUserLabelCode() {
        return recentUserLabelCode;
    }

    public void setRecentUserLabelCode(String recentUserLabelCode) {
        this.recentUserLabelCode = recentUserLabelCode;
    }

    public String getRecentUserName() {
        return recentUserName;
    }

    public void setRecentUserName(String recentUserName) {
        this.recentUserName = recentUserName;
    }

    public String getRecentUserAvatar() {
        return recentUserAvatar;
    }

    public void setRecentUserAvatar(String recentUserAvatar) {
        this.recentUserAvatar = recentUserAvatar;
    }

    public String getRecentUserAvatarThumb() {
        return recentUserAvatarThumb;
    }

    public void setRecentUserAvatarThumb(String recentUserAvatarThumb) {
        this.recentUserAvatarThumb = recentUserAvatarThumb;
    }

    public long getRecentDate() {
        return recentDate;
    }

    public void setRecentDate(long recentDate) {
        this.recentDate = recentDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getRecentUserId());
        dest.writeString(getRecentUserLabelCode());
        dest.writeString(getRecentUserName());
        dest.writeString(getRecentUserAvatar());
        dest.writeString(getRecentUserAvatarThumb());
        dest.writeLong(getRecentDate());
    }

    public static final Creator<RecentVisitor> CREATOR = new Creator<RecentVisitor>() {
        @Override
        public RecentVisitor createFromParcel(Parcel source) {
            RecentVisitor recentVisitor = new RecentVisitor();
            recentVisitor.recentUserId = source.readString();
            recentVisitor.recentUserLabelCode = source.readString();
            recentVisitor.recentUserName = source.readString();
            recentVisitor.recentUserAvatar = source.readString();
            recentVisitor.recentUserAvatarThumb = source.readString();
            recentVisitor.recentDate = source.readLong();
            return recentVisitor;
        }

        @Override
        public RecentVisitor[] newArray(int size) {
            return new RecentVisitor[size];
        }
    };
}
