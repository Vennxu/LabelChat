
package com.ekuater.labelchat.datastruct;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Contact data structure
 *
 * @author LinYong
 */
public class UserContact implements Parcelable {

    private static final String TAG = UserContact.class.getSimpleName();

    private long id;
    private String userId;
    private String labelCode;
    private String nickname;
    private String mobile;
    private String remarkName;
    private int gender;
    private long birthday;
    private int age;
    private int constellation;
    private String province;
    private String city;
    private String school;
    private int height;
    private String job;
    private String signature;
    private String avatar;
    private String avatarThumb;
    private UserLabel[] labels;
    private LocationInfo location;
    private String appearanceFace;
    private LabelStoryFeedTipMessage labelStoryFeedTipMessage;
    private UserTheme theme;
    private AlbumPhoto[] albumPhotos;
    private LabelStory[] labelStories;
    private InterestType[] interestTypes;
    private UserTag[] userTags;
    private int visitorCount;
    private int myPhotoTotal;

    public UserContact() {
    }

    private UserContact(Parcel in) {
        this.id = in.readLong();
        this.userId = in.readString();
        this.labelCode = in.readString();
        this.nickname = in.readString();
        this.mobile = in.readString();
        this.remarkName = in.readString();
        this.gender = in.readInt();
        this.birthday = in.readLong();
        this.age = in.readInt();
        this.constellation = in.readInt();
        this.province = in.readString();
        this.city = in.readString();
        this.school = in.readString();
        this.height = in.readInt();
        this.job = in.readString();
        this.signature = in.readString();
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.labels = in.createTypedArray(UserLabel.CREATOR);
        this.location = in.readParcelable(LocationInfo.class.getClassLoader());
        this.appearanceFace = in.readString();
        this.theme = in.readParcelable(UserTheme.class.getClassLoader());
        this.albumPhotos = in.createTypedArray(AlbumPhoto.CREATOR);
        this.labelStories = in.createTypedArray(LabelStory.CREATOR);
        this.interestTypes = in.createTypedArray(InterestType.CREATOR);
        this.userTags = in.createTypedArray(UserTag.CREATOR);
        this.visitorCount = in.readInt();
        this.myPhotoTotal = in.readInt();
    }

    public UserContact(UserContact other) {
        id = other.id;
        userId = other.userId;
        labelCode = other.labelCode;
        nickname = other.nickname;
        mobile = other.mobile;
        remarkName = other.remarkName;
        gender = other.gender;
        birthday = other.birthday;
        age = other.age;
        constellation = other.constellation;
        province = other.province;
        city = other.city;
        school = other.school;
        height = other.height;
        job = other.job;
        signature = other.signature;
        avatar = other.avatar;
        avatarThumb = other.avatarThumb;
        labels = other.labels;
        location = other.location;
        appearanceFace = other.appearanceFace;
        labelStoryFeedTipMessage = other.labelStoryFeedTipMessage;
        theme = other.theme;
        albumPhotos = other.albumPhotos;
        labelStories = other.labelStories;
        interestTypes = other.interestTypes;
        userTags = other.userTags;
        visitorCount = other.visitorCount;
        myPhotoTotal = other.myPhotoTotal;
    }

    public UserContact(Stranger other) {
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
        height = other.getHeight();
        job = other.getJob();
        signature = other.getSignature();
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

    public LabelStoryFeedTipMessage getLabelStoryFeedTipMessage() {
        return labelStoryFeedTipMessage;
    }

    public void setLabelStoryFeedTipMessage(LabelStoryFeedTipMessage labelStoryFeedTipMessage) {
        this.labelStoryFeedTipMessage = labelStoryFeedTipMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long uid) {
        id = uid;
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

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public void setLocationByString(String location) {
        this.location = LocationInfo.build(location);
    }

    public String getAppearanceFace() {
        return appearanceFace;
    }

    public void setAppearanceFace(String appearanceFace) {
        this.appearanceFace = appearanceFace;
    }

    public UserTheme getTheme() {
        return theme;
    }

    public void setTheme(UserTheme theme) {
        this.theme = theme;
    }

    public String getShowName() {
        String name;

        if (!TextUtils.isEmpty(remarkName)) {
            name = remarkName;
        } else if (!TextUtils.isEmpty(nickname)) {
            name = nickname;
        } else {
            name = labelCode;
        }
        return name;
    }

    public InterestType[] getInterestTypes() {
        return interestTypes;
    }

    public void setInterestTypes(InterestType[] interestTypes) {
        this.interestTypes = interestTypes;
    }

    public LabelStory[] getLabelStories() {
        return labelStories;
    }

    public void setLabelStories(LabelStory[] labelStories) {
        this.labelStories = labelStories;
    }

    public AlbumPhoto[] getAlbumPhotos() {
        return albumPhotos;
    }

    public void setAlbumPhotos(AlbumPhoto[] albumPhotos) {
        this.albumPhotos = albumPhotos;
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
        dest.writeLong(id);
        dest.writeString(userId);
        dest.writeString(labelCode);
        dest.writeString(nickname);
        dest.writeString(mobile);
        dest.writeString(remarkName);
        dest.writeInt(gender);
        dest.writeLong(birthday);
        dest.writeInt(age);
        dest.writeInt(constellation);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(school);
        dest.writeInt(height);
        dest.writeString(job);
        dest.writeString(signature);
        dest.writeString(avatar);
        dest.writeString(avatarThumb);
        dest.writeTypedArray(labels, flags);
        dest.writeParcelable(location, flags);
        dest.writeString(appearanceFace);
        dest.writeParcelable(theme, flags);
        dest.writeTypedArray(albumPhotos, flags);
        dest.writeTypedArray(labelStories, flags);
        dest.writeTypedArray(interestTypes, flags);
        dest.writeTypedArray(userTags, flags);
        dest.writeInt(visitorCount);
        dest.writeInt(myPhotoTotal);
    }

    public static final Parcelable.Creator<UserContact> CREATOR = new Parcelable.Creator<UserContact>() {

        @Override
        public UserContact createFromParcel(Parcel source) {
            return new UserContact(source);
        }

        @Override
        public UserContact[] newArray(int size) {
            return new UserContact[size];
        }
    };

    public static String getConstellationString(Resources resources, int constellation) {
        return MiscUtils.getConstellationString(resources, constellation);
    }

    public static String getAgeString(Resources resources, int age) {
        return MiscUtils.getAgeString(resources, age);
    }

    public static String getHeightString(Resources resources, int height) {
        return MiscUtils.getHeightString(resources, height);
    }
}
