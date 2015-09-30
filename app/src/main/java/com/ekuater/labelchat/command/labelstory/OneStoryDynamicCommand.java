package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/3/18.
 *
 * @author FanChong
 */
public class OneStoryDynamicCommand extends UserCommand {
    private static final String URL = CommandUrl.LABEL_STORY_LATELY_ONE_STORY;

    public OneStoryDynamicCommand() {
        super();
        setUrl(URL);
    }

    public OneStoryDynamicCommand(String sessionId, String userId) {
        super(sessionId, userId);
        setUrl(URL);
    }

    public void putQueryUserId(String queryUserId) {
        putParam(CommandFields.StoryLabel.QUERY_USER_ID, queryUserId);
    }

    public static class CommandImageResponse extends BaseCommand.CommandResponse {

        public CommandImageResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStory[] getLabelStory() {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
        }
    }
}
