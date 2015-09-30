package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.confide.PublishContent;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Leo on 2015/4/9.
 *
 * @author LinYong
 */
public class PublishCommand extends UserCommand {

    private static final String URL = CommandUrl.CONFIDE_PUBLISH;

    public PublishCommand() {
        super();
        setUrl(URL);
    }

    public PublishCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamArrayUserId(List<String> arrayUserId) {
        if (arrayUserId != null && arrayUserId.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (String userId : arrayUserId) {
                jsonArray.put(userId);
            }
            putParam(CommandFields.StoryLabel.RELATED_USER_IDS, jsonArray);
        }
    }

    public void putParamPublishContent(PublishContent content) {
        if (content == null) {
            throw new NullPointerException("Empty PublishContent");
        }

        putParam(CommandFields.Confide.CONTENT, appendSpace(content.getContent()));
        putParam(CommandFields.Confide.BG_COLOR, content.getBgColor());
        putParam(CommandFields.Confide.BG_IMG, content.getBgImg());
        putParam(CommandFields.Confide.ROLE, content.getRole());
        putParam(CommandFields.Confide.SEX, String.valueOf(content.getGender()));
        putParam(CommandFields.Confide.POSITION, content.getPosition());
    }

    private String appendSpace(String str) {
        return str != null ? str + " " : null;
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Confide getConfide() {
            return ConfideCmdUtils.toConfide(getValueJson(CommandFields.Confide.CONFIDE_VO));
        }
    }
}
