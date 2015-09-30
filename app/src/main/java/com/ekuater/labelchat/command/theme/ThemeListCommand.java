package com.ekuater.labelchat.command.theme;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.datastruct.UserTheme;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/1.
 *
 * @author LinYong
 */
public class ThemeListCommand extends BaseCommand {

    private static final String URL = CommandUrl.THEME_LIST;

    public ThemeListCommand() {
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public UserTheme[] getThemes() {
            return ThemeCmdUtils.toThemeArray(getValueJsonArray(CommandFields.Theme.THEMES));
        }
    }
}
