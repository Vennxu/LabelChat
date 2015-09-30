package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Leo on 2015/2/11.
 *
 * @author LinYong
 */
public class ShareStatisticsCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_STORY_SHARE_STATISTICS;

    public ShareStatisticsCommand() {
        super();
        setUrl(URL);
    }

    public ShareStatisticsCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamSharePlatform(String sharePlatform) {
        putParam(CommandFields.Normal.SHARE_PLATFORM, sharePlatform);
    }

    public void putParamLabelStoryId(String labelStoryId) {
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID, labelStoryId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
