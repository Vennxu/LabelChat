package com.ekuater.labelchat.ui.fragment.usershowpage;

import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.settings.SettingHelper;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public class BaseUserInfo {

    public String userId;
    public String nickname;
    public int gender;
    public String avatarThumb;
    public String avatar;

    public BaseUserInfo() {
    }

    public BaseUserInfo(String userId, String nickname, int gender,
                        String avatarThumb, String avatar) {
        this.userId = userId;
        this.nickname = nickname;
        this.gender = gender;
        this.avatarThumb = avatarThumb;
        this.avatar = avatar;
    }

    public static BaseUserInfo fromSettingHelper(SettingHelper settingHelper) {
        return new BaseUserInfo(settingHelper.getAccountUserId(),
                settingHelper.getAccountNickname(),
                settingHelper.getAccountSex(),
                settingHelper.getAccountAvatarThumb(),
                settingHelper.getAccountAvatar());
    }

    public static BaseUserInfo fromContact(UserContact contact) {
        return new BaseUserInfo(contact.getUserId(), contact.getShowName(),
                contact.getSex(), contact.getAvatarThumb(), contact.getAvatar());
    }

    public static BaseUserInfo fromStranger(Stranger stranger) {
        return new BaseUserInfo(stranger.getUserId(), stranger.getNickname(),
                stranger.getSex(), stranger.getAvatarThumb(), stranger.getAvatar());
    }
}
