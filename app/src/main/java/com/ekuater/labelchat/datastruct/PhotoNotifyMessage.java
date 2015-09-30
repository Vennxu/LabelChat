package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.album.AlbumCmdUtils;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.delegate.AlbumManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/3/25.
 *
 * @author LinYong
 */
public class PhotoNotifyMessage {

    public static final String TYPE_HAS_SEEN = AlbumManager.NOTIFY_TYPE_HAS_SEEN;
    public static final String TYPE_UPLOAD_MORE = AlbumManager.NOTIFY_TYPE_UPLOAD_MORE;
    public static final String TYPE_HAS_PRAISE = AlbumManager.NOTIFY_TYPE_PRAISE;

    private String notifyType;
    private AlbumPhoto albumPhoto;
    private LiteStranger notifyUser;
    private int pushType;
    private long time;

    public PhotoNotifyMessage() {
        time = System.currentTimeMillis();
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public AlbumPhoto getAlbumPhoto() {
        return albumPhoto;
    }

    public void setAlbumPhoto(AlbumPhoto albumPhoto) {
        this.albumPhoto = albumPhoto;
    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public LiteStranger getNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(LiteStranger notifyUser) {
        this.notifyUser = notifyUser;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static PhotoNotifyMessage build(JSONObject json) {
        PhotoNotifyMessage newMessage = null;

        if (json != null) {
            String notifyType = json.optString(SystemPushFields.FIELD_PHOTO_NOTIFY_TYPE);
            AlbumPhoto albumPhoto = AlbumCmdUtils.toAlbumPhoto(
                    json.optJSONObject(SystemPushFields.FIELD_PHOTO_VO));
            LiteStranger notifyUser = ContactCmdUtils.toLiteStranger(
                    json.optJSONObject(SystemPushFields.FIELD_STRANGER));

            if (albumPhoto != null && notifyUser != null) {
                newMessage = new PhotoNotifyMessage();
                newMessage.setNotifyType(notifyType);
                newMessage.setAlbumPhoto(albumPhoto);
                newMessage.setNotifyUser(notifyUser);
            }
        }

        return newMessage;
    }

    public static PhotoNotifyMessage build(SystemPush push) {
        PhotoNotifyMessage newMessage = null;
        switch (push.getType()) {
            case SystemPushType.TYPE_PHOTO_NOTIFY:
            case SystemPushType.TYPE_UPLOAD_PHOTO:
                try {
                    newMessage = build(new JSONObject(push.getContent()));
                    if (newMessage != null) {
                        newMessage.setPushType(push.getType());
                        newMessage.setTime(push.getTime());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return newMessage;
    }
}
