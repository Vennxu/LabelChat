package com.ekuater.labelchat.command.tag;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.TagType;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class TagCmdUtils {

    private static final String TAG = TagCmdUtils.class.getSimpleName();

    public static UserTag toUserTag(JSONObject json) {
        if (json == null) {
            return null;
        }
        int tagId = json.optInt(CommandFields.Tag.TAG_ID);
        String tagName = json.optString(CommandFields.Tag.TAG_NAME);
        String tagColor = json.optString(CommandFields.Tag.TAG_COLOR);
        String tagSelectedColor = json.optString(CommandFields.Tag.TAG_SELECTED_COLOR);
        int typeId = json.optInt(CommandFields.Tag.TYPE_ID);

        UserTag tag = new UserTag();
        tag.setTagId(tagId);
        tag.setTagName(tagName);
        tag.setTagColor(tagColor);
        tag.setTagSelectedColor(tagSelectedColor);
        tag.setTypeId(typeId);
        return tag;
    }

    public static JSONObject toJson(UserTag tag) {
        if (tag == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.Tag.TAG_ID, tag.getTagId());
            json.put(CommandFields.Tag.TAG_NAME, tag.getTagName());
            json.put(CommandFields.Tag.TAG_COLOR, tag.getTagColor());
            json.put(CommandFields.Tag.TAG_SELECTED_COLOR, tag.getTagSelectedColor());
            json.put(CommandFields.Tag.TYPE_ID, tag.getTypeId());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static UserTag[] toUserTagArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<UserTag> list = new ArrayList<UserTag>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserTag tag = toUserTag(json);
                if (tag != null) {
                    list.add(tag);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new UserTag[size]) : null;
    }

    public static JSONArray toJsonArray(UserTag[] tags) {
        JSONArray jsonArray = null;

        if (tags != null && tags.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<JSONObject>();

            for (UserTag tag : tags) {
                JSONObject json = toJson(tag);
                if (json != null) {
                    list.add(json);
                }
            }

            if (list.size() > 0) {
                jsonArray = new JSONArray(list);
            }
        }

        return jsonArray;
    }

    public static TagType toTagType(JSONObject json) {
        if (json == null) {
            return null;
        }

        TagType type = null;

        try {
            int typeId = json.getInt(CommandFields.Tag.TYPE_ID);
            String typeName = json.getString(CommandFields.Tag.TYPE_NAME);
            int maxSelect = json.getInt(CommandFields.Tag.MAX_SELECT);
            UserTag[] tags = toUserTagArray(json.optJSONArray(CommandFields.Tag.TAG_ARRAY));

            type = new TagType();
            type.setTypeId(typeId);
            type.setTypeName(typeName);
            type.setMaxSelect(maxSelect);
            type.setTags(tags);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return type;
    }

    public static TagType[] toTagTypeArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<TagType> list = new ArrayList<TagType>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                TagType type = toTagType(json);
                if (type != null) {
                    list.add(type);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new TagType[size]) : null;
    }
}
