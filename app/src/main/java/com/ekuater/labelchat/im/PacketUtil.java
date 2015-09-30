
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.im.message.BaseMessage;
import com.ekuater.labelchat.im.message.MessageFactory;

public final class PacketUtil {

    public static BaseMessage toMessage(final Packet packet) {
        final MessageFactory factory = MessageFactory.getInstance();
        final BaseMessage message = factory.newMessage(packet.getType());

        if (message != null) {
            message.setEncryptType(packet.getEncryptType());
            message.setFrom(packet.getSourceAddress());
            message.setTo(packet.getDestAddress());
            message.fromByteArray(packet.getBody());
        }

        return message;
    }
}
