
package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class RegisterCommand extends BaseCommand {

    private static final String URL = CommandUrl.ACCOUNT_REGISTER;

    public RegisterCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam(CommandFields.User.MOBILE, mobile);
    }

    public void putParamPassword(String password) {
        putParam(CommandFields.User.PASSWORD, password);
    }

    public void putParamVerifyCode(String verifyCode) {
        putParam(CommandFields.Normal.CAPTCHA, verifyCode);
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

        public String getLableCode() {
            return getValueString(CommandFields.User.LABEL_CODE);
        }
    }
}
