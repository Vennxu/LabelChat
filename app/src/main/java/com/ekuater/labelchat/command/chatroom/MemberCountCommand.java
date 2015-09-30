package com.ekuater.labelchat.command.chatroom;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/6.
 *
 * @author LinYong
 */
public class MemberCountCommand extends SessionCommand {

    private static final String URL = CommandUrl.CHAT_ROOM_MEMBER_COUNT;

    public MemberCountCommand() {
        super();
        setUrl(URL);
    }

    public MemberCountCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public void putParamChatRoomId(String chatRoomId) {
        putParam(CommandFields.ChatRoom.CHAT_ROOM_ID, chatRoomId);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public int getMemberCount() {
            return getValueInt(CommandFields.ChatRoom.MEMBER_COUNT);
        }
    }
}
