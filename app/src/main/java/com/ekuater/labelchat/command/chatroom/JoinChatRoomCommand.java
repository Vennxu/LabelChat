package com.ekuater.labelchat.command.chatroom;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/5.
 *
 * @author LinYong
 */
public class JoinChatRoomCommand extends UserCommand {

    private static final String URL = CommandUrl.CHAT_ROOM_JOIN;

    public JoinChatRoomCommand() {
        super();
        setUrl(URL);
    }

    public JoinChatRoomCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamChatRoomId(String chatRoomId) {
        putParam(CommandFields.ChatRoom.CHAT_ROOM_ID, chatRoomId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
