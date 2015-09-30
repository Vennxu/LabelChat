package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UploadCommand;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Label on 2015/1/11.
 *
 * @author XuWenxiang
 */
public class SendLabelStoryImageCommand extends UploadCommand {

    private static final String URL = CommandUrl.LABEL_STORY_IMAGEPOST;

    public SendLabelStoryImageCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabelId(String labelId) {
        putParam(CommandFields.StoryLabel.CATEGORY_ID, labelId);
    }

    public void putParamContent(String content) {
        putParam(CommandFields.StoryLabel.STORY_CONTENT, content);
    }

    public void putParamType(String type) {
        putParam(CommandFields.StoryLabel.TYPE, type);
    }

    public void putParamUserId(String userId) {
        putParam(CommandFields.Album.RELATED_USER_ID, userId);
    }

    public void addPhoto(File photo) throws FileNotFoundException {
        addFile(photo);
    }

    public static class CommandResponse extends UploadCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
