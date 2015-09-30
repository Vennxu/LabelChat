package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ResetPasswordCommand extends BaseCommand {

    private static final String URL = CommandUrl.ACCOUNT_RESET_PASSWORD;

    public ResetPasswordCommand() {
        super();
        setUrl(URL);
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

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
