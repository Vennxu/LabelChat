package com.ekuater.labelchat.command;

import org.json.JSONException;

/**
 * Upload user suggestion to server.
 *
 * @author FanChong
 */
public class FeedbackCommand extends UserCommand {

    private static final String URL = CommandUrl.MISC_FEEDBACK;

    public FeedbackCommand(String session, String userId, String labelCode) {
        super(session, userId);
        setUrl(URL);
        putParamLabelCode(labelCode);
    }

    private void putParamLabelCode(String labelCode) {
        putParam(CommandFields.User.LABEL_CODE, labelCode);
    }

    public void putParamNickname(String nickname) {
        putParam(CommandFields.User.NICKNAME, nickname);
    }

    public void putParamSuggestion(String suggestion) {
        putParam(CommandFields.Normal.SUGGESTION, suggestion);
    }

    public void putParamContactInfo(String contactInfo) {
        putParam(CommandFields.Normal.CONTACT_INFO, contactInfo);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
