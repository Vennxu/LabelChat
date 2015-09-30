package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class CheckVerifyCodeCommand extends BaseCommand {

    private static final String URL = CommandUrl.ACCOUNT_CHECK_VERIFY_CODE;

    public CheckVerifyCodeCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMobile(String mobile) {
        putParam(CommandFields.Normal.MOBILE, mobile);
    }

    public void putParamVerifyCode(String verifyCode) {
        putParam(CommandFields.Normal.CAPTCHA, verifyCode);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public String getMobile() {
            return getValueString(CommandFields.Normal.MOBILE);
        }

        public String getVerifyCode() {
            return getValueString(CommandFields.Normal.CAPTCHA);
        }
    }
}
