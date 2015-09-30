package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.BaseLabel;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Leo on 2015/1/26.
 *
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public class RecommendLabelCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_RECOMMEND_LABEL;

    public RecommendLabelCommand() {
        super();
        setUrl(URL);
    }

    public RecommendLabelCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamFriendUserId(String userId) {
        putParam(CommandFields.Normal.FRIEND_USER_ID, userId);
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
