package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UploadCommand;
import com.ekuater.labelchat.datastruct.LabelStory;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Leo on 2015/4/15.
 *
 * @author LinYong
 */
public class PostMediaStoryCommand extends UploadCommand {

    private static final String URL = CommandUrl.LABEL_STORY_POST_MEDIA;

    public PostMediaStoryCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamCategoryId(String categoryId) {
        putParam(CommandFields.StoryLabel.CATEGORY_ID, categoryId);
    }

    public void putParamContent(String content) {
        putParam(CommandFields.StoryLabel.STORY_CONTENT, content);
    }

    public void putParamRelatedUser(String userId) {
        putParam(CommandFields.Album.RELATED_USER_ID, userId);
    }

    public void putParamType(String type) {
        putParam(CommandFields.StoryLabel.TYPE, type);
    }

    public void putParamImageUrl(String imageUrl) {
        putParam(CommandFields.StoryLabel.SINGERPIC, imageUrl);
    }

    public void setMediaFile(File mediaFile) throws FileNotFoundException {
        clearFiles();
        addFile(mediaFile);
    }

    public void putParamDuration(long duration) {
        putParam(CommandFields.StoryLabel.DURATION, String.valueOf(duration));
    }

    public void putParamMediaUrl(String url) {
        putParam(CommandFields.StoryLabel.MEDIAURL, url);
    }

    public static class CommandResponse extends UploadCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStory[] getStories() {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(
                    CommandFields.StoryLabel.LABEL_STORY_ARRAY));
        }
    }
}
