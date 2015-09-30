package com.ekuater.labelchat.command.contact;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.album.AlbumCmdUtils;
import com.ekuater.labelchat.command.interest.InterestCmdUtils;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.command.labelstory.LabelStoryCmdUtils;
import com.ekuater.labelchat.command.tag.TagCmdUtils;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author LinYong
 */
public final class ContactCmdUtils {

    private static final String TAG = ContactCmdUtils.class.getSimpleName();

    public static Stranger toStranger(JSONObject json) {
        if (json == null) {
            return null;
        }

        Stranger stranger = null;

        try {
            String userId = json.getString(CommandFields.Stranger.USER_ID);
            String labelCode = json.optString(CommandFields.Stranger.LABEL_CODE);
            String nickname = json.optString(CommandFields.Stranger.NICKNAME);
            String mobile = json.optString(CommandFields.Stranger.MOBILE);
            String avatar = json.optString(CommandFields.Stranger.AVATAR);
            String avatarThumb = json.optString(CommandFields.Stranger.AVATAR_THUMB);
            UserLabel[] labels = LabelCmdUtils.toUserLabelArray(
                    json.optJSONArray(CommandFields.Stranger.LABELS));
            boolean bubbleUp = json.optBoolean(CommandFields.Stranger.POP_USER);
            int sex = json.optInt(CommandFields.Stranger.SEX);
            long birthday = json.optLong(CommandFields.Stranger.BIRTHDAY, Long.MAX_VALUE);
            int age = json.optInt(CommandFields.Stranger.AGE, -1);
            int constellation = json.optInt(CommandFields.Stranger.CONSTELLATION, -1);
            String province = json.optString(CommandFields.Stranger.PROVINCE);
            String city = json.optString(CommandFields.Stranger.CITY);
            String school = json.optString(CommandFields.Stranger.SCHOOL);
            String signature = json.optString(CommandFields.Stranger.SIGNATURE);
            String position = json.optString(CommandFields.Stranger.POSITION);
            String theme = json.optString(CommandFields.Stranger.THEME);
            AlbumPhoto[] albumPhotos = AlbumCmdUtils.toAlbumPhotoArray(
                    json.optJSONArray(CommandFields.Album.PHOTO_ARRAY));
            LabelStory[] labelStories = LabelStoryCmdUtils.toLabelStoryArray(
                    json.optJSONArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
            InterestType[] interestTypes = InterestCmdUtils.toInterestTypeArray(
                    json.optJSONArray(CommandFields.Interest.INTEREST_TYPE_ARRAY));
            UserTag[] userTags = TagCmdUtils.toUserTagArray(
                    json.optJSONArray(CommandFields.Tag.TAG_ARRAY));
            int visitorCount = json.optInt(CommandFields.User.VISITED);
            int myPhotoTotal = json.optInt(CommandFields.Album.MY_PHOTO_TOTAL);
            int height = json.optInt(CommandFields.Stranger.HEIGHT);
            String job = json.optString(CommandFields.Stranger.JOB);

            stranger = new Stranger();
            stranger.setLabelCode(labelCode);
            stranger.setUserId(userId);
            stranger.setNickname(nickname);
            stranger.setMobile(mobile);
            stranger.setAvatar(avatar);
            stranger.setAvatarThumb(avatarThumb);
            stranger.setLabels(labels);
            stranger.setSex(sex);
            stranger.setBirthday(birthday);
            stranger.setAge(age);
            stranger.setConstellation(constellation);
            stranger.setProvince(province);
            stranger.setCity(city);
            stranger.setSchool(school);
            stranger.setSignature(signature);
            stranger.setLocationByString(position);
            stranger.setIsBubbleUpUser(bubbleUp);
            stranger.setTheme(TextUtils.isEmpty(theme) ? null : UserTheme.fromThemeName(theme));
            stranger.setAlbumPhotos(albumPhotos);
            stranger.setLabelStories(labelStories);
            stranger.setInterestTypes(interestTypes);
            stranger.setUserTags(userTags);
            stranger.setVisitorCount(visitorCount);
            stranger.setMyPhotoTotal(myPhotoTotal);
            stranger.setHeight(height);
            stranger.setJob(job);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return stranger;
    }

    public static Stranger[] toStrangerArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<Stranger> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                Stranger user = toStranger(json);
                if (user != null) {
                    list.add(user);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new Stranger[size]) : null;
    }

    public static UserContact toContact(JSONObject json) {
        if (json == null) {
            return null;
        }

        UserContact contact = null;

        try {
            String labelCode = json.getString(CommandFields.Contact.LABEL_CODE);
            String userId = json.getString(CommandFields.Contact.USER_ID);

            String nickname = json.optString(CommandFields.Contact.NICKNAME);
            String mobile = json.optString(CommandFields.Contact.MOBILE);
            String avatar = json.optString(CommandFields.Contact.AVATAR);
            String avatarThumb = json.optString(CommandFields.Contact.AVATAR_THUMB);
            UserLabel[] labels = LabelCmdUtils.toUserLabelArray(
                    json.optJSONArray(CommandFields.Contact.LABELS));
            String remark = json.optString(CommandFields.Contact.REMARK);
            int sex = json.optInt(CommandFields.Contact.SEX);
            long birthday = json.optLong(CommandFields.Contact.BIRTHDAY, Long.MAX_VALUE);
            int age = json.optInt(CommandFields.Contact.AGE, -1);
            int constellation = json.optInt(CommandFields.Contact.CONSTELLATION, -1);
            String province = json.optString(CommandFields.Contact.PROVINCE);
            String city = json.optString(CommandFields.Contact.CITY);
            String school = json.optString(CommandFields.Contact.SCHOOL);
            String signature = json.optString(CommandFields.Contact.SIGNATURE);
            String position = json.optString(CommandFields.Contact.POSITION);
            String appearanceFace = json.optString(CommandFields.Contact.APPEARANCE_FACE);
            String theme = json.optString(CommandFields.Contact.THEME);
            AlbumPhoto[] albumPhotos = AlbumCmdUtils.toAlbumPhotoArray(
                    json.optJSONArray(CommandFields.Album.PHOTO_ARRAY));
            LabelStory[] labelStories = LabelStoryCmdUtils.toLabelStoryArray(
                    json.optJSONArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
            InterestType[] interestTypes = InterestCmdUtils.toInterestTypeArray(
                    json.optJSONArray(CommandFields.Interest.INTEREST_TYPE_ARRAY));
            UserTag[] userTags = TagCmdUtils.toUserTagArray(
                    json.optJSONArray(CommandFields.Tag.TAG_ARRAY));
            int visitorCount = json.optInt(CommandFields.User.VISITED);
            int myPhotoTotal = json.optInt(CommandFields.Album.MY_PHOTO_TOTAL);
            int height = json.optInt(CommandFields.Contact.HEIGHT);
            String job = json.optString(CommandFields.Contact.JOB);

            contact = new UserContact();
            contact.setId(-1L);
            contact.setLabelCode(labelCode);
            contact.setUserId(userId);
            contact.setNickname(nickname);
            contact.setMobile(mobile);
            contact.setAvatar(avatar);
            contact.setAvatarThumb(avatarThumb);
            contact.setLabels(labels);
            contact.setRemarkName(remark);
            contact.setSex(sex);
            contact.setBirthday(birthday);
            contact.setAge(age);
            contact.setConstellation(constellation);
            contact.setProvince(province);
            contact.setCity(city);
            contact.setSchool(school);
            contact.setSignature(signature);
            contact.setLocationByString(position);
            contact.setAppearanceFace(appearanceFace);
            contact.setTheme(TextUtils.isEmpty(theme) ? null : UserTheme.fromThemeName(theme));
            contact.setAlbumPhotos(albumPhotos);
            contact.setLabelStories(labelStories);
            contact.setInterestTypes(interestTypes);
            contact.setUserTags(userTags);
            contact.setVisitorCount(visitorCount);
            contact.setMyPhotoTotal(myPhotoTotal);
            contact.setHeight(height);
            contact.setJob(job);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return contact;
    }

    public static UserContact[] toContactArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<UserContact> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserContact user = toContact(json);
                if (user != null) {
                    list.add(user);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new UserContact[size]) : null;
    }

    public static PraiseStranger toPraiseStranger(JSONObject json) {
        if (json == null) {
            return null;
        }

        PraiseStranger instance = null;

        try {
            Stranger stranger = toStranger(json.getJSONObject(CommandFields.Stranger.STRANGER));
            int praiseCount = json.getInt(CommandFields.UserLabel.PRAISE_COUNT);

            if (stranger != null) {
                instance = new PraiseStranger(stranger, null, praiseCount);
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return instance;
    }

    public static PraiseStranger[] toPraiseStrangerArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<PraiseStranger> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                PraiseStranger praiseStranger = toPraiseStranger(json);
                if (praiseStranger != null) {
                    list.add(praiseStranger);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new PraiseStranger[size]) : null;
    }

    public static LiteStranger toLiteStranger(JSONObject json) {
        if (json == null) {
            return null;
        }

        LiteStranger stranger = null;

        try {
            String userId = json.getString(CommandFields.Stranger.USER_ID);
            String labelCode = json.optString(CommandFields.Stranger.LABEL_CODE);
            String nickname = json.getString(CommandFields.Stranger.NICKNAME);
            String avatarThumb = json.getString(CommandFields.Stranger.AVATAR_THUMB);
            int gender = json.optInt(CommandFields.User.SEX);

            stranger = new LiteStranger();
            stranger.setUserId(userId);
            stranger.setLabelCode(labelCode);
            stranger.setNickname(nickname);
            stranger.setAvatarThumb(avatarThumb);
            stranger.setGender(gender);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return stranger;
    }

    public static LiteStranger[] toLiteStrangerArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<LiteStranger> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LiteStranger user = toLiteStranger(json);
                if (user != null) {
                    list.add(user);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new LiteStranger[size]) : null;
    }
}
