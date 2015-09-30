
package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * This command user to update current account settings
 *
 * @author LinYong
 */
public class UpdatePersonalInfoCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_UPDATE_INFO;

    public UpdatePersonalInfoCommand() {
        super();
        setUrl(URL);
    }

    public UpdatePersonalInfoCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamAvatar(String avatar) {
        putParam(CommandFields.User.AVATAR, avatar);
    }

    public void putParamNickname(String nickname) {
        putParam(CommandFields.User.NICKNAME, nickname);
    }

    public void putParamSex(int sex) {
        putParam(CommandFields.User.SEX, sex);
    }

    public void putParamProvince(String province) {
        putParam(CommandFields.User.PROVINCE, province);
    }

    public void putParamCity(String city) {
        putParam(CommandFields.User.CITY, city);
    }

    public void putParamConstellation(int constellation) {
        putParam(CommandFields.User.CONSTELLATION, constellation);
    }

    public void putParamSchool(String school) {
        putParam(CommandFields.User.SCHOOL, school);
    }

    public void putParamSignature(String signature) {
        putParam(CommandFields.User.SIGNATURE, signature);
    }

    public void putParamTheme(String theme) {
        putParam(CommandFields.User.THEME, theme);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

       public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
