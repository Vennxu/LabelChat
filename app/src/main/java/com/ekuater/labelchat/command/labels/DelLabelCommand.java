
package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserLabel;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author LinYong
 */
public class DelLabelCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_DEL_USER_LABEL;

    public DelLabelCommand() {
        super();
        setUrl(URL);
    }

    public void putParamLabels(UserLabel[] labels) {
        JSONArray jsonArray = LabelCmdUtils.toJsonArray(labels);

        if (jsonArray != null) {
            putParam(CommandFields.UserLabel.LABELS, jsonArray);
        }
    }

    public void putParamLabel(UserLabel label) {
        UserLabel[] labels = new UserLabel[]{
                label
        };

        putParamLabels(labels);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
