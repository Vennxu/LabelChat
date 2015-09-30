package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author LinYong
 */
public class WeeklyStarsMessage {

    public static class WeeklyStar extends Stranger {

        public static final String NEW_FRIEND_FIELD = "newFriend";

        public static WeeklyStar[] toWeeklyStarArray(JSONArray jsonArray) {
            if (jsonArray == null || jsonArray.length() <= 0) {
                return null;
            }

            ArrayList<WeeklyStar> list = new ArrayList<WeeklyStar>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.optJSONObject(i);
                if (json != null) {
                    Stranger user = ContactCmdUtils.toStranger(json);
                    if (user != null) {
                        int newFriend = json.optInt(NEW_FRIEND_FIELD, 0);
                        list.add(new WeeklyStar(user, newFriend));
                    }
                }
            }

            final int size = list.size();
            return (size > 0) ? list.toArray(new WeeklyStar[size]) : null;
        }

        private int mNewFriendCount;

        public WeeklyStar(Stranger stranger, int newFriendCount) {
            super(stranger);
            mNewFriendCount = newFriendCount;
        }

        public int getNewFriendCount() {
            return mNewFriendCount;
        }
    }

    private WeeklyStar[] mStars;

    public WeeklyStarsMessage() {
    }

    public WeeklyStar[] getStars() {
        return mStars;
    }

    public void setStars(WeeklyStar[] stars) {
        mStars = stars;
    }

    public static WeeklyStarsMessage build(JSONObject json) {
        WeeklyStarsMessage newMessage = null;

        if (json != null) {
            WeeklyStar[] stars = WeeklyStar.toWeeklyStarArray(
                    json.optJSONArray(SystemPushFields.FIELD_STRANGERS));
            if (stars != null && stars.length > 0) {
                newMessage = new WeeklyStarsMessage();
                newMessage.setStars(stars);
            }
        }

        return newMessage;
    }

    public static WeeklyStarsMessage build(SystemPush push) {
        WeeklyStarsMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_WEEKLY_STAR) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}
