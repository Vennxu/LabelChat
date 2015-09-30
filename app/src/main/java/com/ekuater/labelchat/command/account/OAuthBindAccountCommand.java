package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Bind a third platform OAuth user to our user using mobile
 *
 * @author LinYong
 */
public class OAuthBindAccountCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_OAUTH_BIND_ACCOUNT;

    public OAuthBindAccountCommand() {
        super();
        setUrl(URL);
    }

    public OAuthBindAccountCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamPlatform(String platform) {
        putParam(CommandFields.OAuth.PLATFORM, platform);
    }

    public void putParamOpenId(String openId) {
        putParam(CommandFields.OAuth.OPEN_ID, openId);
    }

    public void putParamMobile(String mobile) {
        putParam(CommandFields.Normal.MOBILE, mobile);
    }

    public void putParamVerifyCode(String verifyCode) {
        putParam(CommandFields.Normal.CAPTCHA, verifyCode);
    }

    public void putParamNewPassword(String password) {
        putParam(CommandFields.Normal.NEW_PASSWORD, password);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
