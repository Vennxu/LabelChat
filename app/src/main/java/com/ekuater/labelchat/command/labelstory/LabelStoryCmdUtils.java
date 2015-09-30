package com.ekuater.labelchat.command.labelstory;


import android.text.TextUtils;
import android.util.Log;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.throwphoto.ThrowCmdUtils;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.datastruct.LabelStoryChildComment;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.LabelStoryGradeUser;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Label on 2014/12/31.
 *
 * @author Xu wenxiang
 */
public class LabelStoryCmdUtils {

    private static final String TAG = LabelStoryCmdUtils.class.getSimpleName();

    public static LabelStory toLabelStory(JSONObject json) {
        if (json == null) {
            return null;
        }
        LabelStory story = null;
        try {
            LabelStory labelStory = new LabelStory();
            if (json.has(CommandFields.StoryLabel.LABEL_STORY_ID)) {
                String labelStoryId = json.getString(CommandFields.StoryLabel.LABEL_STORY_ID);
                if (!labelStoryId.equals("0")) {
                    String categoryId = isHasKey(json, CommandFields.StoryLabel.CATEGORY_ID);
                    String labelContent = json.optString(CommandFields.StoryLabel.STORY_CONTENT);
                    String praise = json.getString(CommandFields.StoryLabel.PRAISE);
                    long createDate = json.getLong(CommandFields.StoryLabel.CREATE_DATE);
                    long modifyDate = json.optLong(CommandFields.StoryLabel.MODIFY_DATE);
                    String authorUserId = json.getString(CommandFields.StoryLabel.AUTHOR_USER_ID);
                    String commentNum = json.optString(CommandFields.StoryLabel.COMMENT_NUM);
                    int letterNum = json.optInt(CommandFields.StoryLabel.SECRET_NUM);
                    String isPraise = json.optString(CommandFields.StoryLabel.ISPRAISE);
                    String userVo = isHasKey(json, CommandFields.StoryLabel.LABEL_STORY_USER_VO);
                    String friendCounts = isHasKey(json, CommandFields.StoryLabel.FRIEND_STRORY_COUNT);
                    String praiseUserArray = json.optString(CommandFields.StoryLabel.PRAISE_USER_ARRAY);
                    String browseNum = json.optString(CommandFields.StoryLabel.BROWSE_NUM);
                    String browserUserArray = json.optString(CommandFields.StoryLabel.BROWER_USER_ARRAY);
                    String isFollowing = json.optString(CommandFields.StoryLabel.ISFOLLOWING);
                    int labelStoryTotal = json.optInt(CommandFields.StoryLabel.MY_STORY_TOTAL);
                    String type = json.optString(CommandFields.StoryLabel.TYPE, LabelStory.TYPE_TXT_IMG);
                    String media = json.optString(CommandFields.StoryLabel.MEDIA);
                    long duration = json.optLong(CommandFields.StoryLabel.DURATION);

                    labelStory.setStoryTotal(labelStoryTotal);
                    labelStory.setCategoryId(categoryId);
                    labelStory.setContent(labelContent);
                    labelStory.setCreateDate(createDate);
                    labelStory.setModifyDate(modifyDate);
                    labelStory.setCommentNum(commentNum);
                    labelStory.setLetterNum(letterNum);
                    labelStory.setPraise(praise);
                    labelStory.setIsPraise(isPraise);
                    labelStory.setIsFollowing(isFollowing);
                    labelStory.setAuthorUserId(authorUserId);
                    labelStory.setFriendStoryCount(friendCounts);
                    labelStory.setBrowseNum(browseNum);
                    labelStory.setType(type);
                    labelStory.setMedia(media);
                    labelStory.setDuration(duration);

                    if (!TextUtils.isEmpty(browserUserArray)) {
                        PickPhotoUser[] pickPhotoUser = ThrowCmdUtils.toPhotoCheckArray(new JSONArray(browserUserArray));
                        if (pickPhotoUser != null) {
                            labelStory.setPickPhotoUser(pickPhotoUser);
                        }
                    }
                    if (!TextUtils.isEmpty(userVo)) {
                        Stranger stranger = ContactCmdUtils.toStranger(new JSONObject(userVo));
                        if (stranger != null) {
                            labelStory.setStranger(stranger);
                        }
                    }
                    if (!TextUtils.isEmpty(praiseUserArray)) {
                        if (!TextUtils.isEmpty(praiseUserArray)) {
                            UserPraise[] userPraise = toUserPraiseArray(new JSONArray(praiseUserArray));
                            if (userPraise != null && userPraise.length > 0) {
                                Log.d("cmds", praiseUserArray);
                                Log.d("cmds", userPraise.length + " ");
                                labelStory.setUserPraise(userPraise);
                            }
                        }
                    }
                    if (json.has(CommandFields.StoryLabel.LABEL_STORY_IMGS)) {
                        String storyImgs = json.optString(CommandFields.StoryLabel.LABEL_STORY_IMGS);
                        String storyImgThumbs = json.optString(CommandFields.StoryLabel.LABEL_STORY_IMG_THUMBS);
                        if (!TextUtils.isEmpty(storyImgs)) {
                            labelStory.setImages(storyImgs.split(";"));
                        }
                        if (!TextUtils.isEmpty(storyImgThumbs)) {
                            labelStory.setThumbImages(storyImgThumbs.split(";"));
                        }
                    }

                    if (json.has(CommandFields.StoryLabel.CATEGORY_VO)) {
                        String category = json.getString(CommandFields.StoryLabel.CATEGORY_VO);
                        if (!TextUtils.isEmpty(category)) {
                            labelStory.setCategory(toCategory(new JSONObject(category)));
                        }
                    }
                }

                labelStory.setLabelStoryId(labelStoryId);

            }
            String labelStoryComments = null;
            if (json.has(CommandFields.StoryLabel.STORY_COMMENT_ARRAY)) {
                labelStoryComments = json.getString(CommandFields.StoryLabel.STORY_COMMENT_ARRAY);
            }
            if (!TextUtils.isEmpty(labelStoryComments)) {
                labelStory.setLabelStoryComments(toLabelStoryCommentsArray(new JSONArray(labelStoryComments)));
            }
            story = labelStory;
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return story;
    }

    public static LabelStoryComments toLabelStoryComments(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        LabelStoryComments labelStoryComments = new LabelStoryComments();
        String labelStoryCommentId = json.optString(CommandFields.StoryLabel.STORY_COMMENT_ID);
        String labelStoryId = json.optString(CommandFields.StoryLabel.LABEL_STORY_ID);
        String storyComment = json.optString(CommandFields.StoryLabel.STORY_COMMENT);
        long commentCreateDate = json.optLong(CommandFields.StoryLabel.CREATE_DATE);
        String commentStranger = json.optString(CommandFields.StoryLabel.LABEL_STORY_USER_VO);
        String commentPraise = json.optString(CommandFields.StoryLabel.PRAISE);
        String parentCommentId = json.optString(CommandFields.StoryLabel.PARENT_COMMENT_ID);
        String replyNickName = json.optString(CommandFields.StoryLabel.REPLY_NICK_NAME);

        labelStoryComments.setmLabelStoryId(labelStoryId);
        labelStoryComments.setmStroyCommentId(labelStoryCommentId);
        labelStoryComments.setmStoryComment(storyComment);
        labelStoryComments.setmCreateDate(commentCreateDate);
        labelStoryComments.setmCommentPraise(commentPraise);
        labelStoryComments.setmCommentIsPraise("N");

        if (!TextUtils.isEmpty(commentStranger)) {
            Stranger stranger = ContactCmdUtils.toStranger(new JSONObject(commentStranger));
            if (stranger != null) {
                labelStoryComments.setmStranger(stranger);
            }
        }
        labelStoryComments.setmParentCommentId(parentCommentId);
        labelStoryComments.setmReplyNickName(replyNickName);
        return labelStoryComments;
    }

    public static LabelStoryChildComment[] toLabelStoryChildCommentsArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toLabelStoryChildCommentsArray null");
            return null;
        }
        ArrayList<LabelStoryChildComment> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelStoryChildComment labelStoryChildComment = toLabelStoryChildComments(json);
                if (labelStoryChildComment != null) {
                    L.w(TAG, "toLabelStoryArray");
                    list.add(labelStoryChildComment);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStoryChildComment[size]) : null;
    }

    public static LabelStoryChildComment toLabelStoryChildComments(JSONObject json) throws JSONException {
        if (json == null) {
            L.w(TAG, "toLabelStoryChildComments null");
            return null;
        }
        LabelStoryChildComment labelStoryChildComment = new LabelStoryChildComment();
        String parentCommentId = json.optString(CommandFields.StoryLabel.PARENT_COMMENT_ID);
        String replyNickName = json.optString(CommandFields.StoryLabel.REPLY_NICK_NAME);
        long createDate = json.optLong(CommandFields.StoryLabel.CREATE_DATE);
        String storyComment = json.optString(CommandFields.StoryLabel.STORY_COMMENT);
        String userVo = json.optString(CommandFields.User.USER);
        labelStoryChildComment.setmParentCommentId(parentCommentId);
        labelStoryChildComment.setmReplyNickName(replyNickName);
        labelStoryChildComment.setmCreateDate(createDate);
        labelStoryChildComment.setmStoryComment(storyComment);
        if (!TextUtils.isEmpty(userVo)) {
            Stranger stranger = ContactCmdUtils.toStranger(new JSONObject(userVo));
            labelStoryChildComment.setmStranger(stranger);
        }
        return labelStoryChildComment;
    }

    public static LabelStory[] toLabelStoryArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toLabelStoryArray null");
            return null;
        }
        ArrayList<LabelStory> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelStory labelStory = toLabelStory(json);
                if (labelStory != null) {
                    L.w(TAG, "toLabelStoryArray");
                    list.add(labelStory);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStory[size]) : null;
    }

    public static LabelStory[] toLabelStoryArray(JSONArray jsonArray, Stranger stranger) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toLabelStoryArray null");
            return null;
        }
        ArrayList<LabelStory> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelStory labelStory = toLabelStory(json);
                if (labelStory != null) {
                    labelStory.setStranger(stranger);
                    L.w(TAG, "toLabelStoryArray");
                    list.add(labelStory);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStory[size]) : null;
    }

    private static void mappingKey(JSONObject json, String[][] keyMap) throws JSONException {
        for (String[] item : keyMap) {
            if (json.has(item[0])) {
                json.put(item[1], json.remove(item[0]));
            }
        }
    }

    private static final String[][] STORY_KEY_MAP = new String[][]{
            {CommandFields.Dynamic.COMMENT_ID, CommandFields.StoryLabel.STORY_COMMENT_ID},
            {CommandFields.Dynamic.OBJECT_ID, CommandFields.StoryLabel.LABEL_STORY_ID},
            {CommandFields.Confide.CONFIDE_COMMENT, CommandFields.StoryLabel.STORY_COMMENT}
    };

    public static LabelStoryComments[] toLabelStoryCommentsArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toLabelStoryCommentsArray null");
            return null;
        }
        ArrayList<LabelStoryComments> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                mappingKey(json, STORY_KEY_MAP);
                LabelStoryComments LabelStoryComments = toLabelStoryComments(json);
                if (LabelStoryComments != null) {
                    L.w(TAG, "toLabelStoryCommentsArray");
                    list.add(LabelStoryComments);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStoryComments[size]) : null;
    }

    public static UserLabel toUserLabel(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        String labelId = json.optString(CommandFields.StoryLabel.CATEGORY_ID);
        String lableName = json.optString(CommandFields.StoryLabel.CATEGORY_NAME);
        int praise = json.optInt(CommandFields.Label.PRAISE_COUNT);
        return new UserLabel(lableName, labelId, praise);
    }

    public static UserPraise[] toUserPraiseArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toUserPraiseArray null");
            return null;
        }
        ArrayList<UserPraise> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserPraise userPraise = toUserPraise(json);
                if (userPraise != null) {
                    L.w(TAG, "toUserPraiseArray");
                    list.add(userPraise);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new UserPraise[size]) : null;
    }

    private static UserPraise toUserPraise(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        String userId = json.optString(CommandFields.User.USER_ID);
        String avatarThumb = json.optString(CommandFields.User.AVATAR_THUMB);
        String userName = json.optString(CommandFields.User.NICKNAME);
        long time = json.optLong(CommandFields.Label.CREATE_TIME);
        UserPraise userPraise = new UserPraise();
        userPraise.setmPraiseUserId(userId);
        userPraise.setmPraiseUserAvatarThumb(avatarThumb);
        userPraise.setmPraiseUserName(userName);
        userPraise.setmTime(time);
        return userPraise;
    }

    private static LabelStoryGradeUser toGradeUser(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        String userId = json.optString(CommandFields.User.USER_ID);
        String avatarThumb = json.optString(CommandFields.User.AVATAR_THUMB);
        String userName = json.optString(CommandFields.User.NICKNAME);
        long createDate = json.optLong(CommandFields.Label.CREATE_TIME);
        LabelStoryGradeUser gradeUser = new LabelStoryGradeUser();
        gradeUser.setmUserId(userId);
        gradeUser.setmAvataThumb(avatarThumb);
        gradeUser.setmNickName(userName);
        gradeUser.setmCreateDate(createDate);
        return gradeUser;
    }

    private static String isHasKey(JSONObject json, String key) throws JSONException {
        String value = null;
        if (json.has(key)) {
            value = json.getString(key);
        }
        return value;
    }

    public static LabelStoryGradeUser[] toGradeUserArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0) {
            L.w(TAG, "toUserPraiseArray null");
            return null;
        }
        ArrayList<LabelStoryGradeUser> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelStoryGradeUser gradeUser = toGradeUser(json);
                if (gradeUser != null) {
                    L.w(TAG, "toUserPraiseArray");
                    list.add(gradeUser);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStoryGradeUser[size]) : null;
    }

    public static LabelStoryCategory[] toCatetoryArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<LabelStoryCategory> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelStoryCategory category = toCategory(json);
                if (category != null) {
                    list.add(category);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelStoryCategory[size]) : null;
    }

    public static LabelStoryCategory toCategory(JSONObject json) {
        if (json == null) {
            return null;
        }
        LabelStoryCategory labelStoryCategory;
        try {
            labelStoryCategory = new LabelStoryCategory();
            String categoryId = json.getString(CommandFields.StoryLabel.CATEGORY_ID);
            String categoryName = json.getString(CommandFields.StoryLabel.CATEGORY_NAME);
            int categoryTotal = json.getInt(CommandFields.StoryLabel.DYNAMIC_TOTAL);
            int categoryNum = json.getInt(CommandFields.StoryLabel.SERIAL_NUM);
            String categoryImage = json.optString(CommandFields.StoryLabel.CATEGORY_IMG);

            labelStoryCategory.setmCategoryId(categoryId);
            labelStoryCategory.setmCategoryName(categoryName);
            labelStoryCategory.setmDynamicTotal(categoryTotal);
            labelStoryCategory.setmSerialNum(categoryNum);
            labelStoryCategory.setmImageUrl(categoryImage);
        } catch (Exception e) {
            return null;
        }
        return labelStoryCategory;
    }
}
