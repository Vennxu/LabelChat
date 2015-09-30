package com.ekuater.labelchat.command.chatroom;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ChatRoom;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/3/5.
 *
 * @author LinYong
 */
public final class ChatRoomCmdUtils {

    private static final String TAG = ChatRoomCmdUtils.class.getSimpleName();

    public static ChatRoom toChatRoom(JSONObject json) {
        if (json == null) {
            return null;
        }

        ChatRoom chatRoom = null;

        try {
            String chatRoomId = json.getString(CommandFields.ChatRoom.CHAT_ROOM_ID);
            String chatRoomName = json.getString(CommandFields.ChatRoom.CHAT_ROOM_NAME);
            int onlineCount = json.optInt(CommandFields.ChatRoom.MEMBER_COUNT);
            String chatRoomImg = json.optString(CommandFields.ChatRoom.CHAT_ROOM_IMG);
            String chatRoomDesc = json.optString(CommandFields.ChatRoom.CHAT_ROOM_DESC);
            chatRoom = new ChatRoom();
            chatRoom.setChatRoomId(chatRoomId);
            chatRoom.setChatRoomName(chatRoomName);
            chatRoom.setOnlineCount(onlineCount);
            chatRoom.setImageUrl(chatRoomImg);
            chatRoom.setDescript(chatRoomDesc);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return chatRoom;
    }

    public static ChatRoom[] toChatRoomArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<ChatRoom> list = new ArrayList<ChatRoom>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                ChatRoom chatRoom = toChatRoom(json);
                if (chatRoom != null) {
                    list.add(chatRoom);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new ChatRoom[size]) : null;
    }
}
