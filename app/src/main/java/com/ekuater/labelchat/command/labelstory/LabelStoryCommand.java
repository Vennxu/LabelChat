package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.throwphoto.ThrowCmdUtils;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.ThrowPhoto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Label on 2014/12/31
 *
 * @author Xu wenxiang.
 */
public class LabelStoryCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_STORY_LIST;
    private static final String MY_URL= CommandUrl.LABEL_STORY_MY_STORY;

    public LabelStoryCommand(){
        super();
        setUrl(URL);
    }
    public LabelStoryCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }
    public LabelStoryCommand(String session, String userId,String urlFlags) {
        super(session, userId);
        setUrl(MY_URL);
    }
    public void putQueryUserId(String queryUserId){
        putParam(CommandFields.StoryLabel.QUERY_USER_ID,queryUserId);
    }
    public void putParamLabelId(String categoryId){
        putParam(CommandFields.StoryLabel.CATEGORY_ID,categoryId);
    }
    public void putParamQueryUserId(String queryUserId){
        putParam(CommandFields.StoryLabel.QUERY_USER_ID,queryUserId);
    }
    public void putParamRequestTime(String requestTime){
        putParam(CommandFields.StoryLabel.REQUEST_TIME,requestTime);
    }
    public void putParamContent(String content){
        putParam(CommandFields.StoryLabel.STORY_CONTENT,content);
    }
    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
        public int getFrendsCount() throws JSONException {
           int frendsCount=getValueInt(CommandFields.StoryLabel.FRIEND_STRORY_COUNT);
            return frendsCount;
        }
        public LabelStory[] getLabelStory() {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
        }
        public LabelStory[] getLabelStory(Stranger stranger) {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY),stranger);
        }
    }
    public static class CommandImageResponse extends BaseCommand.CommandResponse {
        public CommandImageResponse(JSONObject response) {
            super(response);
        }
        public LabelStory[] getLabelStory() {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
        }
    }
}
