
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

/**
 * @author LinYong
 */
public class RequestMessage extends JsonMessage {

    public RequestMessage() {
        super(PacketStruct.TYPE_REQUEST);
    }
}
