
package com.ekuater.labelchat.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.BuildConfig;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.command.tag.TagCmdUtils;
import com.ekuater.labelchat.command.theme.ThemeCmdUtils;
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.settings.Settings.Global;
import com.ekuater.labelchat.settings.Settings.Personal;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public final class SettingHelper {

    private static final String TAG = SettingHelper.class.getSimpleName();

    private static SettingHelper sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingHelper(context.getApplicationContext());
        }
    }

    public static SettingHelper getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }

        return sInstance;
    }

    private final ContentResolver mCR;

    private SettingHelper(Context context) {
        mCR = context.getContentResolver();
    }

    private String checkNull(String string) {
        return string == null ? "" : string;
    }

    public void clearAccountSettings() {
        Personal.clear(mCR);
    }

    public boolean isPrevCurrVersionSame() {
        return Global.getInt(mCR, Global.PREV_VERSION_CODE, -1)
                == BuildConfig.VERSION_CODE;
    }

    public void updatePrevVersion() {
        Global.putInt(mCR, Global.PREV_VERSION_CODE, BuildConfig.VERSION_CODE);
    }

    public boolean isAppFirstLaunch() {
        return Global.getInt(mCR, Global.APP_FIRST_LAUNCH_KEY, 1) != 0;
    }

    public void setAppFirstLaunched() {
        Global.putInt(mCR, Global.APP_FIRST_LAUNCH_KEY, 0);
    }

    public boolean isManualExitApp() {
        return Global.getInt(mCR, Global.MANUAL_EXIT_APP, 0) != 0;
    }

    public void setManualExitApp(boolean manualExit) {
        Global.putInt(mCR, Global.MANUAL_EXIT_APP, manualExit ? 1 : 0);
    }

    public int getLoginMethod() {
        return Global.getInt(mCR, Global.LOGIN_METHOD_KEY,
                SettingConstants.LOGIN_METHOD_AUTO_LOGIN);
    }

    public void setLoginMethod(int value) {
        Global.putInt(mCR, Global.LOGIN_METHOD_KEY, value);
    }

    public int getAccountLoginAuthType() {
        return Personal.getInt(mCR, Personal.ACCOUNT_AUTH_TYPE_KEY,
                ConstantCode.AUTH_TYPE_NORMAL);
    }

    public void setAccountLoginAuthType(int type) {
        Personal.putInt(mCR, Personal.ACCOUNT_AUTH_TYPE_KEY, type);
    }

    public String getAccountOAuthPlatform() {
        return Personal.getString(mCR, Personal.ACCOUNT_OAUTH_PLATFORM_KEY);
    }

    public void setAccountOAuthPlatform(String platform) {
        Personal.putString(mCR, Personal.ACCOUNT_OAUTH_PLATFORM_KEY, platform);
    }

    public String getAccountOAuthOpenId() {
        return Personal.getString(mCR, Personal.ACCOUNT_OAUTH_OPEN_ID_KEY);
    }

    public void setAccountOAuthOpenId(String openId) {
        Personal.putString(mCR, Personal.ACCOUNT_OAUTH_OPEN_ID_KEY, openId);
    }

    public String getAccountSession() {
        return checkNull(Personal.getString(mCR, Personal.ACCOUNT_SESSION_KEY));
    }

    public void setAccountSession(String session) {
        Personal.putString(mCR, Personal.ACCOUNT_SESSION_KEY, session);
    }

    public String getAccountPassword() {
        return Personal.getString(mCR, Personal.ACCOUNT_PASSWORD_KEY);
    }

    public void setAccountPassword(String password) {
        Personal.putString(mCR, Personal.ACCOUNT_PASSWORD_KEY, password);
    }

    public String getAccountUserId() {
        return checkNull(Personal.getString(mCR, Personal.ACCOUNT_USER_ID_KEY));
    }

    public void setAccountUserId(String userId) {
        Personal.putString(mCR, Personal.ACCOUNT_USER_ID_KEY, userId);
    }

    public String getAccountLabelCode() {
        return checkNull(Personal.getString(mCR, Personal.ACCOUNT_LABEL_CODE_KEY));
    }

    public void setAccountLabelCode(String labelCode) {
        Personal.putString(mCR, Personal.ACCOUNT_LABEL_CODE_KEY, labelCode);
    }

    public long getAccountUpdateTime() {
        return Personal.getLong(mCR, Personal.ACCOUNT_UPDATE_TIME, 0);
    }

    public void setAccountUpdateTime(long time) {
        Personal.putLong(mCR, Personal.ACCOUNT_UPDATE_TIME, time);
    }

    public long getAccountLastLoginDate() {
        return Personal.getLong(mCR, Personal.ACCOUNT_LAST_LOGIN_DATE, Long.MIN_VALUE);
    }

    public void setAccountLastLoginDate(long date) {
        Personal.putLong(mCR, Personal.ACCOUNT_LAST_LOGIN_DATE, date);
    }

    public UserLabel[] getAccountUserLabels() {
        final String value = Personal.getString(mCR, Personal.ACCOUNT_USER_LABELS_KEY);
        UserLabel[] labels = null;

        if (!TextUtils.isEmpty(value)) {
            try {
                final JSONArray jsonArray = new JSONArray(value);
                labels = LabelCmdUtils.toUserLabelArray(jsonArray);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return labels;
    }

    public void setAccountUserLabels(UserLabel[] labels) {
        final JSONArray jsonArray = LabelCmdUtils.toJsonArray(labels);
        final String value = (jsonArray != null) ? jsonArray.toString() : "";
        Personal.putString(mCR, Personal.ACCOUNT_USER_LABELS_KEY, value);
    }

    public String getAccountNickname() {
        return Personal.getString(mCR, Personal.ACCOUNT_NICKNAME_KEY);
    }

    public void setAccountNickname(String nickname) {
        Personal.putString(mCR, Personal.ACCOUNT_NICKNAME_KEY, nickname);
    }

    public String getAccountMobile() {
        return Personal.getString(mCR, Personal.ACCOUNT_MOBILE_KEY);
    }

    public void setAccountMobile(String mobile) {
        Personal.putString(mCR, Personal.ACCOUNT_MOBILE_KEY, mobile);
    }

    public String getAccountEmail() {
        return Personal.getString(mCR, Personal.ACCOUNT_EMAIL_KEY);
    }

    public void setAccountEmail(String email) {
        Personal.putString(mCR, Personal.ACCOUNT_EMAIL_KEY, email);
    }

    public int getAccountSex() {
        return Personal.getInt(mCR, Personal.ACCOUNT_SEX_KEY, 0);
    }

    public void setAccountSex(int sex) {
        Personal.putInt(mCR, Personal.ACCOUNT_SEX_KEY, sex);
    }

    public long getAccountBirthday() {
        return Personal.getLong(mCR, Personal.ACCOUNT_BIRTHDAY_KEY, Long.MAX_VALUE);
    }

    public void setAccountBirthday(long birthday) {
        Personal.putLong(mCR, Personal.ACCOUNT_BIRTHDAY_KEY, birthday);
    }

    public int getAccountAge() {
        return Personal.getInt(mCR, Personal.ACCOUNT_AGE_KEY, -1);
    }

    public void setAccountAge(int age) {
        Personal.putInt(mCR, Personal.ACCOUNT_AGE_KEY, age);
    }

    public int getAccountConstellation() {
        return Personal.getInt(mCR, Personal.ACCOUNT_CONSTELLATION_KEY, -1);
    }

    public void setAccountConstellation(int constellation) {
        Personal.putInt(mCR, Personal.ACCOUNT_CONSTELLATION_KEY, constellation);
    }

    public String getAccountProvince() {
        return Personal.getString(mCR, Personal.ACCOUNT_PROVINCE_KEY);
    }

    public void setAccountProvince(String province) {
        Personal.putString(mCR, Personal.ACCOUNT_PROVINCE_KEY, province);
    }

    public String getAccountCity() {
        return Personal.getString(mCR, Personal.ACCOUNT_CITY_KEY);
    }

    public void setAccountCity(String city) {
        Personal.putString(mCR, Personal.ACCOUNT_CITY_KEY, city);
    }

    public String getAccountSchool() {
        return Personal.getString(mCR, Personal.ACCOUNT_SCHOOL_KEY);
    }

    public void setAccountSchool(String school) {
        Personal.putString(mCR, Personal.ACCOUNT_SCHOOL_KEY, school);
    }

    public int getAccountHeight() {
        return Personal.getInt(mCR, Personal.ACCOUNT_HEIGHT_KEY, -1);
    }

    public void setAccountHeight(int height) {
        Personal.putInt(mCR, Personal.ACCOUNT_HEIGHT_KEY, height);
    }

    public String getAccountJob() {
        return Personal.getString(mCR, Personal.ACCOUNT_JOB_KEY);
    }

    public void setAccountJob(String job) {
        Personal.putString(mCR, Personal.ACCOUNT_JOB_KEY, job);
    }


    public String getAccountAppearanceFace() {
        return Personal.getString(mCR, Personal.ACCOUNT_APPEARANCE_FACE);
    }

    public void setAccountAppearanceFace(String face) {
        Personal.putString(mCR, Personal.ACCOUNT_APPEARANCE_FACE, face);
    }

    public String getAccountSignature() {
        return Personal.getString(mCR, Personal.ACCOUNT_SIGNATURE_KEY);
    }

    public void setAccountSignature(String signature) {
        Personal.putString(mCR, Personal.ACCOUNT_SIGNATURE_KEY, signature);
    }

    public String getAccountAvatar() {
        return Personal.getString(mCR, Personal.ACCOUNT_AVATAR_KEY);
    }

    public void setAccountAvatar(String avatar) {
        Personal.putString(mCR, Personal.ACCOUNT_AVATAR_KEY, avatar);
    }

    public String getAccountAvatarThumb() {
        return Personal.getString(mCR, Personal.ACCOUNT_AVATAR_THUMB_KEY);
    }

    public void setAccountAvatarThumb(String avatarThumb) {
        Personal.putString(mCR, Personal.ACCOUNT_AVATAR_THUMB_KEY, avatarThumb);
    }

    public String getAccountRongCloudToken() {
        return Personal.getString(mCR, Personal.ACCOUNT_RONGCLOUD_TOKEN);
    }

    public void setAccountRongCloudToken(String token) {
        Personal.putString(mCR, Personal.ACCOUNT_RONGCLOUD_TOKEN, token);
    }

    public boolean isAccountNewFeedback() {
        return Personal.getInt(mCR, Personal.ACCOUNT_NEW_FEEDBACK, 0) != 0;
    }

    public void setAccountNewFeedback(boolean newUser) {
        Personal.putInt(mCR, Personal.ACCOUNT_NEW_FEEDBACK, newUser ? 1 : 0);
    }

    public String getChatBackground() {
        return Global.getString(mCR, Global.CHAT_BACKGROUND);
    }

    public void setChatBackground(String background) {
        Global.putString(mCR, Global.CHAT_BACKGROUND, background);
    }

    public int getChatFontSize() {
        return Global.getInt(mCR, Global.CHAT_FONT_SIZE, 0);
    }

    public void setChatFontSize(int fontSize) {
        Global.putInt(mCR, Global.CHAT_FONT_SIZE, fontSize);
    }

    public boolean getAccountMusic() {
        String music = Personal.getString(mCR, Personal.ACCOUNT_GETGAME_MUSIC);
        if (music == null) {
            music = "true";
        }
        return Boolean.parseBoolean(music);
    }

    public void setAccountMusic(boolean isMusic) {
        Personal.putString(mCR, Personal.ACCOUNT_GETGAME_MUSIC, String.valueOf(isMusic));
    }

    public void setUserTheme(String theme) {
        Personal.putString(mCR, Personal.USER_THEME, theme);
    }

    public String getUserTheme() {
        return Personal.getString(mCR, Personal.USER_THEME);
    }

    public UserTag[] getAccountUserTags() {
        final String value = Personal.getString(mCR, Personal.ACCOUNT_USER_TAGS_KEY);
        UserTag[] tags = null;

        if (!TextUtils.isEmpty(value)) {
            try {
                final JSONArray jsonArray = new JSONArray(value);
                tags = TagCmdUtils.toUserTagArray(jsonArray);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return tags;
    }

    public void setAccountUserTags(UserTag[] tags) {
        final JSONArray jsonArray = TagCmdUtils.toJsonArray(tags);
        final String value = (jsonArray != null) ? jsonArray.toString() : "";
        Personal.putString(mCR, Personal.ACCOUNT_USER_TAGS_KEY, value);
    }

    public void setChatBg(ChatBg chatBg) {
        JSONObject object = ThemeCmdUtils.toJson(chatBg);
        String value = (object != null) ? object.toString() : "";
        Personal.putString(mCR, Personal.CHAT_BG_KEY, value);
    }

    public ChatBg getChatBg() {
        String value = Personal.getString(mCR, Personal.CHAT_BG_KEY);
        ChatBg chatBg = null;
        if (!TextUtils.isEmpty(value)) {
            chatBg = new ChatBg();
            try {
                JSONObject json = new JSONObject(value);
                int id = json.getInt(CommandFields.Theme.ID);
                String bgImg = json.getString(CommandFields.Theme.BG_IMG);
                String bgThumb = json.getString(CommandFields.Theme.BG_THUMB);
                int serialNum = json.getInt(CommandFields.Theme.SERIAL_NUM);
                chatBg.setId(id);
                chatBg.setBgImg(bgImg);
                chatBg.setBgThumb(bgThumb);
                chatBg.setSerialNum(serialNum);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return chatBg;
    }

    public void setShakeSetting(boolean arg) {
        Personal.putBoolean(mCR, Personal.MESSAGE_NOTIFY_SHAKE_SETTING, arg);
    }

    public boolean getShakeSetting() {
        return Personal.getBoolean(mCR, Personal.MESSAGE_NOTIFY_SHAKE_SETTING, false);
    }

    public void setVoiceSetting(boolean arg) {
        Personal.putBoolean(mCR, Personal.MESSAGE_NOTIFY_VOICE_SETTING, arg);
    }

    public boolean getVoiceSetting() {
        return Personal.getBoolean(mCR, Personal.MESSAGE_NOTIFY_VOICE_SETTING, true);
    }

}
