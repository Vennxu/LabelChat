package com.ekuater.labelchat.command.chatroom;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.datastruct.ChatRoom;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/5.
 *
 * @author LinYong
 */
public class ListChatRoomCommand extends SessionCommand {

    private static final String URL = CommandUrl.CHAT_ROOM_LIST;

    public ListChatRoomCommand() {
        super();
        setUrl(URL);
    }

    public ListChatRoomCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public ChatRoom[] getChatRooms() {
            return ChatRoomCmdUtils.toChatRoomArray(getValueJsonArray(
                    CommandFields.ChatRoom.CHAT_ROOM_ARRAY));
        }
    }
}
