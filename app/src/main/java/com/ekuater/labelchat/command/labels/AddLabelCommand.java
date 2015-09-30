
package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.BaseLabel;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author LinYong
 */
public class AddLabelCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_ADD_USER_LABEL;

    public AddLabelCommand() {
        super();
        setUrl(URL);
    }

    public AddLabelCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabels(BaseLabel[] labels) {
        JSONArray jsonArray = LabelCmdUtils.toJsonArray(labels);

        if (jsonArray != null) {
            putParam(CommandFields.BaseLabel.LABELS, jsonArray);
        }
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
