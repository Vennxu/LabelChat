
package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * System notification class
 *
 * @author LinYong
 */
public class SystemPush implements Parcelable, Comparable<SystemPush> {

    // System push message state
    public static final int STATE_UNPROCESSED = 0;
    public static final int STATE_PROCESSED = 1;

    private static final String EMPTY_STRING = "";

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private long id;
    private int type;
    private long time;
    private int state;
    private String content;
    private String flag;

    @Override
    public String toString() {
        return "SystemPush{" +
                "id=" + id +
                ", type=" + type +
                ", time=" + time +
                ", state=" + state +
                ", content='" + content + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }

    public SystemPush() {
        id = -1L;
        type = SystemPushType.TYPE_ILLEGAL;
        time = getCurrentTime();
        state = STATE_UNPROCESSED;
        content = EMPTY_STRING;
        flag = EMPTY_STRING;
    }

    private SystemPush(Parcel in) {
        this.id = in.readLong();
        this.type = in.readInt();
        this.time = in.readLong();
        this.state = in.readInt();
        this.content = in.readString();
        this.flag = in.readString();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public int compareTo(@NonNull SystemPush another) {
        return (int) (time - another.time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.type);
        dest.writeLong(this.time);
        dest.writeInt(this.state);
        dest.writeString(this.content);
        dest.writeString(this.flag);
    }

    public static final Creator<SystemPush> CREATOR = new Creator<SystemPush>() {
        public SystemPush createFromParcel(Parcel source) {
            return new SystemPush(source);
        }

        public SystemPush[] newArray(int size) {
            return new SystemPush[size];
        }
    };

    public static SystemPush build(JSONObject json) {
        SystemPush systemPush = null;

        try {
            final int type = json.getInt(SystemPushFields.FIELD_TYPE);
            final long time = json.getLong(SystemPushFields.FIELD_TIME);
            final String body = json.getString(SystemPushFields.FIELD_BODY);

            systemPush = new SystemPush();
            systemPush.setId(-1L);
            systemPush.setState(STATE_UNPROCESSED);
            systemPush.setType(type);
            systemPush.setTime(time);
            systemPush.setContent(body);
            systemPush.setFlag(EMPTY_STRING);
            systemPush.setFlag(SystemPushHelper.getSystemPushFlag(systemPush));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return systemPush;
    }
}
