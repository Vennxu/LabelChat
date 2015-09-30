package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Leo on 2015/3/1.
 *
 * @author LinYong
 */
public class UserTheme implements Parcelable {

    private String themeName;
    private String topImg;
    private String bottomImg;
    private String themeThumb;

    public UserTheme() {
    }

    private UserTheme(Parcel in) {
        this.themeName = in.readString();
        this.topImg = in.readString();
        this.bottomImg = in.readString();
        this.themeThumb = in.readString();
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getTopImg() {
        return topImg;
    }

    public void setTopImg(String topImg) {
        this.topImg = topImg;
    }

    public String getBottomImg() {
        return bottomImg;
    }

    public void setBottomImg(String bottomImg) {
        this.bottomImg = bottomImg;
    }

    public String getThemeThumb() {
        return themeThumb;
    }

    public void setThemeThumb(String themeThumb) {
        this.themeThumb = themeThumb;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.themeName);
        dest.writeString(this.topImg);
        dest.writeString(this.bottomImg);
        dest.writeString(this.themeThumb);
    }

    public static final Parcelable.Creator<UserTheme> CREATOR
            = new Parcelable.Creator<UserTheme>() {

        public UserTheme createFromParcel(Parcel source) {
            return new UserTheme(source);
        }

        public UserTheme[] newArray(int size) {
            return new UserTheme[size];
        }
    };

    public static UserTheme fromThemeName(String themeName) {
        if (TextUtils.isEmpty(themeName)) {
            throw new IllegalArgumentException("Empty theme name");
        }

        UserTheme theme = new UserTheme();
        theme.setThemeName(themeName);
        theme.setTopImg(themeName + "-top.jpg");
        theme.setBottomImg(themeName + "-bottom.jpg");
        theme.setThemeThumb(themeName + "-thumb.jpg");
        return theme;
    }
}
