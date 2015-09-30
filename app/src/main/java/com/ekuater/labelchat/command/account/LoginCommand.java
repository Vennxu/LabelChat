
package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class LoginCommand extends BaseCommand {

    private static final String TAG = LoginCommand.class.getSimpleName();

    private static final String URL = CommandUrl.ACCOUNT_LOGIN;

    public LoginCommand() {
        super();
        setUrl(URL);
    }

    public void putParamDeviceID(String deviceID) {
        putParam(CommandFields.Normal.DEVICE_ID, deviceID);
    }

    public void putParamDeviceName(String deviceName) {
        putParam(CommandFields.Normal.DEVICE_NAME, deviceName);
    }

    public void putParamOsVersion(String osVersion) {
        putParam(CommandFields.Normal.DEVICE_OS_VERSION, osVersion);
    }

    public void putParamLoginText(String loginText) {
        putParam(CommandFields.Normal.LOGIN_TEXT, loginText);
    }

    public void putParamPassword(String password) {
        putParam(CommandFields.User.PASSWORD, password);
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

        public String getToken() {
            return getValueString(CommandFields.Normal.TOKEN);
        }
    }
}
