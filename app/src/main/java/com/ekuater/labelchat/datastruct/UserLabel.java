
package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;

/**
 * Use label class
 *
 * @author LinYong
 */
public class UserLabel implements Parcelable, Comparable<UserLabel> {

    private String name;
    private String id;
    private long time;
    private long totalUser;
    private int praiseCount;
    private int integral;
    private UserLabelFeed feed;
    private String image;
    private int gradeNum;
    private int gradeTotal;

    public UserLabel(String name, String id) {
        this(name, id, 0, 0, 0, 0);
    }

    public UserLabel(String name, String id,
                     int praiseCount) {
        this(name, id, 0, 0, praiseCount, 0);
    }

    public UserLabel(String name, String id, long time,
                     long totalUser) {
        this(name, id, time, totalUser, 0, 0);
    }

    public UserLabel(UserLabel other) {
        this(other.getName(), other.getId(), other.getTime(), other.getTotalUser(),
                other.getPraiseCount(), other.getIntegral());
        setImage(other.getImage());
    }

    public UserLabel(String name, String id, long time, long totalUser,
                     int praiseCount, int integral) {
        this.name = name;
        this.id = id;
        this.time = time;
        this.totalUser = totalUser;
        this.praiseCount = praiseCount;
        this.integral = integral;
    }

    private UserLabel(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        this.time = in.readLong();
        this.totalUser = in.readLong();
        this.praiseCount = in.readInt();
        this.integral = in.readInt();
        this.feed = in.readParcelable(UserLabelFeed.class.getClassLoader());
        this.image = in.readString();
        this.gradeNum = in.readInt();
        this.gradeTotal = in.readInt();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public long getTotalUser() {
        return totalUser;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
    }

    public int getIntegral() {
        return integral;
    }

    public void setIntegral(int integral) {
        this.integral = integral;
    }

    public UserLabelFeed getFeed() {
        return feed;
    }

    public void setFeed(UserLabelFeed feed) {
        this.feed = feed;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getGradeNum() {
        return gradeNum;
    }

    public void setGradeNum(int gradeNum) {
        this.gradeNum = gradeNum;
    }

    public int getGradeTotal() {
        return gradeTotal;
    }

    public void setGradeTotal(int gradeTotal) {
        this.gradeTotal = gradeTotal;
    }

    public float getAverageGrade() {
        float grade = (gradeNum > 0)
                ? Math.max(((float) gradeTotal) / gradeNum, 0.0F)
                : 0.0F;
        return Math.round(grade * 10) / 10.0F;
    }

    public BaseLabel toBaseLabel() {
        return new BaseLabel(name, id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
        dest.writeLong(this.time);
        dest.writeLong(this.totalUser);
        dest.writeInt(this.praiseCount);
        dest.writeInt(this.integral);
        dest.writeParcelable(this.feed, flags);
        dest.writeString(this.image);
        dest.writeInt(this.gradeNum);
        dest.writeInt(this.gradeTotal);
    }

    public static final Parcelable.Creator<UserLabel> CREATOR = new Parcelable.Creator<UserLabel>() {

        @Override
        public UserLabel createFromParcel(Parcel source) {
            return new UserLabel(source);
        }

        @Override
        public UserLabel[] newArray(int size) {
            return new UserLabel[size];
        }
    };

    @Override
    public int compareTo(@NonNull UserLabel another) {
        return (int) (time - another.time);
    }

    @Override
    public String toString() {
        return LabelCmdUtils.toJson(this).toString();
    }
}
