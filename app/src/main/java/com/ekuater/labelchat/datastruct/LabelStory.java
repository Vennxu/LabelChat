package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.command.CommandFields;

import java.util.Arrays;

/**
 * @author Xu wenxiang
 */
public class LabelStory implements Parcelable {

    public static final String TYPE_TXT_IMG = CommandFields.StoryLabel.TYPE_TXT_IMG;
    public static final String TYPE_AUDIO = CommandFields.StoryLabel.TYPE_AUDIO;
    public static final String TYPE_VIDEO = CommandFields.StoryLabel.TYPE_VIDEO;
    public static final String TYPE_BANKNOTE = CommandFields.StoryLabel.TYPE_BANKNOTE;
    public static final String TYPE_ONLINEAUDIO = CommandFields.StoryLabel.TYPE_ONLINEAUDIO;

    private String labelStoryId;
    private String categoryId;
    private String authorUserId;
    private long createDate;
    private long modifyDate;
    private String content;
    private String praise;
    private String commentNum;
    private int letterNum;
    private String isMaster;
    private String floor;
    private String isPraise;
    private Stranger stranger;
    private String isFollowing;
    private String friendStoryCount;
    private String[] images;
    private String[] thumbImages;
    private LabelStoryComments[] labelStoryComments;
    private LabelStoryCategory category;
    private UserPraise[] userPraise;
    private String browseNum;
    private PickPhotoUser[] pickPhotoUser;
    private int storyTotal;
    private String type;
    private String media;
    private long duration;

    public LabelStory() {
    }

    private LabelStory(Parcel in) {
        this.labelStoryId = in.readString();
        this.categoryId = in.readString();
        this.authorUserId = in.readString();
        this.createDate = in.readLong();
        this.modifyDate = in.readLong();
        this.content = in.readString();
        this.praise = in.readString();
        this.commentNum = in.readString();
        this.letterNum = in.readInt();
        this.isPraise = in.readString();
        this.stranger = ParcelUtils.createParcelType(in, Stranger.CREATOR);
        this.images = in.createStringArray();
        this.thumbImages = in.createStringArray();
        this.labelStoryComments = in.createTypedArray(LabelStoryComments.CREATOR);
        this.category = ParcelUtils.createParcelType(in, LabelStoryCategory.CREATOR);
        this.friendStoryCount = in.readString();
        this.userPraise = in.createTypedArray(UserPraise.CREATOR);
        this.browseNum = in.readString();
        this.pickPhotoUser = in.createTypedArray(PickPhotoUser.CREATOR);
        this.isFollowing = in.readString();
        this.storyTotal = in.readInt();
        this.type = in.readString();
        this.media = in.readString();
        this.duration = in.readLong();
    }

    @Override
    public String toString() {
        return "LabelStory{" +
                "labelStoryId='" + labelStoryId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", authorUserId='" + authorUserId + '\'' +
                ", createDate=" + createDate +
                ", modifyDate=" + modifyDate +
                ", content='" + content + '\'' +
                ", praise='" + praise + '\'' +
                ", commentNum='" + commentNum + '\'' +
                ", isMaster='" + isMaster + '\'' +
                ", floor='" + floor + '\'' +
                ", isPraise='" + isPraise + '\'' +
                ", stranger=" + stranger +
                ", images=" + Arrays.toString(images) +
                ", thumbImages=" + Arrays.toString(thumbImages) +
                ", labelStoryComments=" + Arrays.toString(labelStoryComments) +
                ", mUserLabel=" + category +
                '}';
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getLabelStoryId() {
        return labelStoryId;
    }

    public void setLabelStoryId(String labelStoryId) {
        this.labelStoryId = labelStoryId;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(String authorUserId) {
        this.authorUserId = authorUserId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(String isMaster) {
        this.isMaster = isMaster;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getPraise() {
        return praise;
    }

    public void setPraise(String praise) {
        this.praise = praise;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getIsPraise() {
        return isPraise;
    }

    public void setIsPraise(String isPraise) {
        this.isPraise = isPraise;
    }

    public String[] getImages() {
        return images;
    }

    public void setThumbImages(String[] thumbImages) {
        this.thumbImages = thumbImages;
    }

    public String[] getThumbImages() {
        return thumbImages;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public LabelStoryComments[] getLabelStoryComments() {
        return labelStoryComments;
    }

    public void setLabelStoryComments(LabelStoryComments[] labelStoryComments) {
        this.labelStoryComments = labelStoryComments;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public int getLetterNum(){
        return letterNum;
    }

    public void setLetterNum(int letterNum){
        this.letterNum = letterNum;
    }

    public LabelStoryCategory getCategory() {
        return category;
    }

    public void setCategory(LabelStoryCategory category) {
        this.category = category;
    }

    public String getFriendStoryCount() {
        return friendStoryCount;
    }

    public void setFriendStoryCount(String friendStoryCount) {
        this.friendStoryCount = friendStoryCount;
    }

    public UserPraise[] getUserPraise() {
        return userPraise;
    }

    public void setUserPraise(UserPraise[] userPraise) {
        this.userPraise = userPraise;
    }

    public PickPhotoUser[] getPickPhotoUser() {
        return pickPhotoUser;
    }

    public void setPickPhotoUser(PickPhotoUser[] pickPhotoUser) {
        this.pickPhotoUser = pickPhotoUser;
    }

    public String getBrowseNum() {
        return browseNum;
    }

    public void setBrowseNum(String browseNum) {
        this.browseNum = browseNum;
    }

    public String getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(String isFollowing) {
        this.isFollowing = isFollowing;
    }

    public int getStoryTotal() {
        return storyTotal;
    }

    public void setStoryTotal(int storyTotal) {
        this.storyTotal = storyTotal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(labelStoryId);
        dest.writeString(categoryId);
        dest.writeString(authorUserId);
        dest.writeLong(createDate);
        dest.writeLong(modifyDate);
        dest.writeString(content);
        dest.writeString(praise);
        dest.writeString(commentNum);
        dest.writeInt(letterNum);
        dest.writeString(isPraise);
        ParcelUtils.writeParcelType(dest, stranger, flags);
        dest.writeStringArray(images);
        dest.writeStringArray(thumbImages);
        dest.writeTypedArray(labelStoryComments, flags);
        ParcelUtils.writeParcelType(dest, category, flags);
        dest.writeString(friendStoryCount);
        dest.writeTypedArray(userPraise, flags);
        dest.writeString(browseNum);
        dest.writeTypedArray(pickPhotoUser, flags);
        dest.writeString(isFollowing);
        dest.writeInt(storyTotal);
        dest.writeString(type);
        dest.writeString(media);
        dest.writeLong(duration);
    }

    public static final Parcelable.Creator<LabelStory> CREATOR
            = new Parcelable.Creator<LabelStory>() {

        @Override
        public LabelStory createFromParcel(Parcel source) {
            return new LabelStory(source);
        }

        @Override
        public LabelStory[] newArray(int size) {
            return new LabelStory[size];
        }
    };
}