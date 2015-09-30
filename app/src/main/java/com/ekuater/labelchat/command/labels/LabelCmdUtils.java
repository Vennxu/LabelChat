package com.ekuater.labelchat.command.labels;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.LabelPraise;
import com.ekuater.labelchat.datastruct.Music;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserLabelFeed;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author LinYong
 */
public final class LabelCmdUtils {

    private static final String TAG = LabelCmdUtils.class.getSimpleName();

    public static JSONObject toJson(UserLabel label) {
        if (label == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.UserLabel.LABEL_NAME, label.getName());
            json.put(CommandFields.UserLabel.LABEL_ID, label.getId());
            json.put(CommandFields.UserLabel.ADD_TIME, label.getTime());
            json.put(CommandFields.UserLabel.TOTAL_USER, label.getTotalUser());
            json.put(CommandFields.UserLabel.PRAISE_COUNT, label.getPraiseCount());
            json.put(CommandFields.UserLabel.INTEGRAL, label.getIntegral());
            json.put(CommandFields.UserLabel.FEED_USER, toJson(label.getFeed()));
            json.put(CommandFields.UserLabel.IMAGE, label.getImage());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static JSONArray toJsonArray(UserLabel[] labels) {
        JSONArray jsonArray = null;

        if (labels != null && labels.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<JSONObject>();

            for (UserLabel label : labels) {
                JSONObject json = toJson(label);
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


    public static UserLabel toUserLabel(JSONObject json) {
        if (json == null) {
            return null;
        }

        UserLabel label = null;

        try {
            String name = json.optString(CommandFields.UserLabel.LABEL_NAME);
            String id = json.optString(CommandFields.UserLabel.LABEL_ID);
            long time = json.optLong(CommandFields.UserLabel.ADD_TIME);
            long totalUser = json.optLong(CommandFields.UserLabel.TOTAL_USER);
            int praiseCount = json.optInt(CommandFields.UserLabel.PRAISE_COUNT);
            int integral = json.optInt(CommandFields.UserLabel.INTEGRAL);
            UserLabelFeed feed = toUserLabelFeed(json.optJSONObject(
                    CommandFields.UserLabel.FEED_USER));
            String image = json.optString(CommandFields.UserLabel.IMAGE);
            int gradeNum = json.optInt(CommandFields.UserLabel.GRADE_NUM);
            int gradeTotal = json.optInt(CommandFields.UserLabel.GRADE_TOTAL);

            label = new UserLabel(name, id, time, totalUser, praiseCount, integral);
            label.setFeed(feed);
            label.setImage(image);
            label.setGradeNum(gradeNum);
            label.setGradeTotal(gradeTotal);
        } catch (NumberFormatException e) {
            L.w(TAG, e);
        }

        return label;
    }

    public static UserLabel[] toUserLabelArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<UserLabel> list = new ArrayList<UserLabel>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserLabel label = toUserLabel(json);
                if (label != null) {
                    list.add(label);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new UserLabel[size]) : null;
    }

    public static JSONObject toJson(SystemLabel label) {
        if (label == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.SystemLabel.LABEL_NAME, label.getName());
            json.put(CommandFields.SystemLabel.LABEL_ID, label.getId());
            json.put(CommandFields.SystemLabel.CREATE_TIME, label.getTime());
            json.put(CommandFields.SystemLabel.TOTAL_USER, label.getTotalUser());
            json.put(CommandFields.SystemLabel.IMAGE, label.getImage());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static JSONArray toJsonArray(SystemLabel[] labels) {
        JSONArray jsonArray = null;

        if (labels != null && labels.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<JSONObject>();

            for (SystemLabel label : labels) {
                JSONObject json = toJson(label);
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

    public static SystemLabel toSystemLabel(JSONObject json) {
        if (json == null) {
            return null;
        }

        SystemLabel label = null;

        try {
            String name = json.getString(CommandFields.SystemLabel.LABEL_NAME);
            String id = json.getString(CommandFields.SystemLabel.LABEL_ID);
            String createUserId = json.optString(CommandFields.SystemLabel.CREATE_USER_ID);
            long time = json.optLong(CommandFields.SystemLabel.CREATE_TIME);
            long totalUser = json.optLong(CommandFields.SystemLabel.TOTAL_USER);
            String image = json.optString(CommandFields.SystemLabel.IMAGE);
            label = new SystemLabel(name, id, createUserId, time, totalUser);
            label.setImage(image);
        } catch (JSONException e) {
            L.w(TAG, e);
        } catch (NumberFormatException e) {
            L.w(TAG, e);
        }

        return label;
    }

    public static SystemLabel[] toSystemLabelArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<SystemLabel> list = new ArrayList<SystemLabel>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                SystemLabel label = toSystemLabel(json);
                if (label != null) {
                    list.add(label);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new SystemLabel[size]) : null;
    }

    public static JSONObject toJson(BaseLabel label) {
        if (label == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.BaseLabel.LABEL_NAME, label.getName());
            json.put(CommandFields.BaseLabel.LABEL_ID, label.getId());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static JSONArray toJsonArray(BaseLabel[] labels) {
        JSONArray jsonArray = null;

        if (labels != null && labels.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<JSONObject>();

            for (BaseLabel label : labels) {
                JSONObject json = toJson(label);
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

    public static BaseLabel toBaseLabel(JSONObject json) {
        if (json == null) {
            return null;
        }

        BaseLabel label = null;

        try {
            String name = json.getString(CommandFields.BaseLabel.LABEL_NAME);
            String id = json.getString(CommandFields.BaseLabel.LABEL_ID);
            label = new BaseLabel(name, id);
        } catch (JSONException e) {
            L.w(TAG, e);
        } catch (NumberFormatException e) {
            L.w(TAG, e);
        }

        return label;
    }

    public static BaseLabel[] toBaseLabelArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<BaseLabel> list = new ArrayList<BaseLabel>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                BaseLabel label = toBaseLabel(json);
                if (label != null) {
                    list.add(label);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new BaseLabel[size]) : null;
    }

    public static LabelPraise toLabelPraise(JSONObject json) {
        if (json == null) {
            return null;
        }

        LabelPraise labelPraise = null;

        try {
            String userId = json.getString(CommandFields.Stranger.USER_ID);
            String labelId = json.getString(CommandFields.UserLabel.LABEL_ID);
            int praiseCount = json.optInt(CommandFields.UserLabel.PRAISE_COUNT);
            labelPraise = new LabelPraise(userId, labelId, praiseCount);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return labelPraise;
    }

    public static LabelPraise[] toLabelPraiseArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<LabelPraise> list = new ArrayList<LabelPraise>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                LabelPraise labelPraise = toLabelPraise(json);
                if (labelPraise != null) {
                    list.add(labelPraise);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new LabelPraise[size]) : null;
    }

    public static JSONObject toJson(UserLabelFeed feed) {
        if (feed == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.User.USER_ID, feed.getUserId());
            json.put(CommandFields.User.NICKNAME, feed.getNickname());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static UserLabelFeed toUserLabelFeed(JSONObject json) {
        if (json == null) {
            return null;
        }

        UserLabelFeed feed = null;

        try {
            String userId = json.getString(CommandFields.User.USER_ID);
            String nickname = json.getString(CommandFields.User.NICKNAME);
            feed = new UserLabelFeed(userId, nickname);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return feed;
    }

    public static Music toMusic(JSONObject json) {
        if (json == null) {
            return null;
        }
        Music music = null;
        try {
            String songId = json.getString(CommandFields.Dynamic.SONG_ID);
            String singerId = json.optString(CommandFields.Dynamic.SINGER_ID);
            String songName = json.optString(CommandFields.Dynamic.SONG_NAME);
            String singerName = json.optString(CommandFields.Dynamic.SINGER_NAME);
            String albumName = json.optString(CommandFields.Dynamic.ALBUM_NAME);
            String singerPic = json.optString(CommandFields.Dynamic.SINGER_PIC);
            String musicUrl = json.optString(CommandFields.Dynamic.MUSIC_URL);
            long duration = json.optLong(CommandFields.Dynamic.DURATION);
            music = new Music();
            music.setSongId(songId);
            music.setSingerId(singerId);
            music.setSongName(songName);
            music.setSingerName(singerName);
            music.setAlbumName(albumName);
            music.setSingerPic(singerPic);
            music.setMusicUrl(musicUrl);
            music.setDuration(duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return music;
    }

    public static Music[] toMusicArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<Music> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                Music music = toMusic(json);
                if (music != null) {
                    list.add(music);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new Music[size]) : null;
    }
}
