package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelPraise;

import org.json.JSONException;

/**
 * Created by Leo on 2015/1/14.
 *
 * @author LinYong
 */
public class QueryPraiseCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_PRAISE_COUNT;

    public QueryPraiseCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamQueryUserId(String userId) {
        putParam(CommandFields.Normal.QUERY_USER_ID, userId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelPraise[] getLabelPraises() {
            return LabelCmdUtils.toLabelPraiseArray(getValueJsonArray(
                    CommandFields.UserLabel.LABEL_PRAISES));
        }
    }
}
