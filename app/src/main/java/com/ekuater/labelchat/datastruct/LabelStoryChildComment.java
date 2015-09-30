package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Label on 2015/2/5.
 *
 * @author Xu wenxiang
 */
public class LabelStoryChildComment implements Parcelable{
    private String mParentCommentId;
    private String mReplyNickName;
    private String mReplyUserId;
    private long mCreateDate;
    private String mStoryComment;
    private Stranger mStranger;

    @Override
    public String toString() {
        return "LabelStoryChildComment{" +
                "mParentCommentId='" + mParentCommentId + '\'' +
                ", mReplyNickName='" + mReplyNickName + '\'' +
                ", mCreateDate=" + mCreateDate +
                ", mStoryComment='" + mStoryComment + '\'' +
                ", mStranger=" + mStranger +
                '}';
    }

    public String getmParentCommentId() {
        return mParentCommentId;
    }

    public void setmParentCommentId(String mParentCommentId) {
        this.mParentCommentId = mParentCommentId;
    }

    public Stranger getmStranger() {
        return mStranger;
    }

    public void setmStranger(Stranger mStranger) {
        this.mStranger = mStranger;
    }

    public String getmStoryComment() {
        return mStoryComment;
    }

    public void setmStoryComment(String mStoryComment) {
        this.mStoryComment = mStoryComment;
    }

    public long getmCreateDate() {
        return mCreateDate;
    }

    public void setmCreateDate(long mCreateDate) {
        this.mCreateDate = mCreateDate;
    }

    public String getmReplyNickName() {
        return mReplyNickName;
    }

    public void setmReplyNickName(String mReplyNickName) {
        this.mReplyNickName = mReplyNickName;
    }
    public String getmReplyUserId() {
        return mReplyUserId;
    }

    public void setmReplyUserId(String mReplyUserId) {
        this.mReplyUserId = mReplyUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mParentCommentId);
        dest.writeString(mReplyNickName);
        dest.writeString(mReplyUserId);
        dest.writeLong(mCreateDate);
        dest.writeString(mStoryComment);
        dest.writeParcelable(mStranger,flags);
    }
    public static final Creator<LabelStoryChildComment> CREATOR=new Creator<LabelStoryChildComment>() {
        @Override
        public LabelStoryChildComment createFromParcel(Parcel source) {
            LabelStoryChildComment labelStoryChildComment=new LabelStoryChildComment();
            labelStoryChildComment.mParentCommentId=source.readString();
            labelStoryChildComment.mReplyNickName=source.readString();
            labelStoryChildComment.mReplyUserId=source.readString();
            labelStoryChildComment.mCreateDate=source.readLong();
            labelStoryChildComment.mStoryComment=source.readString();
            labelStoryChildComment.mStranger=source.readParcelable(Stranger.class.getClassLoader());
            return labelStoryChildComment;
        }

        @Override
        public LabelStoryChildComment[] newArray(int size) {
            return new LabelStoryChildComment[0];
        }
    };
}
