package com.ekuater.labelchat.command.account;

import android.content.Context;

import com.ekuater.labelchat.command.CommandFields.User;
import com.ekuater.labelchat.settings.SettingHelper;

import org.json.JSONObject;

/**
 * @author LinYong
 */
public class PersonalInfo {

    private final JSONObject mUser;

    public PersonalInfo(JSONObject userInfo) {
        mUser = userInfo;
    }

    private String getValueString(String name) {
        return mUser.optString(name, null);
    }

    private int getValueInt(String name, int fallback) {
        return mUser.optInt(name, fallback);
    }

    private long getValueLong(String name, long fallback) {
        return mUser.optLong(name, fallback);
    }

    public String getUserId() {
        return getValueString(User.USER_ID);
    }

    public String getLabelCode() {
        return getValueString(User.LABEL_CODE);
    }

    public String getEmail() {
        return getValueString(User.EMAIL);
    }

    public String getMobile() {
        return getValueString(User.MOBILE);
    }

    public String getNickName() {
        return getValueString(User.NICKNAME);
    }

    public long getLastLoginDate() {
        return getValueLong(User.LAST_LOGIN_DATE, Long.MIN_VALUE);
    }

    public long getBirthday() {
        return getValueLong(User.BIRTHDAY, Long.MAX_VALUE);
    }

    public int getAge() {
        return getValueInt(User.AGE, -1);
    }

    public String getProvince() {
        return getValueString(User.PROVINCE);
    }

    public String getCity() {
        return getValueString(User.CITY);
    }

    public int getSex() {
        return getValueInt(User.SEX, 0);
    }

    public int getConstellation() {
        return getValueInt(User.CONSTELLATION, -1);
    }

    public String getSchool() {
        return getValueString(User.SCHOOL);
    }

    public String getAvatar() {
        return getValueString(User.AVATAR);
    }

    public String getAvatarThumb() {
        return getValueString(User.AVATAR_THUMB);
    }

    public String getSignature() {
        return getValueString(User.SIGNATURE);
    }

    public String getAppearanceFace() {
        return getValueString(User.APPEARANCE_FACE);
    }

    public String getTheme() {
        return getValueString(User.THEME);
    }

    public void saveToSetting(Context context) {
        SettingHelper helper = SettingHelper.getInstance(context);

        helper.setAccountNickname(getNickName());
        helper.setAccountLastLoginDate(getLastLoginDate());
        helper.setAccountMobile(getMobile());
        helper.setAccountEmail(getEmail());
        helper.setAccountBirthday(getBirthday());
        helper.setAccountAge(getAge());
        helper.setAccountProvince(getProvince());
        helper.setAccountCity(getCity());
        helper.setAccountSex(getSex());
        helper.setAccountConstellation(getConstellation());
        helper.setAccountSchool(getSchool());
        helper.setAccountSignature(getSignature());
        helper.setAccountAvatar(getAvatar());
        helper.setAccountAvatarThumb(getAvatarThumb());
        helper.setAccountAppearanceFace(getAppearanceFace());
        helper.setUserTheme(getTheme());
    }
}
