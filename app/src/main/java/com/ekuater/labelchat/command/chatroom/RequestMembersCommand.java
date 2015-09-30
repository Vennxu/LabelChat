package com.ekuater.labelchat.command.chatroom;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.LiteStranger;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/6.
 *
 * @author LinYong
 */
public class RequestMembersCommand extends SessionCommand {

    private static final String URL = CommandUrl.CHAT_ROOM_MEMBERS;

    public RequestMembersCommand() {
        super();
        setUrl(URL);
    }

    public RequestMembersCommand(String session) {
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

        public LiteStranger[] getMembers() {
            return ContactCmdUtils.toLiteStrangerArray(getValueJsonArray(
                    CommandFields.Stranger.STRANGERS));
        }
    }
}
