package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.BaseLabel;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Leo on 2015/2/7.
 *
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public class RecommendStrangerLabelCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_RECOMMEND_STRANGER_LABEL;

    public RecommendStrangerLabelCommand() {
        super();
        setUrl(URL);
    }

    public RecommendStrangerLabelCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamStrangerUserId(String userId) {
        putParam(CommandFields.Normal.STRANGER_USER_ID, userId);
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
