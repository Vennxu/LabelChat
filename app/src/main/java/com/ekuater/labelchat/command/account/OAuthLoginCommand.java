package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * Third platform OAuth interface command
 *
 * @author LinYong
 */
public class OAuthLoginCommand extends BaseCommand {

    private static final String TAG = OAuthLoginCommand.class.getSimpleName();

    private static final String URL = CommandUrl.ACCOUNT_OAUTH_LOGIN;

    public OAuthLoginCommand() {
        super();
        setUrl(URL);
    }

    public void putParamPlatform(String platform) {
        putParam(CommandFields.OAuth.PLATFORM, platform);
    }

    public void putParamOpenId(String openId) {
        putParam(CommandFields.OAuth.OPEN_ID, openId);
    }

    public void putParamAccessToken(String accessToken) {
        putParam(CommandFields.OAuth.ACCESS_TOKEN, accessToken);
    }

    public void putParamTokenExpire(String tokenExpire) {
        putParam(CommandFields.OAuth.TOKEN_EXPIRE, tokenExpire);
    }

    public void putParamAvatar(String avatar) {
        putParam(CommandFields.User.AVATAR, avatar);
    }

    public void putParamAvatarThumb(String avatar) {
        putParam(CommandFields.User.AVATAR_THUMB, avatar);
    }

    public void putParamNickname(String nickname) {
        putParam(CommandFields.User.NICKNAME, nickname);
    }

    public void putParamSex(int sex) {
        putParam(CommandFields.User.SEX, sex);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public String getSession() {
            return getValueString(CommandFields.Base.SESSION);
        }

        public PersonalInfo getPersonalInfo() {
            PersonalInfo info = null;

            try {
                info = new PersonalInfo(getValueJson(CommandFields.User.USER));
            } catch (Exception e) {
                L.w(TAG, e);
            }

            return info;
        }

        public String getPassword() {
            return getValueString(CommandFields.User.PASSWORD);
        }

        public String getToken() {
            return getValueString(CommandFields.Normal.TOKEN);
        }
    }
}
