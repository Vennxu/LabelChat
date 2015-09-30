package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

/**
 * @author LinYong
 */
public class Stranger implements Parcelable {

    private static final String TAG = Stranger.class.getSimpleName();

    private String labelCode;
    private String userId;
    private String nickname;
    private String mobile;
    private boolean bubbleUpUser;
    private int gender;
    private long birthday;
    private int age;
    private int constellation;
    private String province;
    private String city;
    private String school;
    private String signature;
    private int height;
    private String job;
    private String avatar;
    private String avatarThumb;
    private UserLabel[] labels;
    private LocationInfo location;
    private UserTheme theme;
    private AlbumPhoto[] albumPhotos;
    private LabelStory[] labelStories;
    private InterestType[] interestTypes;
    private UserTag[] userTags;
    private int visitorCount;
    private int myPhotoTotal;

    public Stranger() {
    }

    public Stranger(String userId, String userName, String avatarThumb, String avatar, int sex) {
        this.userId = userId;
        this.nickname = userName;
        this.avatarThumb = avatarThumb;
        this.avatar = avatar;
        this.gender = sex;
    }

    private Stranger(Parcel in) {
        this.userId = in.readString();
        this.labelCode = in.readString();
        this.nickname = in.readString();
        this.mobile = in.readString();
        this.gender = in.readInt();
        this.birthday = in.readLong();
        this.age = in.readInt();
        this.constellation = in.readInt();
        this.province = in.readString();
        this.city = in.readString();
        this.school = in.readString();
        this.signature = in.readString();
        this.height = in.readInt();
        this.job = in.readString();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.labels = in.createTypedArray(UserLabel.CREATOR);
        this.location = ParcelUtils.createParcelType(in, LocationInfo.CREATOR);
        this.bubbleUpUser = (in.readByte() != (byte) 0);
        this.theme = ParcelUtils.createParcelType(in, UserTheme.CREATOR);
        this.albumPhotos = in.createTypedArray(AlbumPhoto.CREATOR);
        this.labelStories = in.createTypedArray(LabelStory.CREATOR);
        this.interestTypes = in.createTypedArray(InterestType.CREATOR);
        this.userTags = in.createTypedArray(UserTag.CREATOR);
        this.visitorCount = in.readInt();
        this.myPhotoTotal = in.readInt();
    }

    public Stranger(Stranger other) {
        userId = other.userId;
        labelCode = other.labelCode;
        nickname = other.nickname;
        mobile = other.mobile;
        gender = other.gender;
        birthday = other.birthday;
        age = other.age;
        constellation = other.constellation;
        province = other.province;
        city = other.city;
        school = other.school;
        signature = other.signature;
        height = other.height;
        job = other.job;
        avatar = other.avatar;
        avatarThumb = other.avatarThumb;
        labels = other.labels;
        bubbleUpUser = other.bubbleUpUser;
        location = other.location;
        theme = other.theme;
        albumPhotos = other.albumPhotos;
        labelStories = other.labelStories;
        interestTypes = other.interestTypes;
        userTags = other.userTags;
        visitorCount = other.visitorCount;
        myPhotoTotal = other.myPhotoTotal;
    }

