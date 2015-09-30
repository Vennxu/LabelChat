
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

/**
 * Message factory, create Message by message type.
 * 
 * @author LinYong
 */
public final class MessageFactory {

    public static MessageFactory sInstance;

    public static MessageFactory getInstance() {
        if (sInstance == null) {
            sInstance = new MessageFactory();
        }

        return sInstance;
    }

    private MessageFactory() {
    }

    public BaseMessage newMessage(int type) {
        BaseMessage message = null;

        switch (type) {
            case PacketStruct.TYPE_PING: {
                message = new PingMessage();
                break;
            }
            case PacketStruct.TYPE_MESSAGE: {
                message = new IMMessage();
                break;
            }
            case PacketStruct.TYPE_REQUEST: {
                message = new RequestMessage();
                break;
            }
            case PacketStruct.TYPE_INSTRUCTION: {
                message = new InstructionMessage();
                break;
            }
            case PacketStruct.TYPE_AUTHENTICATION: {
                message = new AuthenticationMessage();
                break;
            }
            case PacketStruct.TYPE_NOTIFICATION: {
                message = new NotificationMessage();
                break;
            }
            default: {
                break;
            }
        }

        return message;
    }
}
