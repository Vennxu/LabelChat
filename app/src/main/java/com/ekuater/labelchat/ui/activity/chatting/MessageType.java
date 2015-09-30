package com.ekuater.labelchat.ui.activity.chatting;

import com.ekuater.labelchat.datastruct.ChatMessage;

/**
 * Created by Leo on 2015/2/10.
 *
 * @author LinYong
 */
@SuppressWarnings("PointlessArithmeticExpression")
public final class MessageType {

    public static final int MSG_DIRECTION_COUNT = ChatMessage.DIRECTION_COUNT;
    public static final int MSG_TYPE_COUNT = ChatMessage.TYPE_COUNT;

    public static final int MSG_TYPE_TOTAL_COUNT = MSG_DIRECTION_COUNT * MSG_TYPE_COUNT;
    public static final int EXT_TYPE_START = MSG_TYPE_TOTAL_COUNT;

    public static final int EXT_TYPE_TIP = (EXT_TYPE_START + 0);
    public static final int EXT_TYPE_COUNT = 1;

    public static int getTypeCount() {
        return MSG_TYPE_TOTAL_COUNT + EXT_TYPE_COUNT;
    }

    public static int getMessageType(ChatMessage chatMessage) {
        int type = chatMessage.getType();

        if (type >= EXT_TYPE_START) {
            return type;
        } else {
            return ((chatMessage.getDirection() * MSG_TYPE_COUNT)
                    + chatMessage.getType());
        }
    }

    public static ChatMessage newTipMessage(String tip, String senderId) {
        ChatMessage tipMessage = new ChatMessage();
        tipMessage.setType(EXT_TYPE_TIP);
        tipMessage.setTime(0L);
        tipMessage.setContent(tip);
        tipMessage.setSenderId(senderId);
        return tipMessage;
    }
}