    public Stranger(UserContact other) {
        userId = other.getUserId();
        labelCode = other.getLabelCode();
        nickname = other.getNickname();
        mobile = other.getMobile();
        gender = other.getSex();
        birthday = other.getBirthday();
        age = other.getAge();
        constellation = other.getConstellation();
        province = other.getProvince();
        city = other.getCity();
        school = other.getSchool();
        signature = other.getSignature();
        height = other.getHeight();
        job = other.getJob();
        avatar = other.getAvatar();
        avatarThumb = other.getAvatarThumb();
        labels = other.getLabels();
        location = other.getLocation();
        theme = other.getTheme();
        albumPhotos = other.getAlbumPhotos();
        labelStories = other.getLabelStories();
        interestTypes = other.getInterestTypes();
        userTags = other.getUserTags();
        visitorCount = other.getVisitorCount();
        myPhotoTotal = other.getMyPhotoTotal();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public void setLabelCode(String labelCode) {
        this.labelCode = labelCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getSex() {
        return gender;
    }

    public void setSex(int sex) {
        gender = sex;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public boolean isBirthdayUnknown() {
        return birthday == Long.MAX_VALUE;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getConstellation() {
        return constellation;
    }

    public void setConstellation(int constellation) {
        this.constellation = constellation;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarThumb() {
        return avatarThumb;
    }

    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    public UserLabel[] getLabels() {
        return labels;
    }

    public void setLabels(UserLabel[] labels) {
        this.labels = labels;
    }

    public AlbumPhoto[] getAlbumPhotos() {
        return albumPhotos;
    }

    public void setAlbumPhotos(AlbumPhoto[] albumPhotos) {
        this.albumPhotos = albumPhotos;
    }

    public LabelStory[] getLabelStories() {
        return labelStories;
    }

    public void setLabelStories(LabelStory[] labelStories) {
        this.labelStories = labelStories;
    }

    public InterestType[] getInterestTypes() {
        return interestTypes;
    }

    public void setInterestTypes(InterestType[] interestTypes) {
        this.interestTypes = interestTypes;
    }

    public UserTag[] getUserTags() {
        return userTags;
    }

    public void setUserTags(UserTag[] userTags) {
        this.userTags = userTags;
    }

    public int getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(int visitorCount) {
        this.visitorCount = visitorCount;
    }

    public String getLabelsString() {
        final String labels;

        if (this.labels != null && this.labels.length > 0) {
            labels = LabelCmdUtils.toJsonArray(this.labels).toString();
        } else {
            labels = "";
        }

        return labels;
    }

    public void setLabelsByString(String labels) {
        if (TextUtils.isEmpty(labels)) {
            this.labels = null;
            return;
        }

        UserLabel[] labelArray = null;

        try {
            labelArray = LabelCmdUtils.toUserLabelArray(new JSONArray(labels));
        } catch (JSONException e) {
            L.w(TAG, e);
        } finally {
            this.labels = labelArray;
        }
    }

    public LocationInfo getLocation() {
        return location;
    }

    public String getLocationString() {
        return location != null ? location.toString() : "";
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public void setLocationByString(String location) {
        this.location = LocationInfo.build(location);
    }

    public UserTheme getTheme() {
        return theme;
    }

    public void setTheme(UserTheme theme) {
        this.theme = theme;
    }

    public boolean isBubbleUpUser() {
        return bubbleUpUser;
    }

    public void setIsBubbleUpUser(boolean bubbleUp) {
        bubbleUpUser = bubbleUp;
    }

    public String getShowName() {
        return TextUtils.isEmpty(getNickname()) ? getLabelCode() : getNickname();
    }

    public String getShowName(ContactsManager contactsManager) {
        UserContact contact = contactsManager.getUserContactByUserId(getUserId());
        return (contact != null) ? contact.getShowName() : getShowName();
    }

    public int getMyPhotoTotal() {
        return myPhotoTotal;
    }

    public void setMyPhotoTotal(int myPhotoTotal) {
        this.myPhotoTotal = myPhotoTotal;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(labelCode);
        dest.writeString(nickname);
        dest.writeString(mobile);
        dest.writeInt(gender);
        dest.writeLong(birthday);
        dest.writeInt(age);
        dest.writeInt(constellation);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(school);
        dest.writeString(signature);
        dest.writeInt(height);
        dest.writeString(job);
        dest.writeString(avatar);
        dest.writeString(avatarThumb);
        dest.writeTypedArray(labels, flags);
        ParcelUtils.writeParcelType(dest, location, flags);
        dest.writeByte(bubbleUpUser ? (byte) 1 : (byte) 0);
        ParcelUtils.writeParcelType(dest, theme, flags);
        dest.writeTypedArray(albumPhotos, flags);
        dest.writeTypedArray(labelStories, flags);
        dest.writeTypedArray(interestTypes, flags);
        dest.writeTypedArray(userTags, flags);
        dest.writeInt(visitorCount);
        dest.writeInt(myPhotoTotal);
    }

    public static final Parcelable.Creator<Stranger> CREATOR
            = new Parcelable.Creator<Stranger>() {

        @Override
        public Stranger createFromParcel(Parcel source) {
            return new Stranger(source);
        }

        @Override
        public Stranger[] newArray(int size) {
            return new Stranger[size];
        }
    };

    @Override
    public String toString() {
        return "Stranger{" +
                "labelCode='" + labelCode + '\'' +
                ", userId='" + userId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", mobile='" + mobile + '\'' +
                ", bubbleUpUser=" + bubbleUpUser +
                ", gender=" + gender +
                ", birthday=" + birthday +
                ", age=" + age +
                ", constellation=" + constellation +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", school='" + school + '\'' +
                ", signature='" + signature + '\'' +
                ", height=" + height +
                ", job='" + job + '\'' +
                ", avatar='" + avatar + '\'' +
                ", avatarThumb='" + avatarThumb + '\'' +
                ", labels=" + Arrays.toString(labels) +
                ", location=" + location +
                ", theme=" + theme +
                ", albumPhotos=" + Arrays.toString(albumPhotos) +
                ", labelStories=" + Arrays.toString(labelStories) +
                ", interestTypes=" + Arrays.toString(interestTypes) +
                ", userTags=" + Arrays.toString(userTags) +
                ", visitorCount=" + visitorCount +
                ", myPhotoTotal=" + myPhotoTotal +
                '}';
    }
}
