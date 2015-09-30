package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.datastruct.SystemLabel;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ListLabelCommand extends SessionCommand {

    private static final String URL = CommandUrl.LABEL_LIST_SYS_LABEL;

    public ListLabelCommand() {
        super();
        setUrl(URL);
    }

    public ListLabelCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public void putParamRequestTime(int requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, String.valueOf(requestTime));
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public SystemLabel[] getSystemLabels() {
            return LabelCmdUtils.toSystemLabelArray(getValueJsonArray(CommandFields.SystemLabel.LABELS));
        }
    }
}
