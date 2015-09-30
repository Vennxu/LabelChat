package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.datastruct.SystemLabel;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QueryLabelCommand extends SessionCommand {

    private static final String URL = CommandUrl.LABEL_QUERY_SYS_LABEL;

    public QueryLabelCommand() {
        super();
        setUrl(URL);
    }

    public QueryLabelCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public void putParamKeyword(String keyword) {
        putParam(CommandFields.Normal.KEYWORD, keyword);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public SystemLabel[] getSysLabels() {
            return LabelCmdUtils.toSystemLabelArray(getValueJsonArray(CommandFields.SystemLabel.LABELS));
        }
    }
}
