package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.SystemLabel;

import org.json.JSONException;

/**
 * Created by FC on 2015/3/3.
 * @author FanChong
 */
public class HotLabelCommand extends UserCommand {
    private static final String URL= CommandUrl.LABEL_HOT_LABEL;
    public HotLabelCommand(){
        super();
        setUrl(URL);
    }
    public HotLabelCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
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
