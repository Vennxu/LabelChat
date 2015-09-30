package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

public class AuthenticationMessage extends StringMessage {

    public AuthenticationMessage() {
        super(PacketStruct.TYPE_AUTHENTICATION);
    }
}
