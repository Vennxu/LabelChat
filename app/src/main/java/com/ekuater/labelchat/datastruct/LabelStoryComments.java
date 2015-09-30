package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Label on 2015/1/13.
 *
 * @author Xu wenxiang
 */
public class LabelStoryComments implements Parcelable{

    private String mStroyCommentId;
    private String mLabelStoryId;
    private String mStoryComment;
    private long mCreateDate;
    private Stranger mStranger;
    private String mCommentIsPraise;
    private String mCommentPraise;
    private String mParentCommentId;
    private String mReplyNickName;
    private String mReplyUserId;
    private List<String> mArrayUserId;


    public LabelStoryComments(){

    }
    public LabelStoryComments(Stranger stranger,long createDate,String content,String commentPraise,String commentIsPraise ){
        this.mStranger=stranger;
        this.mCreateDate=createDate;
        this.mStoryComment=content;
        this.mCommentPraise=commentPraise;
        this.mCommentIsPraise=commentIsPraise;
    }
    public String getmLabelStoryId() {
        return mLabelStoryId;
    }

    public void setmLabelStoryId(String mLabelStoryId) {
        this.mLabelStoryId = mLabelStoryId;
    }

    public String getmStroyCommentId() {
        return mStroyCommentId;
    }

    public void setmStroyCommentId(String mStroyCommentId) {
        this.mStroyCommentId = mStroyCommentId;
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

    public Stranger getmStranger() {
        return mStranger;
    }

    public void setmStranger(Stranger mStranger) {
        this.mStranger = mStranger;
    }
    public String getmCommentPraise() {
        return mCommentPraise;
    }

    public void setmCommentPraise(String mCommentPraise) {
        this.mCommentPraise = mCommentPraise;
    }
    public String getmCommentIsPraise() {
        return mCommentIsPraise;
    }

    public void setmCommentIsPraise(String mCommentIsPraise) {
        this.mCommentIsPraise = mCommentIsPraise;
    }
    public String getmParentCommentId() {
        return mParentCommentId;
    }

    public void setmParentCommentId(String mParentCommentId) {
        this.mParentCommentId = mParentCommentId;
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

    public List<String> getmArrayUserId() {
        return mArrayUserId;
    }

    public void setmArrayUserId(List<String> mArrayUserId) {
        this.mArrayUserId = mArrayUserId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLabelStoryId);
        dest.writeString(mStroyCommentId);
        dest.writeString(mStoryComment);
        dest.writeLong(mCreateDate);
        dest.writeParcelable(mStranger, flags);
        dest.writeString(mCommentPraise);
        dest.writeString(mCommentIsPraise);
        dest.writeString(mParentCommentId);
        dest.writeString(mReplyNickName);
        dest.writeString(mReplyUserId);
        dest.writeStringList(mArrayUserId);
    }
    public static final Creator<LabelStoryComments> CREATOR
            =new Creator<LabelStoryComments>(){

        @Override
        public LabelStoryComments createFromParcel(Parcel source) {
            LabelStoryComments labelStoryComments=new LabelStoryComments();
            labelStoryComments.mLabelStoryId=source.readString();
            labelStoryComments.mStroyCommentId=source.readString();
            labelStoryComments.mStoryComment=source.readString();
            labelStoryComments.mCreateDate=source.readLong();
            labelStoryComments.mStranger=source.readParcelable(Stranger.class.getClassLoader());
            labelStoryComments.mCommentPraise=source.readString();
            labelStoryComments.mCommentIsPraise=source.readString();
            labelStoryComments.mParentCommentId = source.readString();
            labelStoryComments.mReplyNickName = source.readString();
            labelStoryComments.mReplyUserId = source.readString();
            labelStoryComments.mArrayUserId = source.createStringArrayList();
            return labelStoryComments;
        }

        @Override
        public LabelStoryComments[] newArray(int size) {
            return new LabelStoryComments[size];
        }
    };
}
