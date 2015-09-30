package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserLabel;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class EnumLabelCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_ENUM_USER_LABEL;

    public EnumLabelCommand() {
        super();
        setUrl(URL);
    }

    public EnumLabelCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public UserLabel[] getUserLabels() {
            return LabelCmdUtils.toUserLabelArray(getValueJsonArray(CommandFields.UserLabel.LABELS));
        }
    }
}
